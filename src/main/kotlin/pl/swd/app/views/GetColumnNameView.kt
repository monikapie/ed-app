package pl.swd.app.views

import javafx.scene.Parent
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.interfaces.GetResultFragment
import pl.swd.app.utils.asOptional
import tornadofx.*
import java.util.*
import javax.swing.GroupLayout

class GetColumnNameView: Fragment("Column name"), GetResultFragment<String> {
    companion object : KLogging()

    override var cancelFlag: Boolean = false
    private val textField = textfield()

    override val root = form {
        fieldset("Give the column name") {
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
        GetColumnNameView.logger.debug { "Opening a GetColumnNameView" }
    }

    override fun onUndock() {
        GetColumnNameView.logger.debug { "Closing a GetColumnNameView with column name: '${textField.text}'" }
    }

    override fun getResult(): Optional<String> {
        return textField.text.asOptional()
    }
}