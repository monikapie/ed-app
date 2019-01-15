package pl.swd.app.services;

import io.reactivex.subjects.BehaviorSubject
import org.springframework.stereotype.Service
import pl.swd.app.models.Config

/**
 * Holds current configstate in the app
 */
@Service
class ConfigService {
    /**
     * Observable with optional Config file
     * Cannot be unset, but can be changed
     */
    val currentConfig: BehaviorSubject<Config> = BehaviorSubject.create()

    /**
     * Emits a config as a current config
     */
    fun setCurrentConfig(config: Config) {
        currentConfig.onNext(config)
    }
}