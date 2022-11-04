import org.lwjgl.glfw.GLFW
import slack3d.algebra.Vector3
import slack3d.graphics.Slack3D
import slack3d.graphics.camera.Camera
import slack3d.graphics.colour.Colour
import slack3d.graphics.light.{Light, LightProjector}

object Main extends App {

  val window = Slack3D("A sphere",
    light = Some(Light(Colour.White, Vector3(0.0, 5, 0))(LightProjector.PhongLightProjector)),
    camera = Some(new Camera(speed = 0, sensitivity = 0) {
      override def mouseMoved(xPos: Double, yPos: Double): Unit = {}
    }))

  var model = Model()

  window foreach {
    _ =>
      if (window.window.keyPressed(GLFW.GLFW_KEY_R)) {
        model = Model()
      }
      Seq(model)
  }
}
