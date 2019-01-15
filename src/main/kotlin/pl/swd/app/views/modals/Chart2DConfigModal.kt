package pl.swd.app.views.modals

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.models.*
import pl.swd.app.services.ProjectService
import pl.swd.app.utils.emptyObservableList
import tornadofx.*

class Chart2DConfigModal : Modal("2D Chart") {
    companion object : KLogging()

    val projectService: ProjectService by di()
    val selectedSpreadSheet = SimpleObjectProperty<SpreadSheet>()
    val model = Chart2DConfigViewModel()

    override val root = form {
        fieldset("Choose a SpreadSheet to build a chart from") {
            field("SpreadSheet") {
                combobox<SpreadSheet>(property = selectedSpreadSheet) {
                    projectService.currentProject
                            .map {
                                if (!it.isPresent()) {
                                    return@map emptyObservableList<SpreadSheet>()
                                }

                                return@map it.get().spreadSheetList
                            }
                            .doOnNext { logger.debug { "Bound ${it.size} SpreadSheets" } }
                            .subscribe { items = it }

                    cellFormat { spreadSheet: SpreadSheet ->
                        this@cellFormat.text = spreadSheet.name
                    }
                }
            }
        }

        separator()

        fieldset {
            enableWhen(selectedSpreadSheet.isNotNull)
            fieldset("Choose Columns to assign to axises") {
                fieldset {
                    field("X Axis") {
                        combobox<DataColumn>(property = model.xAxisColumn) {
                            selectedSpreadSheet
                                    .toObservable()
                                    .map { it.dataTable.columns }
                                    .doOnNext { logger.debug { "Bound ${it.size} SpreadSheet columns to X Axis" } }
                                    .subscribe { items = it }

                            cellFormat { dataColumn: DataColumn ->
                                this@cellFormat.text = dataColumn.name
                            }

                            required()
                        }
                    }
                    field("Data Type") {
                        combobox<Chart2dAxisType>(property = model.xAxisType) {
                            items.addAll(Chart2dAxisType.values())

                            cellFormat { chart2dAxisType: Chart2dAxisType ->
                                this@cellFormat.text = chart2dAxisType.label
                            }

                            required()
                        }
                    }
                }

                fieldset {
                    field("Y Axis") {
                        combobox<DataColumn>(property = model.yAxisColumn) {
                            selectedSpreadSheet
                                    .toObservable()
                                    .map { it.dataTable.columns }
                                    .doOnNext { logger.debug { "Bound ${it.size} SpreadSheet columns to Y Axis" } }
                                    .subscribe { items = it }

                            cellFormat { dataColumn: DataColumn ->
                                this@cellFormat.text = dataColumn.name
                            }

                            required()
                        }
                    }
                    field("Data Type") {
                        combobox<Chart2dAxisType>(property = model.yAxisType) {
                            items.addAll(Chart2dAxisType.values())

                            cellFormat { chart2dAxisType: Chart2dAxisType ->
                                this@cellFormat.text = chart2dAxisType.label
                            }

                            required()
                        }
                    }
                }
            }
        }

        separator()

        fieldset {
            enableWhen(selectedSpreadSheet.isNotNull)
            fieldset("Choose data class") {
                field("Data class") {
                    combobox<DataColumn>(property = model.dataClass) {
                        selectedSpreadSheet
                                .toObservable()
                                .map { it.dataTable.columns }
                                .doOnNext { logger.debug { "Bound ${it.size} SpreadSheet columns to data class" } }
                                .subscribe { items = it }

                        cellFormat { dataColumn: DataColumn ->
                            this@cellFormat.text = dataColumn.name
                        }

                        required()
                    }
                }
            }
        }

        buttonbar {
            button("Cancel") {
                action { close() }
            }
            button("Create") {
                shortcut(KeyCodeCombination(KeyCode.ENTER))
                enableWhen(model.valid)
                action { generateChart() }
            }
        }

    }

    init {
        /* Whenever a selectedSpreadSheet changes we want to clear the rest of the form, because x and y axis tables might not match */
        selectedSpreadSheet
                .toObservable()
                .subscribe {
                    if (selectedSpreadSheet.value !== null) {
                        logger.debug { "Clearing ViewModel, because a new SpreadSheed is selected: '${it.name}'" }
                        model.rollback()
                    }
                }


        /* if selelectedSpreadSheet was passed as a parameter, then I set it as a selected spreadsheet */
        params["selectedSpreadSheet"].apply {
            if (this === null) {
                return@apply
            }
            selectedSpreadSheet.set(this as SpreadSheet)
        }
    }

    private fun generateChart() {
        val chartData = Chart2dData(
                title = "Graph",
                xAxis = Chart2dAxis(
                        title = model.xAxisColumn.getValue().name
                ),
                yAxis = Chart2dAxis(
                        title = model.yAxisColumn.getValue().name
                ),
                series = parseDataValuesListToStringList(model.dataClass.value.columnValuesList)
        )

        when (model.xAxisType.value!!) {
            Chart2dAxisType.NUMERIC -> chartData.xAxis.numberValues =
                    parseDataValuesListToNumbersList(model.xAxisColumn.value.columnValuesList)
            Chart2dAxisType.STRING -> chartData.xAxis.stringValues =
                    parseDataValuesListToStringList(model.xAxisColumn.value.columnValuesList)
        }

        when (model.yAxisType.value!!) {
            Chart2dAxisType.NUMERIC -> chartData.yAxis.numberValues =
                    parseDataValuesListToNumbersList(model.yAxisColumn.value.columnValuesList)
            Chart2dAxisType.STRING -> chartData.yAxis.stringValues =
                    parseDataValuesListToStringList(model.yAxisColumn.value.columnValuesList)
        }

        find(Chart2DModal::class, mapOf(
                Chart2DModal::chart2dData to chartData
        )).openWindow()
    }

    private fun parseDataValuesListToNumbersList(dataValues: List<DataValue>): List<Number> {
        return dataValues
                .map {
                    try {
                        it.value as Number
                    } catch (e: Exception) {
                        (it.value as String).toFloat()
                    }
                }
    }

    private fun parseDataValuesListToStringList(dataValues: List<DataValue>): List<String> {
        return dataValues
                .map { it.value.toString() }
    }


    class Chart2DConfigViewModel : ViewModel() {
        val xAxisColumn = bind { SimpleObjectProperty<DataColumn>() }
        val xAxisType = bind { SimpleObjectProperty<Chart2dAxisType>() }
        val yAxisColumn = bind { SimpleObjectProperty<DataColumn>() }
        val yAxisType = bind { SimpleObjectProperty<Chart2dAxisType>() }
        val dataClass = bind { SimpleObjectProperty<DataColumn>() }
    }
}

