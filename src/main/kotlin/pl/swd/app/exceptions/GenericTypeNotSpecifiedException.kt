package pl.swd.app.exceptions

class GenericTypeNotSpecifiedException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}