package sample

import com.tinify.Tinify
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.*
import java.io.File
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Alert
import java.util.prefs.Preferences
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.reflect.jvm.jvmName


class Controller {
    private val mPrefs = Preferences.userRoot().node(this::class.jvmName)
    private val PREF_API_KEY = "PREF_API_KEY"
    private val PREF_PATH_KEY = "PREF_PATH_KEY"

    @FXML
    lateinit var choosePathBtn: Button

    @FXML
    lateinit var compressBtn: Button

    @FXML
    lateinit var apiKeyField: TextField

    @FXML
    lateinit var pathLabel: Label

    fun initialize() {
        apiKeyField.text = mPrefs.get(PREF_API_KEY, "")
        val path = mPrefs.get(PREF_PATH_KEY, null)

        if (path != null) {
            pathLabel.text = path
            pathLabel.isVisible = true
        }
    }

    @FXML
    fun onClickChoosePath() {
        val directoryChooser = DirectoryChooser()
        configuringDirectoryChooser(directoryChooser)
        val dir = directoryChooser.showDialog(Main.getPrimaryStage())
        if (dir != null) {
            pathLabel.text = dir.absolutePath
            pathLabel.isVisible = true
        } else {
            pathLabel.text = null
        }
    }

    @FXML
    fun onClickCompress() {
        if (apiKeyField.text.isNullOrEmpty()) {
            showAlert(AlertType.WARNING, "API Key field must not be empty")
            return
        }

        Tinify.setKey(apiKeyField.text)
        mPrefs.put(PREF_API_KEY, apiKeyField.text)

        if (pathLabel.text.isEmpty()) {
            showAlert(AlertType.WARNING, "Folder path must be specified")
            return
        }

        mPrefs.put(PREF_PATH_KEY, pathLabel.text)

        val processDialog = showAlert(AlertType.NONE, "Search for images recursively...")
        Thread {
            try {

                val imgList = getImagesRecursively(File(pathLabel.text))
                Platform.runLater { processDialog.contentText = "Start compressing ${imgList.size} images" }


                if (imgList.isEmpty()) {
                    Platform.runLater {
                        processDialog.alertType = AlertType.INFORMATION
                        processDialog.close()
                        showAlert(AlertType.WARNING, "No images found in folder ${pathLabel.text}")
                    }
                    return@Thread
                }

                imgList.forEach {
                    Tinify.fromFile(it.absolutePath).toFile(it.absolutePath)
                    Platform.runLater { processDialog.contentText = "Compressed ${imgList.indexOf(it) +1} images of ${imgList.size}" }
                }

                Platform.runLater {
                    processDialog.alertType = AlertType.INFORMATION
                    processDialog.close()
                    showAlert(AlertType.INFORMATION, "All images compressed")
                }
            } catch (e: java.lang.Exception) {
                Platform.runLater {
                    processDialog.alertType = AlertType.INFORMATION
                    processDialog.close()
                    showAlert(AlertType.ERROR, e.message?: "UNKNOWN ERROR")
                }
                e.printStackTrace()
            }
        }.start()
    }

    private fun showAlert(type: AlertType, text: String): Alert {
        val dialog = Alert(type)
        dialog.contentText = text
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.show()
        dialog.setOnCloseRequest { dialog.close() }
        return dialog
    }

    private fun getImagesRecursively(file: File, existedList: ArrayList<File> = ArrayList()): List<File> {
        return when {
            file.isDirectory -> {
                file.listFiles().forEach { getImagesRecursively(it, existedList) }
                existedList
            }
            ImageIO.read(file) == null -> existedList
            else -> {
                existedList.add(file)
                existedList
            }
        }
    }

    private fun configuringDirectoryChooser(directoryChooser: DirectoryChooser) {
        directoryChooser.title = "Select directory with images"
        directoryChooser.initialDirectory = if (pathLabel.text.isNullOrEmpty())
            File(System.getProperty("user.home"))
        else
            File(pathLabel.text)
    }
}
