package pl.swd.app.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.models.DataColumn
import pl.swd.app.models.DataValue
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.ConvertValuesModal
import pl.swd.app.views.modals.DiscretizationValuesModal
import pl.swd.app.views.modals.NormalizationValuesModal
import tornadofx.find

@Service
class NormalizationService {

    @Autowired private lateinit var projectService: ProjectService

    fun showDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColunList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<NormalizationValuesModal>(params = mapOf(ConvertValuesModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                convertSelectedColumn(view.getSelectedColumnName(), selectedTabIndex)
            }
        }
    }

    private fun convertSelectedColumn(columnName: String, tabIndex: Int) {
        if (!projectService.currentProject.value.isPresent) return

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        val datatable = spreadSheet.dataTable
        val newColumnName = columnName + "_normalize"
        val columnIndex = datatable.getColumnIndexByName(columnName)
        val dataSize = spreadSheet.dataTable.columns[columnIndex.get()].columnValuesList.size
        var newColumnValues: ArrayList<DataValue> = ArrayList()

        var avg =  (1.0 / dataSize) * spreadSheet.dataTable.columns[columnIndex.get()].columnValuesList.sumByDouble { it.value.toString().toDouble() }
        var standardDeviation = Math.sqrt((1.0/(dataSize.toDouble() - 1.0)) * spreadSheet.dataTable.columns[columnIndex.get()].columnValuesList.sumByDouble { Math.pow(it.value.toString().toDouble() - avg , 2.0) })

        spreadSheet.dataTable.rows.forEach {
            var columnValue = it.rowValuesMap[columnName]?.value.toString().toDouble()
            val currentValue = Math.round(((columnValue - avg) / standardDeviation) * 100.0) / 100.0
            val dataValue = DataValue(currentValue.toString())

            newColumnValues.add(dataValue)
            it.addValue(newColumnName, dataValue)
        }

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