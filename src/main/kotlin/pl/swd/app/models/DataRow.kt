package pl.swd.app.models;

data class DataRow(
        val rawInitialString: String,
        var rowValuesMap: Map<String, DataValue>
) {
    var ida = 0

    fun addValue(columnName: String, data: DataValue) {
        val rowValuesMapp = rowValuesMap.toMutableMap()
        rowValuesMapp.put(columnName, data)
        rowValuesMap = rowValuesMapp
    }

    fun compareRow(row: DataRow): Boolean {
        var equal = false

        this.rowValuesMap.forEach { (name, value) ->
            if (row.rowValuesMap.getValue(name).value.toString() == value.value.toString()) {
                equal = true
            } else {
                return false
            }
        }

        return equal
    }
}