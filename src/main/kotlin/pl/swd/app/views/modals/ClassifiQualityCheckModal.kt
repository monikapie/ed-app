package pl.swd.app.views.modals

import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import pl.swd.app.models.ClassifySelectedDataModel
import pl.swd.app.models.DataRow
import pl.swd.app.models.DataValue
import pl.swd.app.services.ClassifyData.ClassifiDistanceMetric
import tornadofx.*
import java.util.HashMap

class ClassifiQualityCheckModal: Modal("Classify Quality Data") {

    val columnNameList: ArrayList<String> by param()

    private var textfields: HashMap<String, TextField> = hashMapOf()
    private var knumField: TextField by singleAssign()

    val form = form()
    val mainBox = vbox()

    val metricsListView = listview<String>()
    val metricsLabel = label()

    val decisionClassLabel = label()
    val decisionClassComboBox = combobox<String>()

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
        decisionAttributesListView.items.addAll(columnNameList.sorted())
        decisionAttributesListView.minWidth = 400.0
        decisionAttributesListView.maxHeight = 150.0
        decisionAttributesListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        decisionAttributesLabel.text = "Decision attributes"

        mainBox.add(decisionAttributesLabel)
        mainBox.add(decisionAttributesListView)

        decisionClassLabel.text = "Decision class"
        decisionClassComboBox.items.addAll(columnNameList.sorted())

        mainBox.add(decisionClassLabel)
        mainBox.add(decisionClassComboBox)

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

    fun getClassifySelectedData(): ClassifySelectedDataModel {
        val decisionAttributesColumnList = decisionAttributesListView.selectionModel.selectedItems.toList()
        val decisionClassColumn = decisionClassComboBox.selectionModel.selectedItem
        val metrics = ClassifiDistanceMetric.valueOf(metricsListView.selectedItem ?: "")
        val knum = knumField.text.toInt()

        val rowMap = HashMap<String, DataValue>()

        for ((fieldName, field) in textfields) {
            rowMap.put(fieldName, DataValue(field.text))
        }

        return ClassifySelectedDataModel(decisionAttributesColumnList, decisionClassColumn, DataRow("", rowMap), knum, metrics)
    }
}