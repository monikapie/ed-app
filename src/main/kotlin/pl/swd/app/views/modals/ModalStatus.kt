package pl.swd.app.views.modals

enum class ModalStatus {
    PRISTINE,
    COMPLETED,
    CANCELLED;

    fun isPristine() = this === PRISTINE
    fun isCompleted() = this === COMPLETED
    fun isCancelled() = this === CANCELLED
}