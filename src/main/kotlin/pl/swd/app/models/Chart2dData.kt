package pl.swd.app.models

data class Chart2dData(
        val title: String,
        val xAxis: Chart2dAxis,
        val yAxis: Chart2dAxis,
        var series: List<String>
)