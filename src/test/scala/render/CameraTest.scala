import org.scalatest._
import org.scalautils._
import TripleEquals._
import Tolerance._

import scala.math._
import org.lwjgl.util.vector.Vector3f

import com.puffin.Common._
import com.puffin.render.Camera

class CameraTest extends FlatSpec with Matchers {
  val err = 0.001f
  implicit val vectorEq = 
    new Equality[Vector3f] {
      def areEqual(v1: Vector3f, o: Any): Boolean =
        o match {
          case v2: Vector3f => v1.x === v2.x +- err && v1.y === v2.y +- err && v1.z === v2.z +- err
          case _ => false
        }
    }
  
  val cam = new Camera()
  val expected = new Vector3f()
  "Camera" should "move correctly" in {
    expected.set(0, 0, -1)
    cam.pos should equal (expected)

    cam.moveForward(7)
    expected.set(0, 0, 6)
    cam.pos should equal (expected)
    
    cam.moveForward(-3)
    expected.set(0, 0, 3)
    cam.pos should equal (expected)

    cam.moveLateral(4.5f)
    expected.set(4.5f, 0, 3)
    cam.pos should equal (expected)

    cam.moveLateral(-0.5f)
    expected.set(4, 0, 3)
    cam.pos should equal (expected)

    expected.set(1, 1, 1)
    cam.moveTo(expected)
    cam.pos should equal (expected)

    cam.moveBy(new Vector3f(1, 1, 1))
    expected.set(2, 2, 2)
    cam.pos should equal (expected)
  }

  it should "look at things correctly" in {
    cam.moveTo(new Vector3f(0, 0, 0))

    expected.set(0, 0, 1)
    cam.dir should equal (expected)

    cam.lookAt(new Vector3f(1, 1, 1))
    expected.set(1, 1, 1)
    expected.normalise(expected)
    cam.dir should equal (expected)

    cam.lookAt(new Vector3f(3, 3, 3))
    expected.set(1, 1, 1)
    expected.normalise(expected)
    cam.dir should equal (expected)

    cam.moveTo(new Vector3f(-1, 2, 3))
    cam.lookAt(new Vector3f(1, 2, 3))
    expected.set(1, 0, 0)
    expected.normalise(expected)
    cam.dir should equal (expected)
  }

  it should "look around correctly" in {
    cam.moveTo(new Vector3f(0, 0, 0))
    cam.lookAt(new Vector3f(0, 0, 1))

    cam.lookLng(toRadiansF(45))
    expected.set(0, 1, 1)
    expected.normalise(expected)
    cam.dir should equal (expected)

    cam.lookLat(toRadiansF(-90))
    expected.set(-1, 1, 0)
    expected.normalise(expected)
    cam.dir should equal (expected)

    cam.lookLng(toRadiansF(-55))
    expected.set(-1, tan(toRadians(-10)).toFloat, 0)
    expected.normalise(expected)
    cam.dir should equal (expected)
  }
}
