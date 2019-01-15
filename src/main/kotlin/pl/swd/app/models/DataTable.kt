package pl.swd.app.models;

import javafx.collections.ObservableList
import pl.swd.app.utils.asOptional
import java.util.*

class DataTable(
        var rows: ObservableList<DataRow>,
        var columns: ObservableList<DataColumn>
) {
    fun getColumnIndexByName(name: String): Optional<Int> {
        for(i in columns.indices) {
            if (columns[i].name == name) {
                return i.asOptional()
            }
        }

        return Optional.empty()
    }

    fun addRow(row: DataRow) {
        rows.add(row)

        columns.forEach {
            it.columnValuesList.add(row.rowValuesMap.getValue(it.name))
        }
    }
}