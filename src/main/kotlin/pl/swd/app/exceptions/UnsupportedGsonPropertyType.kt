package pl.swd.app.exceptions

class UnsupportedGsonPropertyType : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}