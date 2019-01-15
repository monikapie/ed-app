package pl.swd.app.views.modals

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import tornadofx.*

class ChangeSectionModal : Modal("Change section table data") {
    companion object : KLogging()

    val columnNameList: ArrayList<String> by param()

    val columnChooserBox = hbox()
    val comboboxLabel = label()
    val comboBox = combobox<String>()

    val chooseFirstSectionBox = hbox()
    val sectionFirstLabel = label()
    val sectionFirstField = textfield()

    val chooseSecondSectionBox = hbox()
    val sectionSecondLabel = label()
    val sectionSecondField = textfield()

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

        sectionFirstLabel.text = "Choose new minimum "
        sectionFirstField.alignment = Pos.BASELINE_CENTER

        chooseFirstSectionBox.add(sectionFirstLabel)
        chooseFirstSectionBox.add(sectionFirstField)
        chooseFirstSectionBox.alignment = Pos.BASELINE_CENTER
        chooseFirstSectionBox.padding = Insets(4.0, 4.0, 4.0, 4.0)

        sectionSecondLabel.text = "Choose new maximum "
        sectionSecondField.alignment = Pos.BASELINE_CENTER

        chooseSecondSectionBox.add(sectionSecondLabel)
        chooseSecondSectionBox.add(sectionSecondField)
        chooseSecondSectionBox.alignment = Pos.BASELINE_CENTER
        chooseSecondSectionBox.padding = Insets(4.0, 4.0, 4.0, 4.0)

        root.center.apply {
            add(columnChooserBox)
            add(chooseFirstSectionBox)
            add(chooseSecondSectionBox)
        }

    }

    private fun save() {
        val item = comboBox.selectionModel.selectedItem
        val sectionMinValue = sectionFirstField.text.toDoubleOrNull()
        val sectionMaxValue = sectionSecondField.text.toDoubleOrNull()

        if (item.isNullOrEmpty())  return
        if (sectionMinValue == null) return
        if (sectionMaxValue == null) return

        if (sectionMinValue >= sectionMaxValue) return

        status = ModalStatus.COMPLETED
        close()
    }

    fun getSelectedColumnName(): String {
        return comboBox.selectionModel.selectedItem
    }

    fun getSectionMinValue(): Double {
        return sectionFirstField.text.toDouble()
    }

    fun getSectionMaxValue(): Double {
        return sectionSecondField.text.toDouble()
    }
}