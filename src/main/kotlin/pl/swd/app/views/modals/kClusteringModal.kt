package pl.swd.app.views.modals

import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import pl.swd.app.models.kClustering
import pl.swd.app.services.ClassifyData.ClassifiDistanceMetric
import tornadofx.*
import java.util.HashMap

class kClusteringModal: Modal("k --- Data") {

    val columnNameList: ArrayList<String> by param()

    private var knumField: TextField by singleAssign()

    val form = form()
    val mainBox = vbox()

    val metricsListView = listview<String>()
    val metricsLabel = label()

    val decisionAttributesListView = listview<String>()
    val decisionAttributesLabel = label()

    var objectFieldSet = fieldset("Object")
    val bottomBar = buttonbar {
        button("Cancel") {
            shortcut(KeyCodeCombination(KeyCode.ESCAPE))
            action {
                close()
            }
        }

        button("Save") {
            shortcut(KeyCodeCombination(KeyCode.ENTER))
            action {
                save()
            }
        }
    }

    override val root = scrollpane()

    init {
        decisionAttributesListView.items.addAll(columnNameList)
        decisionAttributesListView.minWidth = 400.0
        decisionAttributesListView.maxHeight = 150.0
        decisionAttributesListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        decisionAttributesLabel.text = "Decision attributes"

        mainBox.add(decisionAttributesLabel)
        mainBox.add(decisionAttributesListView)

        metricsLabel.text = "Metric"
        metricsListView.items.add(ClassifiDistanceMetric.EUKLIDES.name)
        metricsListView.items.add(ClassifiDistanceMetric.MAHALANOBIS.name)
        metricsListView.items.add(ClassifiDistanceMetric.MANHATTAN.name)
        metricsListView.items.add(ClassifiDistanceMetric.INFINITY.name)

        metricsListView.minWidth = 400.0
        metricsListView.maxHeight = 150.0

        mainBox.add(metricsLabel)
        mainBox.add(metricsListView)

        val kField = field("k") {
            knumField = textfield()
        }

        objectFieldSet.add(kField)

        form.add(mainBox)
        form.add(objectFieldSet)
        form.add(bottomBar)

        root.apply {
            minWidth = 400.0
            minHeight = 400.0

            add(form)
        }
    }

    private fun save() {
        status = ModalStatus.COMPLETED
        close()
    }

    fun getClassifySelectedData(): kClustering {
        val decisionAttributesColumnList = decisionAttributesListView.selectionModel.selectedItems.toList()
        val metrics = ClassifiDistanceMetric.valueOf(metricsListView.selectedItem ?: "")
        val knum = knumField.text.toInt()


        return kClustering(decisionAttributesColumnList, knum, metrics)
    }
}