package pl.swd.app.exceptions;

class InvalidColumnException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}