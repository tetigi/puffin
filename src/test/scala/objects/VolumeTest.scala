import com.puffin.objects.Volume
import org.scalatest._

class TerrainTest extends FlatSpec with Matchers {
  val volSize = 5
  "A Volume" should "be created as zero" in {
    val vol = new Volume(volSize)
    for {
      x <- 0 until volSize
      y <- 0 until volSize
      z <- 0 until volSize
    } vol.get(x,y,z) should be (0)
  }

  it should "get and put data properly" in {
    val vol = new Volume(volSize)
    vol.put(0, 1, 2, 10)
    vol.put(0, 0, 4, 100)
    vol.put(1, 1, 1, -10)

    vol.get(0, 1, 2) should be (10)
    vol.get(0, 0, 4) should be (100)
    vol.get(1, 1, 1) should be (-10)
  }
  
}
