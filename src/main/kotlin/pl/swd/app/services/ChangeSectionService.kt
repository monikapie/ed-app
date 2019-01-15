package pl.swd.app.services

import javafx.scene.input.DataFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.models.DataColumn
import pl.swd.app.models.DataValue
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.ChangeSectionModal
import pl.swd.app.views.modals.ConvertValuesModal
import tornadofx.find
import tornadofx.getProperty

@Service
class ChangeSectionService {

    @Autowired private lateinit var projectService: ProjectService

    fun showDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColunList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<ChangeSectionModal>(params = mapOf(ConvertValuesModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                convertSelectedColumn(view.getSelectedColumnName(), view.getSectionMinValue(), view.getSectionMaxValue(), selectedTabIndex)
            }
        }
    }

    private fun convertSelectedColumn(columnName: String, newMin: Double, newMax: Double, tabIndex: Int) {
        if (!projectService.currentProject.value.isPresent) return

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        val datatable = spreadSheet.dataTable
        val newColumnName = columnName + "_newInterval"
        val columnIndex = datatable.getColumnIndexByName(columnName)
        var newColumnValues: ArrayList<DataValue> = ArrayList()

        var maxColumnValue = datatable.columns[columnIndex.get()].columnValuesList.maxBy { dataValue -> dataValue.value.toString().toDouble() }?.let { it.value.toString().toDouble() }
        var minColumnValue = datatable.columns[columnIndex.get()].columnValuesList.minBy { dataValue -> dataValue.value.toString().toDouble() }?.let { it.value.toString().toDouble() }

        spreadSheet.dataTable.rows.forEach {
            var columnValue = it.rowValuesMap[columnName]?.value.toString().toDouble()

            var stdValue = (columnValue - minColumnValue!!)/(maxColumnValue!! - minColumnValue)
            var scaledValue = (newMax - newMin) * stdValue + newMin

            val dataValue = DataValue(scaledValue.toString())

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