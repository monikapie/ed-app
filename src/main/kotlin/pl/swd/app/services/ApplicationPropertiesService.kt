package pl.swd.app.services;

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * This class stores application properties stored in 'application.properties' file.
 * Property values are automatically assigned by Spring.
 */
@Service
class ApplicationPropertiesService {
    @Value("\${configFileName}")
    lateinit var configFileName: String

    @Value("\${projectFileExtension}")
    lateinit var projectFileExtension: String
}