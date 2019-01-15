package pl.swd.app.views.modals

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.models.DataColumn
import pl.swd.app.models.SpreadSheet
import pl.swd.app.services.ProjectService
import pl.swd.app.services.SpaceDivider.SpaceDividerPoint
import pl.swd.app.services.SpaceDivider.SpaceDividerService
import pl.swd.app.utils.emptyObservableList
import tornadofx.*

class SpaceDividerConfigModal : Modal("Space Divider Config") {
    companion object : KLogging()

    val projectService: ProjectService by di()
    val spaceDividerService: SpaceDividerService by di()
    val selectedSpreadSheet = SimpleObjectProperty<SpreadSheet>()
    val model = SpaceDividerConfigViewModel()

    override val root = form {
        fieldset("Choose a SpreadSheet") {
            field("SpreadSheet") {
                combobox<SpreadSheet>(property = selectedSpreadSheet) {
                    projectService.currentProject
                            .map {
                                if (!it.isPresent()) {
                                    return@map emptyObservableList<SpreadSheet>()
                                }

                                return@map it.get().spreadSheetList
                            }
                            .doOnNext { Chart2DConfigModal.logger.debug { "Bound ${it.size} SpreadSheets" } }
                            .subscribe { items = it }

                    cellFormat { spreadSheet: SpreadSheet ->
                        this@cellFormat.text = spreadSheet.name
                    }
                }
            }
        }

        separator()

        fieldset("Choose Columns to find CutLines (only numeric values)") {
            listview(emptyObservableList<DataColumn>()) {
                selectionModel.selectionMode = SelectionMode.MULTIPLE
                model.selectedColumns.setValue(selectionModel.selectedItems)

                selectedSpreadSheet.onChange { spreadSheet ->
                    spreadSheet?.dataTable?.columns?.let { columns ->
                        items = columns
                    }
                }

                prefHeight = 200.0

                cellFormat { column ->
                    this@cellFormat.text = column.name
                }
            }
        }
        fieldset("Choose decision class column") {
            combobox(property = model.selectedDecisionClassColumn) {
                selectedSpreadSheet
                        .toObservable()
                        .map { it.dataTable.columns }
                        .doOnNext { Chart2DConfigModal.logger.debug { "Bound ${it.size} SpreadSheet columns to Decision Class dropdown" } }
                        .subscribe { items = it }

                cellFormat { dataColumn: DataColumn ->
                    this@cellFormat.text = dataColumn.name
                }

                required()
            }
        }
        checkbox("Show 2D chart (only when 2 columns are selected)", model.chart2DCheckbox) {
            enableWhen { model.selectedColumns.value.sizeProperty.eq(2) }
        }


        buttonbar {
            button("Cancel") {
                action { close() }
            }
            button("Start") {
                shortcut(KeyCodeCombination(KeyCode.ENTER))
                enableWhen(model.valid)
                action {
                    val pointsList = generateSpaceDivierPointsList()
                    val axisesNames = getSelectedAxisesNames()
                    find(SpaceDividerResultsModal::class, mapOf(
                            SpaceDividerResultsModal::pointsList to pointsList,
                            SpaceDividerResultsModal::axisesNames to axisesNames,
                            SpaceDividerResultsModal::showChart to (model.chart2DCheckbox.value && model.selectedColumns.value.size == 2)
                    )).openWindow()
                    close()
                }
            }
        }
    }

    private fun getSelectedAxisesNames(): List<String> {
        return model.selectedColumns.value
                .map { it.name }
    }

    private fun generateSpaceDivierPointsList(): List<SpaceDividerPoint> {
        val decisionClassColumn = model.selectedDecisionClassColumn.value
        val axisColumns = model.selectedColumns.value

        val pointsList = ArrayList<SpaceDividerPoint>(decisionClassColumn.columnValuesList.size)
        for (i in 0..decisionClassColumn.columnValuesList.lastIndex) {
            val axisesValues = Array(axisColumns.size) {
                (axisColumns[it].columnValuesList[i].value as String).toFloat()
            }

            pointsList.add(i, SpaceDividerPoint(
                    axisesValues = axisesValues,
                    decisionClass = (decisionClassColumn.columnValuesList[i].value as String)
            ))
        }

        return pointsList
    }

    class SpaceDividerConfigViewModel : ViewModel() {
        val selectedColumns = bind { SimpleListProperty<DataColumn>() }
        val selectedDecisionClassColumn = bind { SimpleObjectProperty<DataColumn>() }
        val chart2DCheckbox = bind { SimpleBooleanProperty() }
    }
}