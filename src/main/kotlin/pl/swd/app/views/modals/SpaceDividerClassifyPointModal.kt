package pl.swd.app.views.modals

import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import mu.KLogging
import pl.swd.app.services.SpaceDivider.SpaceDividerPoint
import pl.swd.app.services.SpaceDivider.SpaceDividerService
import tornadofx.*

class SpaceDividerClassifyPointModal : Modal("Classify Point") {
    companion object : KLogging()

    val axisesNames: List<String> by param()
    val worker: SpaceDividerService.SpaceDividerWorker by param()
    val model = SpaceDividerClassifyPointViewModel(axisesNames)


    override val root = form {
        fieldset("Provide point values to classify") {
            for (axisName in axisesNames) {
                field(axisName) {
                    textfield(model.pointValues[axisName]!!) {
                        required()
                        validator { s: String? ->
                            if (s === null) {
                                return@validator ValidationMessage("Required", ValidationSeverity.Error)
                            }

                            try {
                                s.toFloat()
                                return@validator null
                            } catch (e: NumberFormatException) {
                                return@validator ValidationMessage("Not a valid number", ValidationSeverity.Error)
                            }
                        }
                    }
                }
            }
        }
        buttonbar {
            button("Cancel") {
                action { close() }
            }
            button("Classify") {
                shortcut(KeyCodeCombination(KeyCode.ENTER))
                enableWhen(model.valid)
                action {
                    classifyPoints()
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        currentWindow?.apply {
            width = 300.0
            centerOnScreen()
        }
    }

    fun classifyPoints() {
        val pointValues = model.pointValues.values
                .map { property -> property.value }
                .map { pointValue -> pointValue.toFloat() }
                .toTypedArray()

        val closestPoint = getClosestPoint(worker.initialSortedAxisesPoints.first(), pointValues)

        information("Result", "The point should be classified as ${closestPoint.decisionClass}")
    }

    fun getClosestPoint(pointsList: List<SpaceDividerPoint>, pointValues: Array<Float>): SpaceDividerPoint {
        var minLength = Float.MAX_VALUE
        var point: SpaceDividerPoint? = null

        for (spaceDividerPoint in pointsList) {
            val path = calculatePath(spaceDividerPoint.axisesValues, pointValues)
            if (path < minLength) {
                minLength = path
                point = spaceDividerPoint
            }
        }

        return point!!
    }

    fun calculatePath(pointA: Array<Float>, pointB: Array<Float>): Float {
        var total = 0.0
        for (i in 0..pointA.lastIndex) {
            total += Math.pow(pointA[i].toDouble() - pointB[i].toDouble(), 2.0)
        }
        return Math.sqrt(total).toFloat()
    }

    class SpaceDividerClassifyPointViewModel(axisesNames: List<String>) : ViewModel() {
        val pointValues = HashMap<String, Property<String>>().also {
            for (axisName in axisesNames) {
                it.put(axisName, bind { SimpleStringProperty() })
            }
        }
    }
}