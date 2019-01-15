package pl.swd.app.views;

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import pl.swd.app.models.SpreadSheet
import tornadofx.*

class TabWrapper(val spreadSheet: SpreadSheet) : Tab() {
    /*Need to save the reference, so that a parent component can access it and add a listener to it*/
    var renameMenuItem: MenuItem by singleAssign()

    init {
        textProperty().bindBidirectional(spreadSheet.nameProperty)
        /*Here I create a TabContentFragment component and pass it a dataTable as a parameter*/
        content = vbox {
            this += find(TabContentFragment::class,
                    params = mapOf(TabContentFragment::dataTable to spreadSheet.dataTable))
        }

        renameMenuItem = MenuItem("Rename")
        contextMenu = ContextMenu(renameMenuItem)
    }
}
