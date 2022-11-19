import org.lwjgl.glfw.GLFW
import slack3d.algebra.{Matrix3, Vector3}
import slack3d.graphics.Slack3D.State
import slack3d.graphics.colour.Colour
import slack3d.graphics.shape._
import slack3d.graphics.shape.line.{Line, LineOrRay}
import spire.std.any.DoubleAlgebra

import java.net.URI
import scala.io.Source

object Model {

  /** Open file chooser dialog */
  private def chooseFile(): URI = {
    FileChooserDialog().toURI
  }

  /** Create model from file */
  def apply(colour: Colour = Colour.next(), showAxis: Boolean = false): Model = {
    val fileName = chooseFile()

    // load file
    val source = Source.fromURI(fileName)
    val iterator = source.getLines()

    var shapes = new Array[Shape](0)

    // parse file
    iterator foreach {
      string =>
        val param = string.split(" ").drop(1)
        shapes :+= {
          string.split(" ")(0) match {
            case "Sphere" =>
              Sphere(param(0).toDouble, Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble), colour)
            case "Text" =>
              Text(param(0), Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble), colour = colour)
            case "Box" =>
              Box(param(0).toDouble, param(1).toDouble, param(2).toDouble, colour) +
                Vector3(param(3).toDouble, param(4).toDouble, param(5).toDouble)
            case "Cylinder" =>
              Cylinder(radius = param(0).toDouble, colour = colour)
                .map(_ + Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble))
          }
        }
    }

    //close file
    source.close()

    new Model(shapes, colour, Matrix3.identity(), showAxis,
      Array(Vector3(1, 0, 0), Vector3(0, 1, 0), Vector3(0, 0, 1)))
  }

}

/** Model class which supports rotation and scaling */
case class Model(shapes: Array[Shape], colour: Colour,
                 rot: Matrix3[Double], showAxis: Boolean, axis: Array[Vector3[Double]]) extends Shape {

  override type Self = Model

  /** Process user inputs */
  def control(state: State): Model = {
    var model = Model(shapes, colour, rot, showAxis, axis)

    // rotate
    var rotator: Matrix3[Double] = Matrix3.identity()

    if (state.window.keyPressed(GLFW.GLFW_KEY_X)) {
      rotator *= Matrix3.rotatorX(Math.toRadians(0.5))
    }
    if (state.window.keyPressed(GLFW.GLFW_KEY_Y)) {
      rotator *= Matrix3.rotatorY(Math.toRadians(0.5))
    }
    if (state.window.keyPressed(GLFW.GLFW_KEY_Z)) {
      rotator *= Matrix3.rotatorZ(Math.toRadians(0.5))
    }
    model = rotator * Model(model.shapes, model.colour, rotator * model.rot, showAxis, model.axis)

    // scale
    if (state.window.keyPressed(GLFW.GLFW_KEY_C)) {
      model = model.*(0.99)
    }
    if (state.window.keyPressed(GLFW.GLFW_KEY_V)) {
      model = model.*(1.01)
    }

    if (state.window.keyPressed(GLFW.GLFW_KEY_A)) {
      model = model.scaleX(1.01)
    }
    if (state.window.keyPressed(GLFW.GLFW_KEY_S)) {
      model = model.scaleX(0.99)
    }

    if (state.window.keyPressed(GLFW.GLFW_KEY_D)) {
      model = model.scaleY(1.01)
    }
    if (state.window.keyPressed(GLFW.GLFW_KEY_F)) {
      model = model.scaleY(0.99)
    }

    if (state.window.keyPressed(GLFW.GLFW_KEY_G)) {
      model = model.scaleZ(1.01)
    }
    if (state.window.keyPressed(GLFW.GLFW_KEY_H)) {
      model = model.scaleZ(0.99)
    }

    model
  }

  /** Scale in Ox direction */
  def scaleX(scale: Double): Self = {
    Model(shapes.map {
      case b@Box(_, _, _, _, _, _) =>
        rot * Box(scale * b.width() / 2, b.height() / 2, b.depth() / 2, colour) + b.center() +
          (scale - 1) * b.center().project(axis(0))
      case Text(text) =>
        rot * Text(text.map(_ * scale))
      case Cylinder(b, _, _) =>
        rot * Cylinder(radius = b.radius() * scale, colour = colour)
      case s: Sphere =>
        rot * Sphere(s.radius * scale, s.center, colour)
    }, colour, rot, showAxis, axis)

  }

  /** Scale in Oy direction */
  def scaleY(scale: Double): Self = {
    Model(shapes.map {
      case b@Box(_, _, _, _, _, _) =>
        rot * Box(b.width() / 2, scale * b.height() / 2, b.depth() / 2, colour) + b.center() +
          (scale - 1) * b.center().project(axis(1))
      case Text(text) =>
        rot * Text(text.map(_ * scale))
      case Cylinder(b, _, _) =>
        rot * Cylinder(radius = b.radius() * scale, colour = colour)
      case s: Sphere =>
        rot * Sphere(s.radius * scale, s.center, colour)
    }, colour, rot, showAxis, axis)

  }

  /** Scale in Oz direction */
  def scaleZ(scale: Double): Self = {
    Model(shapes.map {
      case b@Box(_, _, _, _, _, _) =>
        rot * Box(b.width() / 2, b.height() / 2, scale * b.depth() / 2, colour) + b.center() +
          (scale - 1) * b.center().project(axis(2))
      case Text(text) =>
        rot * Text(text.map(_ * scale))
      case Cylinder(b, _, _) =>
        rot * Cylinder(radius = b.radius() * scale, colour = colour)
      case s: Sphere =>
        rot * Sphere(s.radius * scale, s.center, colour)
    }, colour, rot, showAxis, axis)
  }

  override def map(f: Vector3[Double] => Vector3[Double]): Self =
    Model(shapes.map(_.map(f)), colour, rot, showAxis, axis.map(f).map(_.normalise()))

  override def buildMesh(mesh: Mesh[Point, LineOrRay, Triangle]): Unit = {
    shapes.foreach(_.buildMesh(mesh))
    if (showAxis) {
      Line(axis(0), Colour.Red).buildMesh(mesh)
      Line(axis(1), Colour.Blue).buildMesh(mesh)
      Line(axis(2), Colour.Green).buildMesh(mesh)
      Line(Vector3(1.0, 0, 0), Colour.Red).buildMesh(mesh)
      Line(Vector3(0, 1.0, 0), Colour.Blue).buildMesh(mesh)
      Line(Vector3(0, 0, 1.0), Colour.Green).buildMesh(mesh)
    }
  }

}