package pl.swd.app.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.models.DataColumn
import pl.swd.app.models.DataValue
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.ConvertValuesModal
import tornadofx.*

@Service
class ConvertValueService {

    @Autowired private lateinit var projectService: ProjectService

    fun showConvertDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColunList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<ConvertValuesModal>(params = mapOf(ConvertValuesModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                convertSelectedColumn(view.getSelectedValue(), selectedTabIndex)
            }
        }
    }

    fun convertSelectedColumn(columnName: String, tabIndex: Int) {
        if (!projectService.currentProject.value.isPresent) return

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        val datatable = spreadSheet.dataTable
        val newColumnName = columnName + "_convert"
        val columnIndex = datatable.getColumnIndexByName(columnName)
        val filteredColumnValues = datatable.columns[columnIndex.get()].columnValuesList.distinct()
        var newColumnValues: ArrayList<DataValue> = ArrayList()

        spreadSheet.dataTable.rows.forEach {
            val value = DataValue(filteredColumnValues.indexOf(it.rowValuesMap[columnName]).toString())

            newColumnValues.add(value)
            it.addValue(newColumnName, value)
        }

        spreadSheet.dataTable.columns.add(DataColumn(newColumnName, newColumnValues))
    }

    private fun generateColunList(tabIndex: Int): ArrayList<String> {
        if (!projectService.currentProject.value.isPresent) return ArrayList()

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        var columnNameList = ArrayList<String>()

        for(entry in spreadSheet.dataTable.rows.first().rowValuesMap) {
            val columnValue = entry.value.value as String

            if (columnValue.toDoubleOrNull() == null) {
                columnNameList.add(entry.key)
            }
        }

        return columnNameList
    }
}