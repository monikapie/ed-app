package pl.swd.app.views.modals

import com.github.thomasnield.rxkotlinfx.actionEvents
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.models.SpreadSheet
import pl.swd.app.services.DataFileParser.DataFileParserService
import pl.swd.app.services.FileIOService
import pl.swd.app.services.ProjectService
import pl.swd.app.utils.emptyObservableList
import tornadofx.*

class ExportSpreadSheetModal : Modal("Export Data") {
    companion object : KLogging()

    val projectService: ProjectService by di()
    val fileIOService: FileIOService by di()
    val dataFileParserService: DataFileParserService by di()
    val model = ExportSpreadSheetViewModel()

    override val root = form {
        fieldset("Choose a SpreadSheet") {
            field("SpreadSheet") {
                combobox<SpreadSheet>(property = model.selectedSpreadSheet) {
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

                    required()
                }
            }
        }
        fieldset("Export location") {
            field("Path") {
                textfield(model.saveFilePath) {
                    required()
                }
                button("Browse") {
                    this@button.actionEvents()
                            .flatMap {
                                fileIOService.openFileDialogObs(
                                        title = "Choose File",
                                        fileExtensions = fileIOService.csvFileExtension,
                                        mode = FileChooserMode.Save
                                ).take(1)
                            }
                            .subscribe { model.saveFilePath.value = it.absolutePath }
                }

            }
        }

        buttonbar {
            button("Cancel") {
                shortcut(KeyCodeCombination(KeyCode.ESCAPE))
                action { close() }
            }
            button("Export") {
                shortcut(KeyCodeCombination(KeyCode.ENTER))
                enableWhen { model.valid }
                action {
                    export()
                }
            }
        }
    }

    fun export() {
        val rawDataTable = dataFileParserService.parseDataTableToRawData(model.selectedSpreadSheet.value.dataTable)
        fileIOService.saveAsAnyToFile(rawDataTable, model.saveFilePath.value)

        model.commit()
        status = ModalStatus.COMPLETED
        close()
    }

    class ExportSpreadSheetViewModel : ViewModel() {
        val selectedSpreadSheet = bind { SimpleObjectProperty<SpreadSheet>() }
        val saveFilePath = bind { SimpleStringProperty() }
    }
}