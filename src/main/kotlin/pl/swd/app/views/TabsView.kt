package pl.swd.app.views;

import javafx.scene.control.TabPane
import mu.KLogging
import pl.swd.app.models.SpreadSheet
import pl.swd.app.views.modals.RenameSpreadSheetModal
import tornadofx.*

class TabsView : View("My View") {
    companion object : KLogging()

    override val root = TabPane()

    fun addTab(spreadSheet: SpreadSheet) {
        if (root.tabs.any { (it as TabWrapper).spreadSheet === spreadSheet }) {
            logger.debug { "Spreadsheet '${spreadSheet.name}' already exists as a Tab" }
            return
        }

        val tabWrapper = TabWrapper(spreadSheet)
        root.tabs.add(tabWrapper)
        /*Select a newly added tabWrapper*/
        root.selectionModel.select(tabWrapper)

        /*When clicking a "Rename" in a context menu it opens a RenameModal
        * And passes a spreadSheet to it*/
        tabWrapper.renameMenuItem.setOnAction {
            find(RenameSpreadSheetModal::class, mapOf(RenameSpreadSheetModal::spreadSheet to spreadSheet))
                    .openModal()
        }

        /*When closing a tab it should set a flag not to auto open it anymore*/
        tabWrapper.setOnClosed { tabWrapper.spreadSheet.autoOpenTabOnLoad = false }
    }

    fun clearTabs() {
        root.tabs.clear()
    }
}
