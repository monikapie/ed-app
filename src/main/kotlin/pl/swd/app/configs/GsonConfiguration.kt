package pl.swd.app.configs

import com.google.gson.Gson
import mu.KLogging
import org.hildan.fxgson.FxGson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.swd.app.models.DataTable
import pl.swd.app.models.Project
import pl.swd.app.models.SpreadSheet
import pl.swd.app.serializers.DataTableJsonSerializer
import pl.swd.app.serializers.ProjectJsonSerializer
import pl.swd.app.serializers.SpreadSheetJsonSerializer
import pl.swd.app.services.DataFileParser.DataFileParserService

@Configuration
open class GsonConfiguration {
    companion object : KLogging()

    @Bean
    open fun gson(
            dataFileParserService: DataFileParserService
    ): Gson = FxGson.coreBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Project::class.java, ProjectJsonSerializer)
            .registerTypeAdapter(SpreadSheet::class.java, SpreadSheetJsonSerializer)
            .registerTypeAdapter(DataTable::class.java, DataTableJsonSerializer(dataFileParserService))
            .create()
}

