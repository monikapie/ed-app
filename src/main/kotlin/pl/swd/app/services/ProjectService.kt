package pl.swd.app.services;

import io.reactivex.subjects.BehaviorSubject
import mu.KLogging
import org.springframework.stereotype.Service
import pl.swd.app.exceptions.ProjectDoesNotExistException
import pl.swd.app.models.Project
import pl.swd.app.utils.asOptional
import pl.swd.app.utils.emptyOptional
import pl.swd.app.views.modals.RenameProjectModal
import tornadofx.*
import java.util.*

/**
 * Holds a currentProject state
 */
@Service
open class ProjectService {
    companion object : KLogging()

    /**
     * Starting a behaviour subject with empty Project.
     * Need to wrap Project in Optional, because BehaviourSubject doesn't allow null
     */
    val currentProject: BehaviorSubject<Optional<Project>> = BehaviorSubject.createDefault(emptyOptional())

    /**
     * Emits given project as a current Project
     */
    fun setCurrentProject(project: Project) {
        currentProject.onNext(project.asOptional())
    }

    fun unsetCurrentProject() {
        currentProject.onNext(emptyOptional())
    }

    fun renameProject(block: Boolean = false) {
        if (!currentProject.value.isPresent()) {
            throw ProjectDoesNotExistException("Cannot Rename A Project, because Project does not exist")
        }

        find<RenameProjectModal>(
                params = mapOf(RenameProjectModal::project to currentProject.value.get()))
                .openModal(block = block)
    }

}