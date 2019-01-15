package pl.swd.app.views.modals

import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import pl.swd.app.models.TreeUserSelectionModel
import tornadofx.*
import java.util.*

class DecisionTreeModal: Modal("Decision tree") {

    val columnNameList: ArrayList<String> by param()

    private var textfields: HashMap<String, TextField> = hashMapOf()
    private var knumField: TextField by singleAssign()

    val form = form()
    val mainBox = vbox()

    val decisionClassLabel = label()
    val decisionClassComboBox = combobox<String>()

    var objectFieldSet = fieldset("Decision classes")
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
        decisionClassLabel.text = "Decision class"
        decisionClassComboBox.items.addAll(columnNameList.sorted())

        mainBox.add(decisionClassLabel)
        mainBox.add(decisionClassComboBox)

        val kField = field("Number") {
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

    fun getTreeParameters(): TreeUserSelectionModel {
        val decisionClassColumn = decisionClassComboBox.selectionModel.selectedItem
        val knum = knumField.text.toInt()

        return TreeUserSelectionModel(decisionClassColumn, knum)
    }
}
