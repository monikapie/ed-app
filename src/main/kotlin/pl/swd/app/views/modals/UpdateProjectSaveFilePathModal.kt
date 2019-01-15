package pl.swd.app.views.modals

import com.github.thomasnield.rxkotlinfx.actionEvents
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.models.Project
import pl.swd.app.services.FileIOService
import tornadofx.*

class UpdateProjectSaveFilePathModal : Modal("Project File") {
    companion object : KLogging()

    val fileIOService: FileIOService by di()

    val project: Project by param()
    val model = ProjectViewModel(project)

    override val root = form {
        fieldset("Where do you want to save the Project?") {
            field("Path") {
                textfield(model.saveFilePath).requestFocus()
                button("Browse") {
                    this@button.actionEvents()
                            .flatMap {
                                fileIOService.openFileDialogObs(
                                        title = "Choose File",
                                        fileExtensions = fileIOService.projectFileExtensions,
                                        initialDirectory = model.saveFilePath.value,
                                        mode = FileChooserMode.Save
                                ).take(1)
                            }
                            .subscribe { model.saveFilePath.value = it.absolutePath }
                }
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
                shortcut(KeyCodeCombination(KeyCode.ENTER))
                action {
                    save()
                }
            }
        }
    }

    override fun onDock() {
        logger.debug { "Opening an ${this.javaClass.simpleName} with: saveFilePath = '${project.saveFilePath}'" }
        super.onDock()
    }

    override fun onUndock() {
        logger.debug { "Closing an ${this.javaClass.simpleName} with: saveFilePath = '${project.saveFilePath}'" }
        super.onUndock()
    }

    private fun save() {
        logger.debug { "Updated Project saveFilePath from: '${project.saveFilePath}' to: '${model.saveFilePath.value}'" }
        model.commit()
        status = ModalStatus.COMPLETED
        close()
    }

    class ProjectViewModel(var project: Project) : ViewModel() {
        val saveFilePath = bind { project.saveFilePathProperty }
    }
}