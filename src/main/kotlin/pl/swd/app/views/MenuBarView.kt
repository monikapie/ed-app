package pl.swd.app.views;

import com.github.thomasnield.rxkotlinfx.actionEvents
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import mu.KLogging
import pl.swd.app.exceptions.ProjectDoesNotExistException
import pl.swd.app.models.SpreadSheet
import pl.swd.app.services.*
import pl.swd.app.services.ClassifyData.ClassifyDataService
import pl.swd.app.services.DataFileParser.DataFileOption
import pl.swd.app.services.DataFileParser.DataFileParserService
import pl.swd.app.services.DecisionTree.TreeService
import pl.swd.app.views.modals.Chart2DConfigModal
import pl.swd.app.views.modals.ExportSpreadSheetModal
import pl.swd.app.views.modals.ParseDataFileOptionsModal
import pl.swd.app.views.modals.SpaceDividerConfigModal
import tornadofx.*
import java.io.File

class MenuBarView : View("My View") {
    companion object : KLogging()

    val projectService: ProjectService by di()
    val projectSaverService: ProjectSaverService by di()
    val fileIOService: FileIOService by di()
    val dataFileParserService: DataFileParserService by di()
    val convertValueService: ConvertValueService by di()
    val discretizationService: DiscretizationService by di()
    val normalizationService: NormalizationService by di()
    val changeSectionService: ChangeSectionService by di()
    val selectProcentDataService: SelectProcentDataService by di()
    val classifyDataService: ClassifyDataService by di()
    val kClusteringService: kClusteringService by di()
    val treeService: TreeService by di()
    val tabsView: TabsView by inject()

    override val root = menubar {
        menu("File") {
            item("Open...", KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)) {
                actionEvents()
                        .subscribe { projectSaverService.loadFromFile(askUserForPath = true) }
            }

            item("Save", KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)) {
                actionEvents()
                        .subscribe { projectSaverService.saveToFile() }
            }

            item("Save As", KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN)) {
                actionEvents()
                        .subscribe { projectSaverService.saveToFile(askUserForPath = true) }
            }

            separator()

            item("Import Data", KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN)) {
                actionEvents()
                        .doOnNext { logger.debug { "'Import Data' menu item clicked" } }
                        .flatMap { fileIOService.openFileDialogObs() }
                        .map { file ->
                            val optionsView = find(ParseDataFileOptionsModal::class).apply { openModal(block = true) }

                            if (optionsView.cancelFlag) {
                                return@map
                            }

                            registerSpreadSheet(file, optionsView.getResultList().get(), optionsView.getOption())
                        }
                        .subscribe { logger.debug { "Registered new SpreadSheet: ${it}" } }
            }
            item("Export Data", KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)) {
                actionEvents()
                        .doOnNext { logger.debug { "'Export Data' menu item clicked" } }
                        .map {
                            find(ExportSpreadSheetModal::class).also {
                                it.openModal()
                            }
                        }
                        .subscribe()

            }
        }

        menu("Project") {
            item("Rename") {
                actionEvents()
                        .subscribe { projectService.renameProject() }
            }
        }

        menu("Data") {
            item("Convert values") {
                actionEvents()
                        .subscribe { convertValueService.showConvertDialog(tabsView) }
            }

            item("Discretization") {
                actionEvents()
                        .subscribe { discretizationService.showDialog(tabsView) }
            }

            item("Normalization") {
                actionEvents()
                        .subscribe { normalizationService.showDialog(tabsView) }
            }

            item("Change interval") {
                actionEvents()
                        .subscribe { changeSectionService.showDialog(tabsView) }
            }

            item("Select procent") {
                actionEvents()
                        .subscribe { selectProcentDataService.showDialog(tabsView) }
            }

            item("Classify data") {
                actionEvents()
                        .subscribe { classifyDataService.showDialog(tabsView) }
            }

            item("Classify Quantiti") {
                actionEvents()
                        .subscribe { classifyDataService.showQualityDialog(tabsView) }
            }

            item("k clustering") {
                actionEvents()
                        .subscribe { kClusteringService.showDialog(tabsView) }
            }

            item("decision tree") {
                actionEvents()
                        .subscribe { treeService.showDialog(tabsView) }
            }

            item("Space divider") {
                actionEvents()
                        .subscribe { find(SpaceDividerConfigModal::class).openWindow() }
            }
        }

        menu("Charts") {
            item("2D Chart") {
                actionEvents()
                        .subscribe { find(Chart2DConfigModal::class).openWindow() }
            }
        }
    }

    /**
     * Registers new SpreadSheet to currentProject
     *
     * @return spreadSheet name
     */
    fun registerSpreadSheet(file: File, columnNames: List<String>, options: DataFileOption): String {
        val spreadSheetName = file.name

        projectService.currentProject.value.apply {
            /*Can only register a spreadsheet when currentProject exists*/
            if (!isPresent()) {
                throw ProjectDoesNotExistException("Cannot register spreadsheet, because Project does not exist")
            }

            val dataTable = dataFileParserService.generateDataTable(
                    rows = file.readLines(),
                    columnNames = columnNames,
                    options = options)

            val spreadSheet = SpreadSheet(
                    name = spreadSheetName,
                    dataTable = dataTable
            )

            get().addSpreadSheet(spreadSheet)
        }

        return spreadSheetName
    }
}
