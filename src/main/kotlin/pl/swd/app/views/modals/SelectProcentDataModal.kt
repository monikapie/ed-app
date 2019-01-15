package pl.swd.app.views.modals

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import tornadofx.*

class SelectProcentDataModal : Modal("Procent table data") {
    companion object : KLogging()

    val columnNameList: ArrayList<String> by param()

    val columnChooserBox = hbox()
    val comboboxLabel = label()
    val comboBox = combobox<String>()

    val choosProcentBox = hbox()
    val procentLabel = label()
    val procentField = textfield()

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

        procentLabel.text = "Choose procent of data "
        procentField.alignment = Pos.BASELINE_CENTER

        choosProcentBox.add(procentLabel)
        choosProcentBox.add(procentField)
        choosProcentBox.alignment = Pos.BASELINE_CENTER
        choosProcentBox.padding = Insets(4.0, 4.0, 4.0, 4.0)

        root.center.apply {
            add(columnChooserBox)
            add(choosProcentBox)
        }

    }

    private fun save() {
        val item = comboBox.selectionModel.selectedItem
        val sectionValue = procentField.text.toIntOrNull()

        if (item.isNullOrEmpty())  return
        if (sectionValue == null) return
        if (sectionValue <= 0 || sectionValue > 100) return

        status = ModalStatus.COMPLETED
        close()
    }

    fun getSelectedColumnName(): String {
        return comboBox.selectionModel.selectedItem
    }

    fun getProcentValue(): Double {
        return procentField.text.toDouble()
    }
}