package pl.swd.app.models

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import pl.swd.app.utils.emptyObservableList
import tornadofx.*

class Project(name: String,
              saveFilePath: String = "",
              val spreadSheetList: ObservableList<SpreadSheet> = emptyObservableList()) {
    /* Name of the project*/
    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    /* File, which this project was saved to */
    val saveFilePathProperty = SimpleStringProperty(saveFilePath)
    var saveFilePath by saveFilePathProperty

    fun addSpreadSheet(spreadSheet: SpreadSheet) {
        spreadSheetList.add(spreadSheet)
    }
}