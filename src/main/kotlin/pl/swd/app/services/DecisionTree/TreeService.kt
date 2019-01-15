package pl.swd.app.services.DecisionTree

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.services.ConvertValueService
import pl.swd.app.services.ProjectService
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.DecisionTreeModal
import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import pl.swd.app.models.ClassifySelectedDataModel
import pl.swd.app.models.DataColumn
import pl.swd.app.models.DataRow
import pl.swd.app.models.TreeUserSelectionModel
import pl.swd.app.services.DiscretizationService
import pl.swd.app.services.ProjectSaverService
import tornadofx.*
import java.util.*
import javax.swing.tree.TreeSelectionModel
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.log2
import com.google.gson.*

@Service
class TreeService {
    @Autowired private lateinit var projectService: ProjectService
    @Autowired private lateinit var convertValueService: ConvertValueService
    @Autowired private lateinit var discretizationService: DiscretizationService

    private val filteredColumnList: ArrayList<DataColumn> = ArrayList()
    private var decisionColumn: DataColumn = DataColumn("", ArrayList())

    fun showDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColumnList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<DecisionTreeModal>(params = mapOf(DecisionTreeModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                discretizeAtributes(view.getTreeParameters(), selectedTabIndex)
                separateColumn(view.getTreeParameters(), selectedTabIndex)

                val root = buildTree()
                val quality = checkQuality(root, selectedTabIndex, view.getTreeParameters().decisionClassCol)
                ProjectSaverService.logger.debug { "${quality}" }
                separateColumn(view.getTreeParameters(), selectedTabIndex)
            }
        }
    }

    private fun checkQuality(root: TreeNode, tabIndex: Int, decisionClass: String): Double {
        val project = projectService.currentProject.value?.let { it } ?: return 0.0
        val rows = project.get().spreadSheetList[tabIndex].dataTable.rows.toList()
        val numOfRows = rows.count()
        var valid = 0.0

        val validateMap = HashMap<Int, Double>(numOfRows)

        rows.forEachIndexed { index, dataRow ->
            val treeObjectClass = traverseTree(root, dataRow)

            if (dataRow.rowValuesMap.getValue(decisionClass).value.toString().toInt() == treeObjectClass) {
                valid++
            }
        }


        return valid / numOfRows
    }

    private fun traverseTree(root: TreeNode, row: DataRow): Int {
        var currentNode = root
        var objectClass = -1


        while (!currentNode.childrens.isEmpty()) {
            var value = row.rowValuesMap.getValue(currentNode.attributeName).value.toString().toInt()
            var childrens = currentNode.childrens

            childrens.forEach {
                if (it.nodeValue == value) {
                    currentNode = it
                    return@forEach
                }
            }

            if (currentNode.decisionClass && currentNode.nodeValue == value) {
                objectClass = currentNode.decisionClassAtr
                break
            }
        }

        return objectClass
    }

    private fun generateColumnList(tabIndex: Int): ArrayList<String> {
        checkAndConvertValues(tabIndex)

        var columnNameList = ArrayList<String>()
        val project = projectService.currentProject.value?.let { it } ?: return columnNameList
        val rowValuesMap = project.get().spreadSheetList[tabIndex].dataTable.rows.first().rowValuesMap

        for (entry in rowValuesMap) {
            val columnValue = entry.value.value as String

            if (columnValue.toDoubleOrNull() != null) {
                columnNameList.add(entry.key)
            }
        }

        return columnNameList
    }

    private fun checkAndConvertValues(tabIndex: Int) {
        val project = projectService.currentProject.value?.let { it } ?: return
        val rowValuesMap = project.get().spreadSheetList[tabIndex].dataTable.rows.first().rowValuesMap

        for (entry in rowValuesMap) {
            if (rowValuesMap.containsKey("${entry.key}_convert")) continue

            val columnValue = entry.value.value as String

            if (columnValue.toDoubleOrNull() == null) convertValueService.convertSelectedColumn(entry.key, tabIndex)
        }
    }

    private fun discretizeAtributes(treeParameters: TreeUserSelectionModel, tabIndex: Int) {
        val project = projectService.currentProject.value?.let { it } ?: return
        val rowValuesMap = project.get().spreadSheetList[tabIndex].dataTable.rows.first().rowValuesMap

        for (entry in rowValuesMap) {
            if (rowValuesMap.containsKey("${entry.key}_discrete")) continue
            if (entry.key == treeParameters.decisionClassCol || entry.key == "${treeParameters.decisionClassCol}_convert") continue

            val columnValue = entry.value.value as String

            if (columnValue.toDoubleOrNull() != null) discretizationService.convertSelectedColumn(entry.key, treeParameters.sectionNumber, tabIndex)
        }
    }

    private fun separateColumn(treeParameters: TreeUserSelectionModel, tabIndex: Int) {
        val project = projectService.currentProject.value?.let { it } ?: return
        val columnList = project.get().spreadSheetList[tabIndex].dataTable.columns.toList()

        columnList.forEach {
            if (it.name.contains("_discrete")) {
                filteredColumnList.add(it)
            }

            if (it.name.contains("_convert")) {
                decisionColumn = it
            }
        }
    }

    private fun buildTree(): TreeNode {
        val allDataEntropy = calculateEntropy(calculateAllDataPropability(decisionColumn))
        val informationGain = HashMap<String, Double>()

        for (column in filteredColumnList) {
            val columnValues = column.columnValuesList.map { it.value.toString().toInt() }.distinct()
            val columnEntropy = HashMap<Int, Double>()

            for (value in columnValues) {
                val propability = calculatePropabilityForAtribute(column, decisionColumn, value)
                val entropy = calculateEntropy(propability)

                columnEntropy.put(value, entropy)
            }

            informationGain.put(column.name, calculateInformationGain(allDataEntropy, column, columnEntropy))
        }

        val firstAtr = informationGain.maxBy { it.value }!!

        val root = TreeNode(decisionColumn, filteredColumnList)
        root.attributeName = firstAtr.key
        decomposeTree(root)

        return root
    }

    private fun decomposeTree(node: TreeNode) {
        addChilds(node)

        if (node.childrens.count() != 0) {
            node.childrens.forEach {
                val allDataEntropy = calculateEntropy(calculateAllDataPropability(it.decisionColumn))
                val informationGain = HashMap<String, Double>()

                for (column in it.attributeColums) {
                    val columnValues = column.columnValuesList.map { it.value.toString().toInt() }.distinct()
                    val columnEntropy = HashMap<Int, Double>()

                    for (value in columnValues) {
                        val propability = calculatePropabilityForAtribute(column, it.decisionColumn, value)
                        val entropy = calculateEntropy(propability)

                        columnEntropy.put(value, entropy)
                    }

                    informationGain.put(column.name, calculateInformationGain(allDataEntropy, column, columnEntropy))
                }

                val firstAtr = informationGain.maxBy { it.value }

                if (firstAtr == null) {
                    return@forEach
                }

                if(it.decisionClass == true) {
                    return@forEach
                }

                it.attributeName = firstAtr.key

                decomposeTree(it)
                it.visited = true
            }
        }
    }

    private fun addChilds(node: TreeNode) {
        if (node.decisionClass == true) return

        val parentsValues = node.attributeColums.find { it.name == node.attributeName }
        val parentsUnwrap = parentsValues?.let { it } ?: return

        val column = parentsUnwrap.columnValuesList.map { it.value.toString().toInt() }
        val columnValues = column.distinct()

        val decisionClassValues = decisionColumn.columnValuesList.map { it.value.toString().toInt() }
        val map = HashMap<Int, ArrayList<Int>>()
        var filter = ArrayList<Int>()

        for (value in columnValues) {
            map.put(value, ArrayList())
        }

        column.forEachIndexed { index, i ->
            var list = map.getValue(i)
            list.add(decisionClassValues[index])
            map.put(i, list)
        }

        for (key in map.keys) {
            val list = map.getValue(key)

            if (list.distinct().count() == 1) {
                val decisionNode = TreeNode(decisionColumn, node.attributeColums, list.first())
                decisionNode.decisionClass = true
                decisionNode.visited = true
                decisionNode.nodeValue = key
                decisionNode.decisionClassAtr = list.first()
                decisionNode.parent = node
                decisionNode.attributeName = decisionColumn.name
                node.childrens.add(decisionNode)
                filter.add(key)
            }
        }

        for(value in parentsUnwrap.columnValuesList.map { it.value.toString().toInt() }.distinct().sorted()) {
            if (filter.contains(value)) {
                continue
            }

            if (node.attributeColums.size == 1) {
                val lastChildren = TreeNode(decisionColumn, ArrayList(), value)
                lastChildren.parent = node
                lastChildren.decisionClass = true
                lastChildren.visited = true
                lastChildren.nodeValue = value
                lastChildren.attributeName = decisionColumn.name

                node.attributeColums.first().columnValuesList.forEachIndexed { index, dataValue ->
                    if(dataValue.value.toString().toInt() == value) {
                        lastChildren.decisionClassAtr = node.decisionColumn.columnValuesList[index].value.toString().toInt()
                        return@forEachIndexed
                    }
                }

                node.childrens.add(lastChildren)
            } else {
                val children = TreeNode(decisionColumn, node.attributeColums.filter { it.name != parentsUnwrap.name }, value)
                children.parent = node

                node.childrens.add(children)
            }
        }
    }

    private fun calculateAllDataPropability(column: DataColumn): Map<Int, Double> {
        val map = HashMap<Int, Double>()
        val valueList = column.columnValuesList.map { it.value.toString().toInt() }.distinct()

        for (value in valueList) {
            map.put(value,  column.columnValuesList.filter { it.value.toString().toInt() == value }.count().toDouble() / column.columnValuesList.count().toDouble())
        }

        return map
    }

    private fun calculatePropabilityForValues(values: List<Int>): Map<Int, Double> {
        val propabilityMap = HashMap<Int, Double>()
        val singleValues = values.distinct()

        for (value in singleValues) {
            propabilityMap.put(value, values.filter { it == value }.count().toDouble() / values.count().toDouble() )
        }

        return propabilityMap
    }

    private fun calculatePropabilityForAtribute(column: DataColumn, decisionColumn: DataColumn, attribute: Int): Map<Int, Double> {
        val propabilityMap = HashMap<Int, Double>()
        val decisioValues = decisionColumn.columnValuesList.map { it.value.toString().toInt() }.distinct()
        val columnValues = column.columnValuesList.map { it.value.toString().toInt() }.distinct()

        var selectedValues = ArrayList<Int>()

        column.columnValuesList.forEachIndexed { index, dataValue ->
            if(dataValue.value.toString().toInt() == attribute ) {
                selectedValues.add(decisionColumn.columnValuesList[index].value.toString().toInt())
            }
        }

        return calculatePropabilityForValues(selectedValues)
    }

    private fun calculateInformationGain(allDataEntropy: Double, attributeColumn: DataColumn, entropyValues: Map<Int, Double>): Double {
        var gainSum = 0.0

        for (dataValue in attributeColumn.columnValuesList.distinct()) {
            gainSum += (attributeColumn.columnValuesList.filter {
                            it.value.toString().toInt() == dataValue.value.toString().toInt()
                        }.count().toDouble() / attributeColumn.columnValuesList.count().toDouble()) * entropyValues.getValue(dataValue.value.toString().toInt())
        }

        return allDataEntropy - gainSum
    }

    private fun calculateEntropy(propabilityMap: Map<Int, Double>): Double {
        var entropy = 0.0

        for (key in propabilityMap.keys) {
            val propabilityValue = propabilityMap.getValue(key)

            entropy += - propabilityValue * log2(propabilityValue)
        }

        return entropy
    }

    fun printTree(node: TreeNode) {
        //int outputattr = numAttributes - 1;

        if (node.childrens.size == 0) {

//            int[] values = getAllValues(node.data, outputattr);
//            node.nodeValue
//            if (values.length == 1) {
//
//                System.out.println(tab + "\t" + attributeNames[outputattr] + " = \"" +
//
//                        domains[outputattr].elementAt(values[0]) + "\";");
//
//                return;
//
//            }
//
//            System.out.print(tab + "\t" + attributeNames[outputattr] + " = {");
//
//            for (int i = 0; i < values.length; i++) {
//
//                System.out.print("\"" + domains[outputattr].elementAt(values) + "\"");
//
//                if (i != values.length - 1) System.out.print(" , ");
//
//            }

            println("\t ${node.attributeName} == ${node.nodeValue} };")
            return
        }


        node.childrens.forEachIndexed { index, treeNode ->
            println("if( ${treeNode.attributeName} == ${treeNode.nodeValue} ) {" )
            printTree(treeNode)

            if (index != node.childrens.size - 1) {
                print("} else ")
            } else {
                println("}")
            }
        }
    }
}