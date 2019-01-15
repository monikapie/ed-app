package pl.swd.app.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.models.DataColumn
import pl.swd.app.models.DataValue
import pl.swd.app.views.TabsView
import pl.swd.app.views.modals.ConvertValuesModal
import pl.swd.app.views.modals.ProcentDataModal
import pl.swd.app.views.modals.SelectProcentDataModal
import tornadofx.find

@Service
class SelectProcentDataService {
    @Autowired private lateinit var projectService: ProjectService

    fun showDialog(tabsView: TabsView) {
        val selectedTabIndex = tabsView.root.selectionModel.selectedIndex

        if (selectedTabIndex != -1 && projectService.currentProject.value.isPresent) {
            val columnList = generateColunList(selectedTabIndex)

            if (columnList.isEmpty()) return

            val view = find<SelectProcentDataModal>(params = mapOf(ConvertValuesModal::columnNameList to columnList)).apply {
                openModal(block = true)
            }

            if (view.status.isCompleted()) {
                convertSelectedColumn(view.getSelectedColumnName(), view.getProcentValue() ,selectedTabIndex)
            }
        }
    }

    private fun convertSelectedColumn(columnName: String, procent: Double ,tabIndex: Int) {
        if (!projectService.currentProject.value.isPresent) return

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        val datatable = spreadSheet.dataTable
        val columnIndex = datatable.getColumnIndexByName(columnName)
        val filteredColumnValues = datatable.columns[columnIndex.get()].columnValuesList

        filteredColumnValues.sortBy { it.value.toString().toDouble() }

        val numberData = Math.round((filteredColumnValues.size.toDouble() * (procent/100.0)) / 2.0).toInt()

        showDataView(filteredColumnValues.take(numberData), filteredColumnValues.takeLast(numberData))
    }

    private fun showDataView(underProcentValues: List<DataValue>, aboveProcentValues: List<DataValue>) {
        find<ProcentDataModal>(params = mapOf(ProcentDataModal::valueMin to underProcentValues, ProcentDataModal::valueMax to aboveProcentValues)).apply {
            openModal(block = true)
        }
    }

    private fun generateColunList(tabIndex: Int): ArrayList<String> {
        if (!projectService.currentProject.value.isPresent) return ArrayList()

        val spreadSheet = projectService.currentProject.value.get().spreadSheetList[tabIndex]
        var columnNameList = ArrayList<String>()

        for(entry in spreadSheet.dataTable.rows.first().rowValuesMap) {
            val columnValue = entry.value.value as String

            if (columnValue.toDoubleOrNull() != null) {
                columnNameList.add(entry.key)
            }
        }

        return columnNameList
    }
}