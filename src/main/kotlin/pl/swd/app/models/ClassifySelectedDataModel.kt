package pl.swd.app.models

import pl.swd.app.services.ClassifyData.ClassifiDistanceMetric

class ClassifySelectedDataModel(val decisionCols: List<String>,
                                val decisionClassCol: String,
                                val newDataRow: DataRow,
                                val kNum: Int,
                                val metric: ClassifiDistanceMetric) {

}