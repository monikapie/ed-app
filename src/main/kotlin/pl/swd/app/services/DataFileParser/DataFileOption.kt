package pl.swd.app.services.DataFileParser

enum class DataFileOption {
    AUTO_DETECT_COLUMS,
    USER_COLUMS;

    fun isAutoDetect() = this === AUTO_DETECT_COLUMS
    fun isUserColums() = this === USER_COLUMS
}