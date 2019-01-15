package pl.swd.app.exceptions;

class ProjectDoesNotExistException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}