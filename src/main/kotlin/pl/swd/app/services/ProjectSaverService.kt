package pl.swd.app.services;

import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.exceptions.ConfigDoesNotExistException
import pl.swd.app.exceptions.ProjectDoesNotExistException
import pl.swd.app.exceptions.ValueNotInitializedException
import pl.swd.app.models.Project
import pl.swd.app.views.modals.UpdateProjectSaveFilePathModal
import tornadofx.*

@Service
open class ProjectSaverService {
    companion object : KLogging()

    @Autowired private lateinit var fileIOService: FileIOService
    @Autowired private lateinit var projectService: ProjectService
    @Autowired private lateinit var applicationPropertiesService: ApplicationPropertiesService
    @Autowired private lateinit var configService: ConfigService

    /**
     * Saves current project to a file with path specified as `savePathFile`
     * @param askUserForPath true - ask user for path
     *                       false - ask user for path only when there is no saveFilePath in project settings
     */
    fun saveToFile(askUserForPath: Boolean = false) {
        val project = projectService.currentProject.value.apply {
            if (!isPresent()) {
                throw ProjectDoesNotExistException("Cannot save app state, because a project does not exist")
            }
        }.get()

        /* When there is no saveFilePath then we need to ask user about it*/
        if (askUserForPath || project.saveFilePath.isEmpty()) {
            logger.debug { "No Project saveFilePath is available. Showing a modal." }
            val modal = find<UpdateProjectSaveFilePathModal>(
                    params = mapOf(UpdateProjectSaveFilePathModal::project to project)).apply {
                openModal(block = true)
            }

            if (modal.status.isCancelled()) {
                logger.debug { "User has cancelled providing a saveFilePath. Aborting savingToFile..." }
                return
            }
            /* Here we know user has successfully completed a modal and updated a provided model */
        }
        /* If user didn't provide a path with a valid project extension, then we append it */
        if (!project.saveFilePath.endsWith(fileExtension())) {
            project.saveFilePath += fileExtension()
        }

        logger.debug { "Saving Project to a file: [${project.saveFilePath}]" }

        fileIOService.saveAsJsonToFile(
                data = project,
                fileName = project.saveFilePath)

        configService.currentConfig.value.lastOpenedProjectFilePath = project.saveFilePath
    }

    /**
     * Loads a project from a file with a saveFilePath saved in a live configs file
     * @param askUserForPath true - ask user for path
     *                       false - get path from `lastOpenedProjectFilePath` from Config
     */
    fun loadFromFile(askUserForPath: Boolean = false) {
        val path: String
        if (askUserForPath) {
            logger.debug { "Asking user for a path. Showing a modal." }
            path = fileIOService.openFileDialog(
                    title = "Choose a Project",
                    fileExtensions = fileIOService.projectFileExtensions)
                    .let {
                        if (it.size == 0) {
                            logger.debug { "User didn't choose any file. Aborting loadingFromFile..." }
                            return
                        }
                        it.get(0).absolutePath
                    }
        } else {
            path = configService.currentConfig.value.apply {
                if (this == null) {
                    throw ConfigDoesNotExistException("Cannot load project from file, because Config does not exist")
                }
            }.lastOpenedProjectFilePath.apply {
                if (this == null) {
                    throw ValueNotInitializedException("Value 'lastOpenedProjectFilePath' is not set in a Config. Aborting loading project from a file")
                }
            }!! // i force anti null, because I already validated it above, but somehow IntelliJ doesn't see it
        }

        logger.debug { "Loading Project from a file: [$path]" }

        val project = fileIOService.getAsObjectFromJsonFile<Project>(path)
        project.saveFilePath = path
        projectService.setCurrentProject(project)

        configService.currentConfig.value.lastOpenedProjectFilePath = project.saveFilePath
    }

    fun loadDefaultProject() {
        logger.debug { "Loading default project from memmory" }
        projectService.setCurrentProject(Project("Empty Project"))
    }

    private fun fileExtension(): String = "." + applicationPropertiesService.projectFileExtension
}