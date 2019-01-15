package pl.swd.app.views.modals

import javafx.geometry.Insets
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.interfaces.GetResultFragment
import pl.swd.app.services.DataFileParser.DataFileOption
import pl.swd.app.utils.asOptional
import tornadofx.*
import java.util.*

class ParseDataFileOptionsModal : Modal("Rename Column"), GetResultFragment<String> {
    companion object : KLogging()

    override var cancelFlag: Boolean = false

    private val buttonBox = hbox()
    private val manualView = vbox()
    private val columnListView = listview<String> {
        selectionModel.selectionMode = SelectionMode.SINGLE
    }
    private var option = DataFileOption.AUTO_DETECT_COLUMS

    override val root = borderpane {
        center {
            vbox {
                togglegroup {
                    radiobutton {
                        text = "Auto detect columns name"
                        isSelected = true

                        action {
                            columnListView.items.removeAll()
                            manualView.hide()
                            currentStage?.height = 150.0
                            option = DataFileOption.AUTO_DETECT_COLUMS
                        }
                    }

                    radiobutton {
                        text = "Add manual colums name"

                        action {
                            manualView.show()
                            currentStage?.height = 300.0
                            option = DataFileOption.USER_COLUMS
                        }
                    }

                    spacing = 2.0
                }

                spacing = 10.0
            }.add(manualView)

            padding = Insets(4.0, 4.0, 4.0, 4.0)
        }

        bottom {
            buttonbar {
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
            }

            padding = Insets(4.0, 4.0, 4.0, 4.0)
        }
    }

    init {
        manualView.apply {
            hide()
            add(buttonBox)
            add(columnListView)
            spacing = 2.0
        }

        buttonBox.apply {
            spacing = 2.0
            padding = Insets(4.0, 4.0, 4.0, 4.0)
            add(button {
                text = "+"

                action {
                    val columnNameView = find<SetColumnNameModal>().apply { openModal(block = true) }

                    if (columnNameView.getResult().isPresent && !columnNameView.cancelFlag) {
                        if (!columnNameView.getResult().get().isBlank()) {
                            columnListView.items.add(columnNameView.getResult().get())
                        }
                    }
                }
            })
            add(button {
                text = " - "

                action {
                    if (!columnListView.selectedItem.isNullOrEmpty()) {
                        columnListView.items.removeAt(columnListView.items.indexOf(columnListView.selectedItem))
                    }
                }
            })
        }
    }

    override fun onDock() {
        logger.debug { "Opening a Rename ParseDataFileOptionsModal Modal" }
        super.onDock()
    }

    override fun onUndock() {
        logger.debug { "Closing a Rename ParseDataFileOptionsModal Modal" }
        super.onUndock()
    }

    /**
     * Gets column names list
     */
    override fun getResultList(): Optional<List<String>> {
        super.getResultList()
        return columnListView.items.asOptional()
    }

    fun getOption(): DataFileOption {
        return option
    }
}