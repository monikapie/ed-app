package pl.swd.testHelpers

import pl.swd.app.services.SpaceDivider.SpaceDividerPoint

fun isStructuralEquality(arg1: List<SpaceDividerPoint>, arg2: List<SpaceDividerPoint>): Boolean {
    if (arg1.size != arg2.size) {
        return false
    }

    for (i in 0..arg1.size - 1) {
        if (!arg1[i].axisesValues.contentEquals(arg2[i].axisesValues)
                && !arg1[i].decisionClass.equals(arg2[i].decisionClass)) return false
    }

    return true
}