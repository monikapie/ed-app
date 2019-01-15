package pl.swd.app.services

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import javafx.stage.FileChooser
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tornadofx.*
import java.io.File
import java.io.FileNotFoundException

@Service
class FileIOService {
    companion object : KLogging()

    @Autowired lateinit var gson: Gson
    @Autowired lateinit var appProps: ApplicationPropertiesService

    val textFileExtensions = arrayOf(FileChooser.ExtensionFilter("Text Tile", "*.txt"))
    val projectFileExtensions by lazy { arrayOf(FileChooser.ExtensionFilter("StatApp Project", "*.${appProps.projectFileExtension}")) }
    val csvFileExtension = arrayOf(FileChooser.ExtensionFilter("CSV", "*.csv"))

    /**
     * Opens a new window with an option of choosing a file
     */
    fun openFileDialogObs(title: String = "Open File",
                          fileExtensions: Array<FileChooser.ExtensionFilter> = textFileExtensions,
                          initialDirectory: String = getCurrentDirectory(),
                          mode: FileChooserMode = FileChooserMode.Single): Observable<File> {
        return openFileDialog(title, fileExtensions, initialDirectory, mode)
                .toObservable()
                .doOnNext { logger.debug { "Selected file: ${it.absolutePath}" } }
    }

    fun openFileDialog(title: String = "Open File",
                       fileExtensions: Array<FileChooser.ExtensionFilter> = textFileExtensions,
                       initialDirectory: String = getCurrentDirectory(),
                       mode: FileChooserMode = FileChooserMode.Single): List<File> {
        return chooseFile(title = title, filters = fileExtensions, mode = mode) {
            /**
             * Checking if the initialDirectory path is a path or a file.
             */
            val potentialDirectory = File(initialDirectory)
            /* When file or directory doesn't exist it sets a default directory  */
            if (!potentialDirectory.exists()) {
                logger.debug { "Initial directory: [$initialDirectory] doesn't exist. Setting a current directory." }
                this.initialDirectory = File(getCurrentDirectory())
            }
            /* When the potentialDirectory is a file it gets a file name and a directory */
            else if (potentialDirectory.isFile()) {
                logger.debug { "Initial directory: [$initialDirectory] is a file. Stripping its name and parent directory." }
                this.initialFileName = potentialDirectory.name
                this.initialDirectory = potentialDirectory.parentFile.let {
                    /* When providing single file name it treats it as a file in terms of current directory */
                    if (it === null) {
                        return@let File(getCurrentDirectory())
                    }
                    it
                }
            }
            /* When the potentialDirectory is a directory it sets it as initialDirectory */
            else {
                logger.debug { "Initial directory: [$initialDirectory] is a directory. Setting." }
                this.initialDirectory = potentialDirectory
            }
        }
    }

    /**
     * Saves an object in JSON format to a file
     * If a file exists it overrides its content
     */
    fun <T> saveAsJsonToFile(data: T, fileName: String) {
        val jsonData = gson.toJson(data)

        File(fileName).apply {
            writeText(jsonData)
        }
    }

    fun saveAsAnyToFile(data: List<String>, fileName: String) {
        File(fileName).apply {
            writeText(data.joinToString("\n"))
        }
    }

    /**
     * Gets an object from a file with JSON format
     */
    inline fun <reified T> getAsObjectFromJsonFile(fileName: String): T {
        val file = File(fileName).apply {
            if (!exists()) {
                throw FileNotFoundException("File $fileName does not exist")
            }

            if (isDirectory()) {
                throw FileNotFoundException("File $fileName is a directory")
            }
        }

        val jsonData = file.readText()

        return gson.fromJson(jsonData, T::class.java)
    }

    /**
     * Gets a current directory
     */
    fun getCurrentDirectory() = System.getProperty("user.dir")
}