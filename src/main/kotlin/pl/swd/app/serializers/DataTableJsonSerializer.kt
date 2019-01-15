package pl.swd.app.serializers

import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.*
import pl.swd.app.models.DataTable
import pl.swd.app.services.DataFileParser.DataFileOption
import pl.swd.app.services.DataFileParser.DataFileParserService
import java.lang.reflect.Type

class DataTableJsonSerializer(
        val dataFileParserService: DataFileParserService
) : JsonSerializer<DataTable>, JsonDeserializer<DataTable> {

    override fun serialize(src: DataTable, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val rowsList = dataFileParserService.parseDataTableToRawData(src)
        return jsonArray(rowsList)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DataTable {
        val rowsList = json.asJsonArray.toList()
                .map { it.asString }

        return dataFileParserService.parseRawDataToDataTable(rowsList, emptyList(), DataFileOption.AUTO_DETECT_COLUMS)
    }

}