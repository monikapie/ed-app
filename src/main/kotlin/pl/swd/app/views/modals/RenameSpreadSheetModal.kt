package pl.swd.app.views.modals

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.models.SpreadSheet
import tornadofx.*

class RenameSpreadSheetModal : Modal("SpreadSheet") {
    companion object : KLogging()

    val spreadSheet: SpreadSheet by param()
    val model = SpreadSheetViewModel(spreadSheet)

    override val root = form {
        fieldset("Choose a new name for the SpreadSheet") {
            field("Name") {
                textfield(model.name).requestFocus()
            }
        }

        buttonbar {
            button("Cancel") {
                shortcut(KeyCodeCombination(KeyCode.ESCAPE))
                action {
                    close()
                }
            }

            button("Save") {
                enableWhen(model.dirty)
                shortcut(KeyCodeCombination(KeyCode.ENTER))
                action {
                    save()
                }
            }
        }
    }

    override fun onDock() {
        logger.debug { "Opening a ${this.javaClass.simpleName} with: name = '${spreadSheet.name}'" }
        super.onDock()
    }

    override fun onUndock() {
        logger.debug { "Closing a ${this.javaClass.simpleName} with: name = '${spreadSheet.name}'" }
        super.onUndock()
    }

    private fun save() {
        logger.debug { "Updated SpreadSheet name from: '${spreadSheet.name}' to: '${model.name.value}'" }
        model.commit()
        status = ModalStatus.COMPLETED
        close()
    }

    class SpreadSheetViewModel(var spreadSheet: SpreadSheet) : ViewModel() {
        val name = bind { spreadSheet.nameProperty }
    }
}
// todo https://github.com/edvin/tornadofx/wiki/ViewModel

