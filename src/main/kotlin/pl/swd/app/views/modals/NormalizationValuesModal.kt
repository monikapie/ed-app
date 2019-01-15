package pl.swd.app.views.modals

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import tornadofx.*

class NormalizationValuesModal : Modal("Normalize table data") {
    companion object : KLogging()

    val columnNameList: ArrayList<String> by param()

    val columnChooserBox = hbox()
    val comboboxLabel = label()
    val comboBox = combobox<String>()

    override val root = borderpane {
        center {
            vbox {}
        }

        bottom {
            buttonbar {
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

            padding = Insets(4.0, 4.0, 4.0, 4.0)
        }
    }

    init {

        comboboxLabel.text = "Choose table column "
        comboBox.items.addAll(columnNameList)

        columnChooserBox.add(comboboxLabel)
        columnChooserBox.add(comboBox)
        columnChooserBox.alignment = Pos.BASELINE_CENTER
        columnChooserBox.padding = Insets(4.0, 4.0, 4.0, 4.0)

        root.center.apply {
            add(columnChooserBox)
        }
    }

    private fun save() {
        if (comboBox.selectionModel.selectedItem.isNullOrEmpty()) {
            close()
        } else {
            status = ModalStatus.COMPLETED
            close()
        }
    }

    fun getSelectedColumnName(): String {
        return comboBox.selectionModel.selectedItem
    }
}