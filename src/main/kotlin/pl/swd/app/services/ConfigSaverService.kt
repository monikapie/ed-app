package pl.swd.app.services;

import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.swd.app.exceptions.ConfigDoesNotExistException
import pl.swd.app.models.Config

@Service
class ConfigSaverService {
    companion object : KLogging()

    @Autowired private lateinit var configService: ConfigService
    @Autowired private lateinit var fileIOService: FileIOService
    @Autowired private lateinit var applicationPropertiesService: ApplicationPropertiesService

    /**
     * Saves application configs to a file with a name provided in 'application.properties'
     */
    fun saveToFile() {
        logger.debug { "Saving Config to file: [${applicationPropertiesService.configFileName}]" }
        val config = configService.currentConfig.value.apply {
            if (this == null) {
                throw ConfigDoesNotExistException("Cannot save Config to file, because Config does not exist")
            }
        }

        fileIOService.saveAsJsonToFile(
                data = config,
                fileName = applicationPropertiesService.configFileName
        )
    }

    /**
     * Loads application configs from a file with a name provided in 'application.properties'
     */
    fun loadFromFile() {
        logger.debug { "Loading Config from file: [${applicationPropertiesService.configFileName}]" }
        val config = fileIOService.getAsObjectFromJsonFile<Config>(applicationPropertiesService.configFileName)

        configService.setCurrentConfig(config)
    }

    fun loadDefaultConfig() {
        logger.debug { "Loading default config from memmory" }
        configService.setCurrentConfig(Config())
    }
}