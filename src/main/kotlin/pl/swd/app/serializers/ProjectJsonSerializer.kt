package pl.swd.app.serializers

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.*
import pl.swd.app.models.Project
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType

object ProjectJsonSerializer : JsonSerializer<Project>, JsonDeserializer<Project> {
    override fun serialize(src: Project, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return jsonObject(
                Project::name.name to JsonPrimitive(src.name),
                Project::saveFilePath.name to JsonPrimitive(src.saveFilePath),
                Project::spreadSheetList.name to context.serialize(src.spreadSheetList)
        )
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Project {
        return Project(
                name = json.get(Project::name.name).asString,
                saveFilePath = json.get(Project::saveFilePath.name).asString,
                spreadSheetList = context.deserialize(json.get(Project::spreadSheetList.name), Project::spreadSheetList.returnType.javaType)
        )
    }
}