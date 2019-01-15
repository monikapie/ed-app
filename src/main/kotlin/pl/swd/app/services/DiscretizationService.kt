package pl.swd.app.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.models.DataColumn
import pl.swd.app.models.DataValue
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.ConvertValuesModal
import pl.swd.app.views.modals.DiscretizationValuesModal
import tornadofx.find
import java.lang.Math.*

@Service
class DiscretizationService {

    @Autowired private lateinit var projectService: ProjectService

    fun showDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColunList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<DiscretizationValuesModal>(params = mapOf(ConvertValuesModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                convertSelectedColumn(view.getSelectedColumnName(), view.getSectionNumber(), selectedTabIndex)
            }
        }
    }

    fun convertSelectedColumn(columnName: String, sectionNumber: Int, tabIndex: Int) {
        if (!projectService.currentProject.value.isPresent) return

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        val datatable = spreadSheet.dataTable
        val newColumnName = columnName + "_discrete"
        val columnIndex = datatable.getColumnIndexByName(columnName)

        var maxColumnValue = datatable.columns[columnIndex.get()].columnValuesList.maxBy { dataValue -> dataValue.value.toString().toDouble() }?.let { it.value.toString().toDouble() }
        var minColumnValue = datatable.columns[columnIndex.get()].columnValuesList.minBy { dataValue -> dataValue.value.toString().toDouble() }?.let { it.value.toString().toDouble() }
        var sectionDiff = maxColumnValue!! - minColumnValue!!
        var sectionValue = sectionDiff / sectionNumber
        var newColumnValues: ArrayList<DataValue> = ArrayList()

        maxColumnValue = round(maxColumnValue * 100.0) / 100.0
        minColumnValue = round(minColumnValue * 100.0) / 100.0
        sectionValue = round(sectionValue * 100.0) / 100.0

        var sectionList: ArrayList<Double> = ArrayList()
        var value = minColumnValue

        sectionList.add(minColumnValue)

        while(value < maxColumnValue) {
            sectionList.add(round((value + sectionValue) * 100.0) / 100.0)
            value += sectionValue
        }

        spreadSheet.dataTable.rows.forEach {
            var columnValue = round(it.rowValuesMap[columnName]?.value.toString().toDouble() * 100.0) / 100.0

            for (i in sectionList.indices) {
                if (sectionList.size - 1 != i) {
                    //IN RANGE
                    if (columnValue == columnValue.coerceIn(sectionList[i], sectionList[i+1])) {
                        val dataValue = DataValue(i.toString())

                        newColumnValues.add(dataValue)
                        it.addValue(newColumnName, dataValue)
                        break
                    }
                }
            }
        }

        //Todo REMOVE OLD COLUMN?
//        spreadSheet.dataTable.columns.removeIf {
//            it.name == newColumnName
//        }

        spreadSheet.dataTable.columns.add(DataColumn(newColumnName, newColumnValues))
    }

    private fun generateColunList(tabIndex: Int): ArrayList<String> {
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
}