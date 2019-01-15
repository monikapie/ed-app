package pl.swd.app.services.SpaceDivider

import mu.KLogging
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.ArrayList

@Service
class SpaceDividerService {
    companion object : KLogging()

    fun initializeAlgorithm(initData: SpaceDividerInitData): SpaceDividerWorker {
        val (pointsList) = initData
        val axisesSize = determineAxisesSize(pointsList)
        val initialSortedAxisesPoints = sortAxisesPointsAscending(pointsList, axisesSize)
        val remainingSortedAxisesPoints = sortAxisesPointsAscending(pointsList, axisesSize)

        return SpaceDividerWorker(initialSortedAxisesPoints, remainingSortedAxisesPoints, axisesSize, initData.axisNames)
    }

    inner class SpaceDividerWorker(
            val initialSortedAxisesPoints: List<List<SpaceDividerPoint>>,
            val remainingSortedAxisesPoints: List<List<SpaceDividerPoint>>,
            val axisesSize: Int,
            val axisNames: List<String>
    ) {
        val iterationsResults = ArrayList<PointsToRemoveIn1CutResponse>()

        fun nextIteration(): PointsToRemoveIn1CutResponse {
            validateOperationsCompleteness()

            lateinit var pointsToRemoveResponse: PointsToRemoveIn1CutResponse
            do {
                val allPotentialCutPoints = findAllPotentialCutPoints(remainingSortedAxisesPoints, axisesSize)
                pointsToRemoveResponse = findMostPointsThatCanBeRemovedIn1Cut(allPotentialCutPoints)
                if (pointsToRemoveResponse.pointsToRemoveIn1Cut.size == 0) {
                    removePointsFromRemainingLists(remainingSortedAxisesPoints, listOf(remainingSortedAxisesPoints.first().first()))
                    continue
                } else {
                    break
                }
            } while (true)

            addVectorValuesToAllPoints(initialSortedAxisesPoints, pointsToRemoveResponse)

            removePointsFromRemainingLists(remainingSortedAxisesPoints, pointsToRemoveResponse.pointsToRemoveIn1Cut)

            iterationsResults.add(pointsToRemoveResponse)
            return pointsToRemoveResponse
        }

        fun completeAllIterations(): List<PointsToRemoveIn1CutResponse> {
            val nextIterationsList = ArrayList<PointsToRemoveIn1CutResponse>()
            var i = 0
            try {
                while (true) {
                    nextIterationsList.add(nextIteration())
                    logger.trace { "Iteration ${++i} completed. Remaining size ${remainingSortedAxisesPoints.first().size}" }
                }
            } catch (e: IterationsAlreadyCompletedException) {

            }

            return nextIterationsList
        }

        internal fun validateOperationsCompleteness() {
            if (axisesSize <= 0) {
                throw IterationsAlreadyCompletedException("Cannot iterate, because axises size is $axisesSize")
            }

            if (remainingSortedAxisesPoints.first().isEmpty()) {
                throw IterationsAlreadyCompletedException("Not remaining points left")
            }

            val potentialOnlyDecisionClass = remainingSortedAxisesPoints.first().first().decisionClass
            val potentialSameAxisesValues = remainingSortedAxisesPoints.first().first().axisesValues

            /* When every remaining point has the same decision class */
            if (remainingSortedAxisesPoints.all { remainingSortedAxisPoints ->
                remainingSortedAxisPoints.all {
                    it.decisionClass.equals(potentialOnlyDecisionClass) || it.axisesValues.equals(potentialSameAxisesValues)
                }
            }) {
                throw IterationsAlreadyCompletedException("Every remaining point has the same decision class")
            }
        }
    }

    /**
     * Validates if every point has the same declared number of axises.
     * Returns number of axieses
     */
    internal fun determineAxisesSize(pointsList: List<SpaceDividerPoint>): Int {
        if (pointsList.isEmpty()) throw EmptyListException("Points List cannot be empty")

        val probableAxiesesSize = pointsList.first().axisesValues.size
        val allHasTheSameSize = pointsList.all { it.axisesValues.size == probableAxiesesSize }

        if (!allHasTheSameSize) throw AxisesSizeMissmatchException("Points does not have the same axises values size")

        return probableAxiesesSize
    }

    /**
     * Returns a listof axises.
     * Each element of a list contains a list of points that are sorted by corresponding axis index
     */
    internal fun sortAxisesPointsAscending(pointsList: List<SpaceDividerPoint>, axisesSize: Int): List<List<SpaceDividerPoint>> {
        val sortedAxises = ArrayList<LinkedList<SpaceDividerPoint>>(axisesSize)

        if (axisesSize == 0) return sortedAxises

        for (axisIndex in 0..axisesSize - 1) {
            val sortedPoints = pointsList.sortedWith(Comparator { o1, o2 -> o1.axisesValues[axisIndex].compareTo(o2.axisesValues[axisIndex]) })
            sortedAxises.add(axisIndex, LinkedList(sortedPoints))
        }

        return sortedAxises
    }

    /**
     * Finds left and right edge points with the same decision class
     * For example: ["a","b","c","c"]
     * It returns: {
     *  negativeCutPoints: ["a"]
     *  positiveCutPoints: ["c","c"]
     * }
     *
     * There is an edge case when 2 edge points with the same value, but different decision classes
     * In that case from that edge there are no points to cut
     * For example: ["a"(1),"b"(1),"c"(2),"c"(3)]
     * It returns: {
     *  negativeCutPoints: [] // because right points have the same value (1), but different decision classes "a","b"
     *  positiveCutPoints: ["c"(3),"c"(2)]
     * }
     */
    internal fun findPointsToCut(sortedAxisPoints: List<SpaceDividerPoint>, axisIndex: Int): PointsToCutResposne {
        if (sortedAxisPoints.isEmpty()) throw EmptyListException("List cannot be empty")

        val negativePointDecisionClass = sortedAxisPoints.first().decisionClass
        val negativeCutPoints = arrayListOf<SpaceDividerPoint>()

        for (i in 0..sortedAxisPoints.lastIndex) {
            /* When we have the same values */
            if (sortedAxisPoints[i].decisionClass != negativePointDecisionClass) {
                negativeCutPoints.removeIf { it.axisesValues[axisIndex] == sortedAxisPoints[i].axisesValues[axisIndex] }
                break
            }
            negativeCutPoints.add(sortedAxisPoints[i])
        }


        val positivePointDecisionClass = sortedAxisPoints.last().decisionClass
        val positiveCutPoints = arrayListOf<SpaceDividerPoint>()

        for (i in sortedAxisPoints.lastIndex downTo 0) {
            if (sortedAxisPoints[i].decisionClass != positivePointDecisionClass) {
                positiveCutPoints.removeIf { it.axisesValues[axisIndex] == sortedAxisPoints[i].axisesValues[axisIndex] }
                break
            }
            positiveCutPoints.add(sortedAxisPoints[i])
        }

        return PointsToCutResposne(
                negativeCutPoints = negativeCutPoints,
                positiveCutPoints = positiveCutPoints
        )
    }

    /**
     *  Generates a list of all potential points to cut among [remainingSortedAxisesPoints]
     *  When there are 2 axies it generates:
     *   * 2 lists for axis X (left and right) or (positive and negative)
     *   * 2 lists for axis Y (bottom and top) or (positive and negative)
     */
    internal fun findAllPotentialCutPoints(remainingSortedAxisesPoints: List<List<SpaceDividerPoint>>, axisesSize: Int): List<PointsToCutResposne> {
        val allPotentialCutPoints = ArrayList<PointsToCutResposne>(axisesSize)
        for (index in 0..axisesSize - 1) {
            val potentialCutPoints = findPointsToCut(remainingSortedAxisesPoints[index], index)
            allPotentialCutPoints.add(index, potentialCutPoints)
        }
        return allPotentialCutPoints
    }

    /**
     * Returns [SpaceDividerPoint] list which has the biggest size from within [allPotentialCutPoints]
     * When there are several lists with the same size it takes the first one.
     */
    internal fun findMostPointsThatCanBeRemovedIn1Cut(allPotentialCutPoints: List<PointsToCutResposne>): PointsToRemoveIn1CutResponse {
        if (allPotentialCutPoints.isEmpty()) throw EmptyListException("All potential cut points list cannot be empty")

        val response = PointsToRemoveIn1CutResponse(
                axisIndex = 0,
                isPositive = false,
                cutLineValue = 0f,
                pointsToRemoveIn1Cut = emptyList()
        )
        allPotentialCutPoints.forEachIndexed { index, potentialAxisCutPoints ->
            if (potentialAxisCutPoints.negativeCutPoints.size > response.pointsToRemoveIn1Cut.size) {
                with(response) {
                    axisIndex = index
                    isPositive = false
                    /* first(), because we take the value of the point to the very right */
                    cutLineValue = potentialAxisCutPoints.negativeCutPoints.last().axisesValues[index]
                    pointsToRemoveIn1Cut = potentialAxisCutPoints.negativeCutPoints
                }
            }
            if (potentialAxisCutPoints.positiveCutPoints.size > response.pointsToRemoveIn1Cut.size) {
                with(response) {
                    axisIndex = index
                    isPositive = true
                    /* last(), because we take the value of the point to the very left */
                    cutLineValue = potentialAxisCutPoints.positiveCutPoints.last().axisesValues[index]
                    pointsToRemoveIn1Cut = potentialAxisCutPoints.positiveCutPoints
                }
            }
        }

        return response
    }

    /**
     * INVALID - RETYPE
     * When providing axis with values [1,2,3,4,5,6,7] and a [cutlineValue] of 5 with [positive] cut
     * It will add vector values like this [1(0), 2(0), 3(0), 4(0), 5(1), 6(1), 7(1)]
     *
     * When providing axis with values [1,2,3,4,5,6,7] and a [cutlineValue] of 5 with [negative] cut
     * It will add vector values like this [1(0), 2(0), 3(0), 4(0), 5(0), 6(1), 7(1)]
     *
     * Take a look at value 5(*). When the cut is positive the [1] value in a vector is inclusive
     */
    internal fun addVectorValuesToAllPoints(
            initialSortedAxisesPoints: List<List<SpaceDividerPoint>>,
            pointsToRemoveResponse: PointsToRemoveIn1CutResponse
    ) {

        for (point in initialSortedAxisesPoints[pointsToRemoveResponse.axisIndex]) {
            if (point.axisesValues[pointsToRemoveResponse.axisIndex] > pointsToRemoveResponse.cutLineValue) {
                if (pointsToRemoveResponse.isPositive) {
                    point.vector.add(0b1)
                } else {
                    point.vector.add(0b0)
                }
            } else if (point.axisesValues[pointsToRemoveResponse.axisIndex] < pointsToRemoveResponse.cutLineValue) {
                if (pointsToRemoveResponse.isPositive) {
                    point.vector.add(0b0)
                } else {
                    point.vector.add(0b1)
                }
            } else if (point.axisesValues[pointsToRemoveResponse.axisIndex] == pointsToRemoveResponse.cutLineValue) {
                point.vector.add(0b1)
            }
        }
    }

    /**
     * Removes points from all [remainingSortedAxisesPoints] based on refferential equality
     */
    internal fun removePointsFromRemainingLists(
            remainingSortedAxisesPoints: List<List<SpaceDividerPoint>>,
            pointsToRemove: List<SpaceDividerPoint>
    ) {
        for (remainingSortedAxisPoints in remainingSortedAxisesPoints) {
            /* Remove the point [it] from remaining list when it is inside points to remove list */
            (remainingSortedAxisPoints as LinkedList).removeIf { potentialPointToRemove ->
                pointsToRemove.any { it === potentialPointToRemove }
            }
        }
    }
}

data class SpaceDividerInitData(
        val pointsList: List<SpaceDividerPoint>,
        val axisNames: List<String>
)

data class SpaceDividerPoint(
        /**
         * Size of array indicates how many axises there are
         * For example: [1,2,3] means x=1, y=2, z=3
         */
        val axisesValues: Array<Float>,
        val decisionClass: String,
        val vector: ArrayList<Byte> = ArrayList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpaceDividerPoint

        if (!Arrays.equals(axisesValues, other.axisesValues)) return false
        if (decisionClass != other.decisionClass) return false
        if (vector != other.vector) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(axisesValues)
        result = 31 * result + decisionClass.hashCode()
        result = 31 * result + vector.hashCode()
        return result
    }
}

data class PointsToCutResposne(
        var positiveCutPoints: List<SpaceDividerPoint>,
        var negativeCutPoints: List<SpaceDividerPoint>
)

data class PointsToRemoveIn1CutResponse(
        /**
         * When we remove points from axis x, this value will be 0
         */
        var axisIndex: Int,
        /**
         * When we remove points from the right side of the axis it will be true
         * When we remove points form the left side of the axis it will be false
         */
        var isPositive: Boolean,
        /**
         * Value on the axis [axisIndex] where the line should be placed to make a cut
         * It's the value from the nearest [pointsToRemoveIn1Cut] to the rest of the points
         */
        var cutLineValue: Float,
        /**
         * List of points to remove from remaining points list
         */
        var pointsToRemoveIn1Cut: List<SpaceDividerPoint>
)

class AxisesSizeMissmatchException(message: String) : Exception(message)

class EmptyListException(message: String) : Exception(message)

class IterationsAlreadyCompletedException(message: String) : Exception(message)