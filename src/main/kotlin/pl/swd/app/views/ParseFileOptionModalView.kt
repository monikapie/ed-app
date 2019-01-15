package pl.swd.app.views

import javafx.geometry.Insets
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.interfaces.GetResultFragment
import pl.swd.app.utils.asOptional
import tornadofx.*
import java.util.*
import javax.swing.GroupLayout

class ParseFileOptionModalView: Fragment("Rename Column"), GetResultFragment<String> {
    companion object : KLogging()

    override var cancelFlag: Boolean = false

    private val buttonBox = hbox()
    private val manualView = vbox()
    private val columnListView = listview<String> {
        selectionModel.selectionMode = SelectionMode.SINGLE
    }

    override val root = borderpane {
        center {
            vbox {
                togglegroup {
                    radiobutton {
                        text = "Auto detect columns name"

                        action {
                            columnListView.items.removeAll()
                            manualView.hide()
                            currentStage!!.width = 300.0
                            currentStage!!.height = 150.0
                        }
                    }.isSelected = true

                    radiobutton {
                        text = "Add manual colums name"

                        action {
                            manualView.show()
                            currentStage!!.width = 300.0
                            currentStage!!.height = 300.0
                        }
                    }

                    spacing = 2.0
                }

                spacing = 10.0
            }.add(manualView)

            padding = Insets(4.0,4.0,4.0,4.0)
        }

        bottom {
            hbox {
                button("Cancel") {
                    shortcut(KeyCodeCombination(KeyCode.ESCAPE))
                    action {
                        cancelFlag = true
                        close()
                    }
                }

                button("Save") {
                    shortcut(KeyCodeCombination(KeyCode.ENTER))
                    action {
                        close()
                    }
                }

                spacing = 10.0
            }

            padding = Insets(4.0,4.0,4.0,4.0)
        }
    }

    init {
        manualView.hide()
        manualView.add(buttonBox)
        manualView.add(columnListView)

        manualView.spacing = 2.0
        buttonBox.spacing = 2.0
        buttonBox.padding = Insets(4.0,4.0,4.0,4.0)
        buttonBox.add(button {
            text = "+"

            action {
                val columnNameView = find<GetColumnNameView>().apply { openModal(block = true) }

                if (columnNameView.getResult().isPresent && !columnNameView.cancelFlag) {
                    if (!columnNameView.getResult().get().isBlank()) {
                        columnListView.items.add(columnNameView.getResult().get())
                    }
                }
            }
        })

        buttonBox.add(button {
            text = " - "

            action {
                if (!columnListView.selectedItem.isNullOrEmpty()) {
                    columnListView.items.removeAt(columnListView.items.indexOf(columnListView.selectedItem))
                }
            }
        })
    }

    override fun onDock() {
        ParseFileOptionModalView.logger.debug { "Opening a Rename ParseFileOptionModalView Modal" }

        this.modalStage!!.width = 300.0
        this.modalStage!!.height = 150.0
    }

    override fun onUndock() {
        ParseFileOptionModalView.logger.debug { "Closing a Rename ParseFileOptionModalView Modal" }
    }

    override fun getResultList(): Optional<List<String>> {
        return columnListView.items.asOptional()
    }
}