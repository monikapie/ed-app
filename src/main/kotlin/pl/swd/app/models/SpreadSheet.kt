package pl.swd.app.models;

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SpreadSheet(name: String,
                  val dataTable: DataTable,
                  autoOpenTabOnLoad: Boolean = true) {
    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    // todo: add a checkbox in a contextmenu of a project drawer to manipulate this flag?
    val autoOpenTabOnLoadProperty = SimpleBooleanProperty(autoOpenTabOnLoad)
    var autoOpenTabOnLoad by autoOpenTabOnLoadProperty
}
