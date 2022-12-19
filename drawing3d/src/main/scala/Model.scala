import org.lwjgl.glfw.GLFW
import slack3d.algebra.{Matrix3, Vector3}
import slack3d.graphics.Slack3D.State
import slack3d.graphics.colour.Colour
import slack3d.graphics.shape._
import slack3d.graphics.shape.line.{Line, LineOrRay}
import spire.std.any.DoubleAlgebra

import java.net.URI
import java.util.Scanner
import java.util.regex.Pattern
import scala.io.Source
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

object Model {

  /** Open file chooser dialog */
  private def chooseFile(): URI = {
    FileChooserDialog().toURI
  }

  /** Create model from file */
  def apply(
             colour: Colour = Colour.next(),
             showAxis: Boolean = false
           ): Model = {
    val fileName = chooseFile()

    // load file
    val source = Source.fromURI(fileName)
    val iterator = source.getLines()

    var shapes = new Array[Shape](0)

    // parse file
    iterator foreach { string =>
      val param = string.split(" ").drop(1)
      shapes :+= {
        string.split(" ")(0) match {
          case "Sphere" =>
            Sphere(
              param(0).toDouble,
              Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble),
              colour
            )
          case "Text" =>
            Text(
              param(0),
              Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble),
              colour = colour
            )
          case "Box" =>
            Box(
              param(0).toDouble,
              param(1).toDouble,
              param(2).toDouble,
              colour
            ) +
              Vector3(param(3).toDouble, param(4).toDouble, param(5).toDouble)
          case "Cylinder" =>
            Cylinder(radius = param(0).toDouble, colour = colour)
              .map(
                _ + Vector3(
                  param(1).toDouble,
                  param(2).toDouble,
                  param(3).toDouble
                )
              )
        }
      }
    }

    // close file
    source.close()

    Model(
      shapes,
      colour,
      Matrix3.identity(),
      showAxis,
      Array(Vector3(1, 0, 0), Vector3(0, 1, 0), Vector3(0, 0, 1)),
      Vector3.zeroes(),
      None
    )
  }

}

/** Model class which supports rotation and scaling */
case class Model(
                  shapes: Array[Shape],
                  colour: Colour,
                  rot: Matrix3[Double],
                  showAxis: Boolean,
                  axis: Array[Vector3[Double]],
                  center: Vector3[Double],
                  func: Option[(Double => Double, Double => Double, Double, Double)]
                ) extends Shape {

  override type Self = Model

  /** Process user inputs */
  def control(state: State): Model = {
    var model = Model(shapes, colour, rot, showAxis, axis, center, func)

    if (Math.abs(center.y) > 1.2 || Math.abs(center.x) > 1.2) model = model.moveTo(Vector3.zeroes())

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

    model = rotator * Model(
      model.shapes,
      model.colour,
      rotator * model.rot,
      showAxis,
      model.axis,
      model.center,
      func
    )

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

    // move to specific point
    if (state.window.keyPressed(GLFW.GLFW_KEY_P)) {
      val in = new Scanner(System.in)
      val pointPattern =
        Pattern.compile("-?\\d+\\.\\d+,-?\\d+\\.\\d+,-?\\d+\\.\\d+")

      print("Point[Double,Double,Double]:")

      while (!in.hasNext(pointPattern)) {
        in.next()
      }
      val point = in.next(pointPattern).split(",").map(_.toDouble)
      model = model.moveTo(Vector3(point(0), point(1), point(2)))
    }

    // move along the specific trajectory
    if (state.window.keyPressed(GLFW.GLFW_KEY_O)) {
      val in = new Scanner(System.in)
      val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()

      val funcPattern =
        Pattern.compile("y\\(x\\)=.+,z\\(x\\)=.+,-?\\d+\\.\\d+,\\d+\\.\\d+")
      print("Functions, start x, speed [y(x)=[func],z(x)=[func],Double,Double]:")

      while (!in.hasNext(funcPattern)) {
        in.next()
      }

      val args = in.next(funcPattern).split(",")

      val yfStr = "object FunctionWrapper { def apply(x: Double): Double = {" + args(0).split("=")(1) + "}}"
      val zfStr = "object FunctionWrapper { def apply(x: Double): Double = {" + args(1).split("=")(1) + "}}"


      val yfS =
        toolbox.define(toolbox.parse(yfStr).asInstanceOf[toolbox.u.ImplDef])
      val yf = toolbox.eval(q"$yfS.apply _").asInstanceOf[Double => Double]

      val zfS =
        toolbox.define(toolbox.parse(zfStr).asInstanceOf[toolbox.u.ImplDef])
      val zf = toolbox.eval(q"$zfS.apply _").asInstanceOf[Double => Double]

      model = Model(
        model.shapes,
        model.colour,
        model.rot,
        showAxis,
        model.axis,
        model.center,
        Some(yf, zf, args(2).toDouble, args(3).toDouble)
      )
    }

    model.func match {
      case Some((f, g, x, speed)) =>
        model = model.moveTo(Vector3(x, f(x), g(x)))

        if (Math.abs(f(x)) > 1.2 || Math.abs(x) > 1.2)
          model = Model(
            model.shapes,
            model.colour,
            model.rot,
            showAxis,
            model.axis,
            model.center,
            None
          )
        else model = Model(
          model.shapes,
          model.colour,
          model.rot,
          showAxis,
          model.axis,
          model.center,
          Some(f, g, x + speed, speed)
        )

      case None =>
    }

    model
  }

  /** Scale in Ox direction */
  private def scaleX(scale: Double): Self = {
    Model(
      shapes.map {
        case b@Box(_, _, _, _, _, _) =>
          rot * Box(
            scale * b.width() / 2,
            b.height() / 2,
            b.depth() / 2,
            colour
          ) + b.center() +
            (scale - 1) * b.center().-(center).project(axis(0) - center)
        case Text(text) =>
          rot * Text(text.map(_ * scale))
        case Cylinder(b, _, _) =>
          rot * Cylinder(radius = b.radius() * scale, colour = colour)
        case s: Sphere =>
          rot * Sphere(s.radius * scale, s.center, colour)
      },
      colour,
      rot,
      showAxis,
      axis,
      center,
      func
    )
  }

  /** Scale in Oy direction */
  private def scaleY(scale: Double): Self = {
    Model(
      shapes.map {
        case b@Box(_, _, _, _, _, _) =>
          rot * Box(
            b.width() / 2,
            scale * b.height() / 2,
            b.depth() / 2,
            colour
          ) + b.center() +
            (scale - 1) * b.center().-(center).project(axis(1) - center)
        case Text(text) =>
          rot * Text(text.map(_ * scale))
        case Cylinder(b, _, _) =>
          rot * Cylinder(radius = b.radius() * scale, colour = colour)
        case s: Sphere =>
          rot * Sphere(s.radius * scale, s.center, colour)
      },
      colour,
      rot,
      showAxis,
      axis,
      center,
      func
    )
  }

  /** Scale in Oz direction */
  private def scaleZ(scale: Double): Self = {
    Model(
      shapes.map {
        case b@Box(_, _, _, _, _, _) =>
          rot * Box(
            b.width() / 2,
            b.height() / 2,
            scale * b.depth() / 2,
            colour
          ) + b.center() +
            (scale - 1) * b.center().-(center).project(axis(2) - center)
        case Text(text) =>
          rot * Text(text.map(_ * scale))
        case Cylinder(b, _, _) =>
          rot * Cylinder(radius = b.radius() * scale, colour = colour)
        case s: Sphere =>
          rot * Sphere(s.radius * scale, s.center, colour)
      },
      colour,
      rot,
      showAxis,
      axis,
      center,
      func
    )
  }

  /** Move to a specific point */
  private def moveTo(point: Vector3[Double]): Self = {
    Model(
      shapes.map(_.map(_ - center + point)),
      colour,
      rot,
      showAxis,
      axis.map(_ - center + point).map(_ - point).map(_.normalise() + point),
      point,
      func
    )
  }

  override def map(f: Vector3[Double] => Vector3[Double]): Self =
    Model(
      shapes.map(_.map(f)),
      colour,
      rot,
      showAxis,
      axis.map(f).map(_ - f(center)).map(_.normalise() + f(center)),
      f(center),
      func
    )

  override def buildMesh(mesh: Mesh[Point, LineOrRay, Triangle]): Unit = {
    shapes.foreach(_.buildMesh(mesh))
    if (showAxis) {
      Line(center, axis(0), Colour.Red).buildMesh(mesh)
      Line(center, axis(1), Colour.Blue).buildMesh(mesh)
      Line(center, axis(2), Colour.Green).buildMesh(mesh)
      Line(Vector3(1.0, 0, 0), Colour.Red).buildMesh(mesh)
      Line(Vector3(0, 1.0, 0), Colour.Blue).buildMesh(mesh)
      Line(Vector3(0, 0, 1.0), Colour.Green).buildMesh(mesh)
    }
  }

}
