package pl.swd.app.views.modals

import javafx.scene.Node
import tornadofx.*

abstract class Modal(title: String? = null, icon: Node? = null) : Fragment(title, icon) {
    /**
     * Flag that holds the status of the modal
     */
    var status: ModalStatus = ModalStatus.PRISTINE

    override fun onUndock() {
        if (status === ModalStatus.PRISTINE) {
            status = ModalStatus.CANCELLED
        }
        super.onUndock()
    }

    override fun onDock() {
        currentWindow?.apply {
            width = 500.0
            centerOnScreen()
        }
        super.onDock()
    }
}
