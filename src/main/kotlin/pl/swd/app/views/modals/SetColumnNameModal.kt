package pl.swd.app.views.modals

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.interfaces.GetResultFragment
import pl.swd.app.utils.asOptional
import tornadofx.*
import java.util.*

class SetColumnNameModal : Modal("Column name"), GetResultFragment<String> {
    companion object : KLogging()

    override var cancelFlag: Boolean = false
    private val textField = textfield()

    override val root = form {
        fieldset("Enter the column name") {
            field("Column name").add(textField)
        }

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
    }

    init {
        textField.requestFocus()
    }

    override fun onDock() {
        logger.debug { "Opening a SetColumnNameModal" }
        super.onDock()
    }

    override fun onUndock() {
        logger.debug { "Closing a SetColumnNameModal with column name: '${textField.text}'" }
        super.onUndock()
    }

    override fun getResult(): Optional<String> {
        return textField.text.asOptional()
    }
}