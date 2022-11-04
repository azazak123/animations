import javafx.embed.swing.JFXPanel
import scalafx.application.Platform
import scalafx.stage.{FileChooser, Stage}

import java.io.File
import java.util.concurrent.{Callable, FutureTask}


object FileChooserDialog {
  def apply(): File = {
    //initialize JavaFX
    new JFXPanel()

    val getFile = new FutureTask(new Callable[File]() {
      override def call(): File = new FileChooser().showOpenDialog(new Stage)
    })

    // Create a dialog stage and display it on JavaFX Application Thread
    Platform.runLater(getFile)

    while (!getFile.isDone) {}

    val file = getFile.get()

    //Platform.exit()

    file
  }


}