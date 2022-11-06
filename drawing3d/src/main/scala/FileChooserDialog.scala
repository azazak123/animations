import javafx.embed.swing.JFXPanel
import scalafx.application.Platform
import scalafx.stage.{FileChooser, Stage}

import java.io.File
import java.util.concurrent.{Callable, FutureTask}


object FileChooserDialog {
  def apply(): File = {
    //initialize JavaFX
    new JFXPanel()

    //create task
    val getFile = new FutureTask(new Callable[File]() {
      override def call(): File = new FileChooser().showOpenDialog(new Stage)
    })

    // Create a file dialog and display it
    Platform.runLater(getFile)

    // get file and return it
    while (!getFile.isDone) {}
    getFile.get()
  }


}