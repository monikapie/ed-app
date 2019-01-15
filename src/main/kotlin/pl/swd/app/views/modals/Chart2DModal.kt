package pl.swd.app.views.modals

import io.reactivex.Observable
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.ScatterChart
import javafx.scene.chart.XYChart
import javafx.scene.layout.VBox
import mu.KLogging
import pl.swd.app.models.Chart2dData
import tornadofx.*

class Chart2DModal : Modal() {
    companion object : KLogging()

    val chart2dData: Chart2dData by param()
    var box: VBox by singleAssign()

    override val root = borderpane {
        center {
            box = vbox()
        }
    }

    init {
        val chart =
                /* X - Numbers, Y - Numbers */
                if (chart2dData.xAxis.numberValues !== null && chart2dData.yAxis.numberValues !== null) {
                    ScatterChart(
                            NumberAxis().apply { this.label = chart2dData.xAxis.title },
                            NumberAxis().apply { this.label = chart2dData.yAxis.title })
                            .apply {
                                val seriesMap: HashMap<String, XYChart.Series<Number, Number>> = HashMap()

                                chart2dData.series.forEach {
                                    if (!seriesMap.containsKey(it)) {
                                        seriesMap.put(it, XYChart.Series())
                                    }
                                }

                                var index = 0
                                zipValues(chart2dData.xAxis.numberValues!!, chart2dData.yAxis.numberValues!!)
                                        .blockingSubscribe { pair ->
                                            if (index < chart2dData.series.size) {
                                                seriesMap.get(chart2dData.series[index++])?.data(pair.first, pair.second)
                                            } else {
                                                seriesMap.putIfAbsent(chart2dData.title, XYChart.Series())?.data(pair.first, pair.second)
                                            }
                                        }

                                seriesMap
                                        .toSortedMap()
                                        .forEach { key, value ->
                                            value.name = key
                                            data.add(value)
                                        }
                                (xAxis as NumberAxis).setForceZeroInRange(false)
                                (yAxis as NumberAxis).setForceZeroInRange(false)
//
//                                (xAxis as NumberAxis).apply {
//                                    setAutoRanging(true)
//                                    lowerBound = 4.0
//                                    upperBound = 15.0
//                                    tickUnit = 2.0
//                                }

                            }
                }
                /* X - Numbers, Y - Strings */
                else if (chart2dData.xAxis.numberValues !== null && chart2dData.yAxis.stringValues !== null) {
                    ScatterChart(
                            NumberAxis().apply { this.label = chart2dData.xAxis.title },
                            CategoryAxis().apply { this.label = chart2dData.yAxis.title })
                            .apply {
                                val seriesMap: HashMap<String, XYChart.Series<Number, String>> = HashMap()

                                chart2dData.series.forEach {
                                    if (!seriesMap.containsKey(it)) {
                                        seriesMap.put(it, XYChart.Series())
                                    }
                                }

                                var index = 0
                                zipValues(chart2dData.xAxis.numberValues!!, chart2dData.yAxis.stringValues!!)
                                        .blockingSubscribe { pair ->
                                            if (index < chart2dData.series.size) {
                                                seriesMap.get(chart2dData.series[index++])?.data(pair.first, pair.second)
                                            } else {
                                                seriesMap.putIfAbsent(chart2dData.title, XYChart.Series())?.data(pair.first, pair.second)
                                            }
                                        }

                                seriesMap
                                        .toSortedMap()
                                        .forEach { key, value ->
                                            value.name = key
                                            data.add(value)
                                        }

                                (xAxis as NumberAxis).setForceZeroInRange(false)
                            }
                }
                /* X - Strings, Y - Numbers */
                else if (chart2dData.xAxis.stringValues !== null && chart2dData.yAxis.numberValues !== null) {
                    ScatterChart(
                            CategoryAxis().apply { this.label = chart2dData.xAxis.title },
                            NumberAxis().apply { this.label = chart2dData.yAxis.title })
                            .apply {
                                val seriesMap: HashMap<String, XYChart.Series<String, Number>> = HashMap()

                                chart2dData.series.forEach {
                                    if (!seriesMap.containsKey(it)) {
                                        seriesMap.put(it, XYChart.Series())
                                    }
                                }

                                var index = 0
                                zipValues(chart2dData.xAxis.stringValues!!, chart2dData.yAxis.numberValues!!)
                                        .blockingSubscribe { pair ->
                                            if (index < chart2dData.series.size) {
                                                seriesMap.get(chart2dData.series[index++])?.data(pair.first, pair.second)
                                            } else {
                                                seriesMap.putIfAbsent(chart2dData.title, XYChart.Series())?.data(pair.first, pair.second)
                                            }
                                        }

                                seriesMap
                                        .toSortedMap()
                                        .forEach { key, value ->
                                            value.name = key
                                            data.add(value)
                                        }

                                (yAxis as NumberAxis).setForceZeroInRange(false)
                            }
                }
                /* X- Strings, Y - String */
                else if (chart2dData.xAxis.stringValues !== null && chart2dData.yAxis.stringValues !== null) {
                    ScatterChart(
                            CategoryAxis().apply { this.label = chart2dData.xAxis.title },
                            CategoryAxis().apply { this.label = chart2dData.yAxis.title })
                            .apply {
                                val seriesMap: HashMap<String, XYChart.Series<String, String>> = HashMap()

                                chart2dData.series.forEach {
                                    if (!seriesMap.containsKey(it)) {
                                        seriesMap.put(it, XYChart.Series())
                                    }
                                }

                                var index = 0
                                zipValues(chart2dData.xAxis.stringValues!!, chart2dData.yAxis.stringValues!!)
                                        .blockingSubscribe { pair ->
                                            if (index < chart2dData.series.size) {
                                                seriesMap.get(chart2dData.series[index++])?.data(pair.first, pair.second)
                                            } else {
                                                seriesMap.putIfAbsent(chart2dData.title, XYChart.Series())?.data(pair.first, pair.second)
                                            }
                                        }

                                seriesMap
                                        .toSortedMap()
                                        .forEach { key, value ->
                                            value.name = key
                                            data.add(value)
                                        }
                            }
                } else {
                    throw Exception("Axises must have at least one stringValues or numberValues, but now 1 or more has none")
                }

        chart.title = chart2dData.title
        box.add(chart)

    }

    private fun <A, B> zipValues(firstList: List<A>, secondList: List<B>): Observable<Pair<A, B>> {
        return Observable.fromIterable(firstList)
                .zipWith(secondList, { t1, t2 ->
                    Pair(t1, t2)
                })
    }
}