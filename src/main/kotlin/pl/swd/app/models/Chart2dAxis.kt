package pl.swd.app.models 

data class Chart2dAxis(
        val title: String,
        var numberValues: List<Number>? = null,
        var stringValues: List<String>? = null
)