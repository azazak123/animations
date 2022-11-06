import slack3d.algebra.Vector3
import slack3d.graphics.colour.Colour
import slack3d.graphics.shape._
import slack3d.graphics.shape.line.LineOrRay

import java.net.URI
import scala.io.Source

object Model {

  //open file chooser dialog
  private def chooseFile(): URI = {
    FileChooserDialog().toURI
  }

  //model constructor
  def apply(colour: Colour = Colour.next()): Model = {
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
              Sphere(param(0).toDouble, Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble))
            case "Text" =>
              Text(param(0), Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble))
            case "Box" =>
              Box(param(0).toDouble, param(1).toDouble, param(2).toDouble)
                .map(_ + Vector3(param(3).toDouble, param(4).toDouble, param(5).toDouble))
            case "Cylinder" =>
              Cylinder(radius = param(0).toDouble)
                .map(_ + Vector3(param(1).toDouble, param(2).toDouble, param(3).toDouble))
          }
        }
    }

    //close file
    source.close()

    new Model(shapes, colour)
  }
}

// implement Shape trait for Model
case class Model(shapes: Array[Shape], colour: Colour) extends Shape {
  override type Self = Model

  override def map(f: Vector3[Double] => Vector3[Double]): Self =
    Model(shapes.map(_.map(f)), colour)

  override def buildMesh(mesh: Mesh[Point, LineOrRay, Triangle]): Unit =
    shapes.foreach(_.buildMesh(mesh))
}