package pl.swd.app.views.modals

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.InputMethodRequests
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import tornadofx.*

class DiscretizationValuesModal : Modal("Discretization table data") {
    companion object : KLogging()

    val columnNameList: ArrayList<String> by param()

    val columnChooserBox = hbox()
    val comboboxLabel = label()
    val comboBox = combobox<String>()

    val chooseSectionBox = hbox()
    val sectionLabel = label()
    val sectionField = textfield()

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

        sectionLabel.text = "Choose number of section "
        sectionField.alignment = Pos.BASELINE_CENTER

        chooseSectionBox.add(sectionLabel)
        chooseSectionBox.add(sectionField)
        chooseSectionBox.alignment = Pos.BASELINE_CENTER
        chooseSectionBox.padding = Insets(4.0, 4.0, 4.0, 4.0)

        root.center.apply {
            add(columnChooserBox)
            add(chooseSectionBox)
        }

    }

    private fun save() {
        val item = comboBox.selectionModel.selectedItem
        val sectionValue = sectionField.text.toIntOrNull()

        if (item.isNullOrEmpty())  return
        if (sectionValue == null) return
        if (sectionValue < 2) return

        status = ModalStatus.COMPLETED
        close()
    }

    fun getSelectedColumnName(): String {
        return comboBox.selectionModel.selectedItem
    }

    fun getSectionNumber(): Int {
        return sectionField.text.toInt()
    }
}