package pl.swd.app.serializers

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.*
import pl.swd.app.models.SpreadSheet
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType

object SpreadSheetJsonSerializer : JsonSerializer<SpreadSheet>, JsonDeserializer<SpreadSheet> {
    override fun serialize(src: SpreadSheet?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return jsonObject(
                SpreadSheet::name.name to JsonPrimitive(src?.name),
                SpreadSheet::autoOpenTabOnLoad.name to JsonPrimitive(src?.autoOpenTabOnLoad),
                SpreadSheet::dataTable.name to context?.serialize(src?.dataTable)
        )
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): SpreadSheet {
        return SpreadSheet(
                name = json.get(SpreadSheet::name.name).asString,
                autoOpenTabOnLoad = json.get(SpreadSheet::autoOpenTabOnLoad.name).asBoolean,
                dataTable = context.deserialize(json.get(SpreadSheet::dataTable.name), SpreadSheet::dataTable.returnType.javaType)
        )
    }
}