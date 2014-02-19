import org.scalatest._
import org.lwjgl.util.vector.Vector3f
import org.scalautils._
import TripleEquals._
import Tolerance._
import scala.collection.mutable.ListBuffer  

import com.puffin.Common._

class CommonTest extends FlatSpec with Matchers {
  val err = 0.001f
  implicit val vectorEq = 
    new Equality[Vector3f] {
      def areEqual(v1: Vector3f, o: Any): Boolean =
        o match {
          case v2: Vector3f => v1.x === v2.x +- err && v1.y === v2.y +- err && v1.z === v2.z +- err
          case _ => false
        }
    }
  
  "Clamp" should "restrict values above it's limits" in {
    clamp(0, 10) should be (0)
    clamp(10, 10) should be (10)
    clamp(11, 10) should be (10)
  }

  it should "restrict values in a range" in {
    clamp(-1, 0, 10) should be (0)
    clamp(5, 0, 10) should be (5)
    clamp(1000, 0, 10) should be (10)
  }

  val buff: ListBuffer[Int] = new ListBuffer()
  val expected: ListBuffer[Int] = new ListBuffer()
  buff.appendAll(List(1,2,3,4))

  "repeatEachElem" should "repeat each element n times" in {
    expected.appendAll(List(1,1,1,2,2,2,3,3,3,4,4,4))
    repeatEachElem(buff, 3) should equal (expected)
  }
  
  it should "work for n = 0" in {
    expected.clear()
    repeatEachElem(buff, 0) should equal (expected)
  }
}
