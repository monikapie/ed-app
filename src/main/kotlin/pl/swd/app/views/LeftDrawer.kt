package pl.swd.app.views;

import com.github.thomasnield.rxkotlinfx.onChangedObservable
import io.reactivex.rxkotlin.toObservable
import mu.KLogging
import pl.swd.app.models.SpreadSheet
import pl.swd.app.services.ProjectService
import pl.swd.app.utils.emptyObservableList
import tornadofx.*

class LeftDrawer : View("Drawer") {
    companion object : KLogging()

    val projectService: ProjectService by di()
    val tabsView: TabsView by inject()

    override val root = drawer {
        item("Project") {
            listview(emptyObservableList<SpreadSheet>()) {
                // todo add unsubscribe
                /*Subscribes to a current project and binds its spreadsheetsList to listview items*/
                projectService.currentProject
                        .map { currentProject ->
                            if (!currentProject.isPresent()) {
                                logger.debug { "No Current Project - setting empty SpreadSheets list" }
                                return@map emptyObservableList<SpreadSheet>()
                            }

                            logger.debug { "Found New Project '${currentProject.get().name}' - binding SpreadSheets list" }
                            return@map currentProject.get().spreadSheetList
                        }
                        /* Binding ObservableList of Spreadsheets to a drawer list */
                        .doOnNext { items = it }
                        /* When receiving a new project I need to clear tabs list */
                        // todo move it to tabsView?
                        .doOnNext { tabsView.clearTabs() }
                        /*Checking if a speadsheet should be opened in a tab*/
                        // todo: move it somewhere else?
                        // todo: onChangedObservable emits list of all speadsheets instead of only the added one
                        .switchMap { it.onChangedObservable() }
                        .doOnNext { println(it.size) }
                        .switchMap { it.toObservable() }
                        .subscribe {
                            if (it.autoOpenTabOnLoad) {
                                tabsView.addTab(it)
                            }
                        }

                /*Makes sure that whenever a spreadsheet name changes
                * it will properly reflect it in a listview*/
                cellFormat { spreadSheet ->
                    graphic = label(spreadSheet.nameProperty)
                }

                /* todo: should only trigger event on a double click on an item list
                * Current behavour: when item list is selected it doesn't matter where you double click*/
                onUserSelect { tabsView.addTab(it) }

                // todo add context menu on item right click. Add extension functions?
            }
        }
    }
}