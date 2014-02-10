import org.scalatest._

import Common._

class CommonTest extends FlatSpec with Matchers {
  
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
}
