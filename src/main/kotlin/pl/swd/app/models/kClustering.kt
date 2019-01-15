package pl.swd.app.models

import pl.swd.app.services.ClassifyData.ClassifiDistanceMetric

class kClustering(val decisionCols: List<String>,
                                val kNum: Int,
                                val metric: ClassifiDistanceMetric) {

}