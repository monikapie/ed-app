package pl.swd.app.views.modals

import javafx.geometry.Pos
import mu.KLogging
import pl.swd.app.models.DataValue
import tornadofx.borderpane
import tornadofx.center
import tornadofx.vbox
import tornadofx.*

class ProcentDataModal : Modal("Procent table data") {
    companion object : KLogging()

    val valueMin: List<DataValue> by param()
    val valueMax: List<DataValue> by param()

    val minListView = listview<String>()
    val maxListView = listview<String>()
    val listBox = hbox()
    val minLabel = label()
    val maxLabel = label()
    val labelBox = hbox()
    val centerBox = vbox()

    override val root = borderpane {
        center {
            vbox {}
        }
    }

    init {
        minListView.items.addAll(valueMin.map { it.value.toString() })
        maxListView.items.addAll(valueMax.map { it.value.toString() })
        minLabel.text = "Minimum values"
        maxLabel.text = "Maximum values"

        labelBox.add(minLabel)
        labelBox.add(maxLabel)
        labelBox.alignment = Pos.BASELINE_CENTER

        listBox.add(minListView)
        listBox.add(maxListView)

        centerBox.add(labelBox)
        centerBox.add(listBox)

        root.center.apply {
            add(centerBox)
        }
    }
}