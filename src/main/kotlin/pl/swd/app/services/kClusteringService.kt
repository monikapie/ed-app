package pl.swd.app.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.models.*
import pl.swd.app.services.ClassifyData.ClassifyDataService
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.ClassifyDataModal
import pl.swd.app.views.modals.ConvertValuesModal
import pl.swd.app.views.modals.kClusteringModal
import tornadofx.find
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class kClusteringService {

    private val tag = "grupTag"

    @Autowired private lateinit var projectService: ProjectService
    @Autowired private lateinit var convertValueService: ConvertValueService
    @Autowired private lateinit var clasifiDataService: ClassifyDataService

    fun showDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColumnList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<kClusteringModal>(params = mapOf(ConvertValuesModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                clasifiData(view.getClassifySelectedData(), selectedTabIndex)
            }
        }
    }

    private fun clasifiData(userSelectedParameters: kClustering, tabIndex: Int) {
        val project = projectService.currentProject.value?.let { it } ?: return
        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        val rows = project.get().spreadSheetList[tabIndex].dataTable.rows.toList()
        val columns = project.get().spreadSheetList[tabIndex].dataTable.columns.toList()

        var newColumnValues: ArrayList<DataValue> = ArrayList()

        val a = clusterData(rows, userSelectedParameters)

        spreadSheet.dataTable.rows.forEachIndexed { i, r ->

            val value = a[i].rowValuesMap[tag]!!

            newColumnValues.add(value)
            r.addValue(tag, value)
        }

        indexes(newColumnValues, generateColumnList(tabIndex).filter { it != tag }.filter { !userSelectedParameters.decisionCols.contains(it) }.first(), columns, spreadSheet, rows, a)
        tablePrinter(newColumnValues, generateColumnList(tabIndex).filter { it != tag }.filter { !userSelectedParameters.decisionCols.contains(it) }.first(), columns, spreadSheet, rows, a)

        spreadSheet.dataTable.columns.add(DataColumn(tag, newColumnValues))
    }

    private fun generateColumnList(tabIndex: Int): ArrayList<String> {
        checkAndConvertValues(tabIndex)

        var columnNameList = ArrayList<String>()
        val project = projectService.currentProject.value?.let { it } ?: return columnNameList
        val rowValuesMap = project.get().spreadSheetList[tabIndex].dataTable.rows.first().rowValuesMap

        for(entry in rowValuesMap) {
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

        for(entry in rowValuesMap) {
            if (rowValuesMap.containsKey("${entry.key}_convert")) continue

            val columnValue = entry.value.value as String

            if (columnValue.toDoubleOrNull() == null) convertValueService.convertSelectedColumn(entry.key, tabIndex)
        }
    }

    fun initialClusteringCenters(data: List<DataRow>, conf: kClustering): java.util.ArrayList<DataRow> {
        val invCov =  clasifiDataService.inverseCovarianceMatrix(data, conf.decisionCols)

        //select centers with k-means++
        val randomDataIndex = Random().nextInt(data.size - 1)
        val first = data[randomDataIndex]
        first.addValue(tag, DataValue("0")) //Meyby not
        val C = arrayListOf(data.first())
        for (i in 1..conf.kNum-1) {
            var minDistance = 0.0
            var mostDistantObject: DataRow? = null
            var currentMaxDistance = 0.0
            for (row in data) {
                //do not check current centers
                //Cant contains rows :) so create hacks for this
                if (C.contains(row)) {
                    continue
                }

                //check lowest distance from current centers
                C.forEachIndexed { i, r ->
                    if (i == 0) {
                        minDistance = clasifiDataService.calculateDistance(conf.metric, r, row, conf.decisionCols, invCov)
                    } else {
                        val dist = clasifiDataService.calculateDistance(conf.metric, r, row, conf.decisionCols, invCov)
                        minDistance = Math.min(minDistance, dist)
                    }
                }

                if (minDistance > currentMaxDistance) {
                    mostDistantObject = row
                    currentMaxDistance = minDistance
                }
            }
            mostDistantObject!!.addValue(tag, DataValue(i.toString()))
            C.add(mostDistantObject!!)
        }
        return C
    }

    fun updatedClusteringCenters(data: List<DataRow>, conf: kClustering): java.util.ArrayList<DataRow> {
        val C: java.util.ArrayList<DataRow> = arrayListOf()

        for (i in 0..conf.kNum - 1) {

            val currentTagRows = data.filter {
                if (it.rowValuesMap[tag] == null) {
                    "0" == i.toString()
                } else {
                    it.rowValuesMap[tag]!!.value.toString() == i.toString()
                }

            }
            val rowMap = HashMap<String, DataValue>()
            val newCenter = DataRow("", rowMap)
            newCenter.addValue(tag, DataValue(i.toString()))

            conf.decisionCols.forEach {
                var sum = 0.0
                val col = it
                currentTagRows.forEach {
                    sum += it.rowValuesMap[col]!!.value.toString().toDouble()
                }
                val mean = sum / currentTagRows.count()
                newCenter.addValue(col, DataValue(mean))
            }
            C.add(newCenter)
        }

        return C
    }

    //k-means
    fun clusterData(data: List<DataRow>, conf: kClustering): List<DataRow> {

        val invCov = clasifiDataService.inverseCovarianceMatrix(data, conf.decisionCols)
        var C = initialClusteringCenters(data, conf)

        do {
            var sthHasChangedTag = false
            for (row in data) {
                //do not check current centers
                if (C.contains(row)) {
                    continue
                }
                var minDistance = 0.0
                var minDistanceObject = C.first()
                C.forEachIndexed { i, r ->
                    if (i == 0) {
                        minDistance = clasifiDataService.calculateDistance(conf.metric, r, row, conf.decisionCols, invCov)
                        minDistanceObject = r
                    } else {
                        val dist = clasifiDataService.calculateDistance(conf.metric, r, row, conf.decisionCols, invCov)
                        if (dist < minDistance) {
                            minDistance = dist
                            minDistanceObject = r
                        }
                    }
                }
                if(row.rowValuesMap[tag] == null) {
                    if (minDistanceObject.rowValuesMap[tag] == null) {
                        row.addValue(tag, DataValue("0"))
                        sthHasChangedTag = true
                    } else {
                        row.addValue(tag, DataValue(minDistanceObject.rowValuesMap[tag]!!.value ?: "0"))
                        sthHasChangedTag = true
                    }
                } else if (row.rowValuesMap[tag] == null || minDistanceObject.rowValuesMap[tag] == null) {
                    row.addValue(tag, DataValue("0"))
                    sthHasChangedTag = true
                } else {
                    if(row.rowValuesMap[tag]!!.value.toString() != minDistanceObject.rowValuesMap[tag]!!.value.toString()) {
                        row.addValue(tag, DataValue(minDistanceObject.rowValuesMap[tag]!!.value ?: "0"))
                        sthHasChangedTag = true
                    }
                }
            }

            if (!sthHasChangedTag) {
                break
            }
            C = updatedClusteringCenters(data, conf)

        } while (true)

        return data
    }

    private fun tablePrinter(newColumnValues: ArrayList<DataValue>, columnNameToCompare: String, columns: List<DataColumn>, spreadSheet: SpreadSheet, rows: List<DataRow>, newRows: List<DataRow>) {
        val clasifiValues = newColumnValues.distinct().map { it.value.toString().toInt() }
        val oldValues = columns[spreadSheet.dataTable.getColumnIndexByName(columnNameToCompare!!).get()].columnValuesList.distinct().map { it.value.toString().toInt() }

        var map = HashMap<Int, HashMap<Int, Int>>()

        for (value in oldValues) {
            var map1 = HashMap<Int, Int>()

            for (value1 in clasifiValues) {
                map1.put(value1, 0)
            }

            map.put(value, map1)
        }

        if (columnNameToCompare != null) {
            rows.forEachIndexed { index, dataRow ->
                var valueMap = map.get(dataRow.rowValuesMap[columnNameToCompare]!!.value.toString().toInt())!!
                var calsifiValue = valueMap[newRows[index].rowValuesMap[tag]!!.value.toString().toInt()]!! + 1
                valueMap.set(newRows[index].rowValuesMap[tag]!!.value.toString().toInt(), calsifiValue)
            }

            //PRINT
            print("\n")
            print("\t")
            for(value in clasifiValues.sorted()) {
                print("${value}\t")
            }
            print("\n")

            for (value in map.keys) {
                print("${value}\t")

                var mapp = map[value]!!

                for (value1 in mapp.keys) {
                    print("${mapp[value1]!!}\t")
                }
                print("\n")
            }
        }
    }

    private fun indexes(newColumnValues: ArrayList<DataValue>, columnNameToCompare: String, columns: List<DataColumn>, spreadSheet: SpreadSheet, rows: List<DataRow>, newRows: List<DataRow>) {
        val clasifiValues = newColumnValues.distinct().map { it.value.toString().toInt() }.sorted()
        val oldValues = columns[spreadSheet.dataTable.getColumnIndexByName(columnNameToCompare!!).get()].columnValuesList.distinct().map { it.value.toString().toInt() }

        var indexedMap = HashMap<Int, ArrayList<Int>>()

        val numberValuesForOneOldValue = clasifiValues.count()/oldValues.count()
        var a = 0

        for(i in oldValues){
            var list = ArrayList<Int>()
            for(index in a..clasifiValues.size) {
                if (index == a + numberValuesForOneOldValue) {
                    a += numberValuesForOneOldValue
                    break
                }
                list.add(clasifiValues[index])
            }
            indexedMap.put(i, list)
        }

        var equalElementsJacard = 0

        //JACARD
        if (columnNameToCompare != null) {
            rows.forEachIndexed { index, dataRow ->
                if(indexedMap[dataRow.rowValuesMap[columnNameToCompare]!!.value.toString().toInt()]!!.contains(newRows[index].rowValuesMap[tag]!!.value.toString().toInt())) {
                    equalElementsJacard += 1
                }
            }

            ProjectSaverService.logger.debug { "Jacard index: ${equalElementsJacard.toDouble()/(newRows.count().toDouble() + newRows.count().toDouble() - equalElementsJacard.toDouble())}" }
            ProjectSaverService.logger.debug { "Simple matching coefficient: ${equalElementsJacard.toDouble()/(newRows.count().toDouble())}" }
        }
    }
}