package pl.swd.app.views

import com.github.thomasnield.rxkotlinfx.toObservable
import pl.swd.app.services.ProjectService
import tornadofx.*

class MainView : View("Stat App") {
    val projectService: ProjectService by di()

    override val root = borderpane {
        top(MenuBarView::class)
        left(LeftDrawer::class)
        center(TabsView::class)
    }

    init {
        projectService.currentProject
                .filter { it.isPresent() }
                .map { it.get() }
                .switchMap { it.nameProperty.toObservable() }
                .subscribe { title = "$it - Stat App" }
    }
}
