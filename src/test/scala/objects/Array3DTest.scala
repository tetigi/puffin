import com.puffin.data.Array3D
import com.puffin.Common.xyzIn
import org.scalatest._

class Array3DTest extends FlatSpec with Matchers {
  val (dX, dY, dZ) = (20, 30, 40)
  var arr = new Array3D[String](dX, dY, dZ)

  "The Array" should "retrieve neighbours properly" in {
    arr.getNeighbours(0, 0, 0).toSet should be (Set((0,0,1), (0,1,0), (1,0,0)))
    arr.getNeighbours(1, 1, 1).toSet should be (Set((0,1,1), (1,0,1), (1,1,0),(2,1,1),(1,2,1),(1,1,2)))
  }

  it should "init correctly" in {
    arr = Array3D.initWith(dX, dY, dZ, { () => new String() })
    for ((x, y, z) <- xyzIn(0, dX, dY, dZ))
      arr.get(x, y, z) should equal ("")
  }
}
