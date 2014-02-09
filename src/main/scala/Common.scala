import scala.math._

object Common {
  def clamp(x: Int, llim: Int) = 
    max(llim, x)

  def clamp(x: Int, llim: Int, ulim: Int) = 
    max(llim, min(ulim, x))

  def clamp(x: Double, llim: Double, ulim: Double) = 
    max(llim, min(ulim, x))
}
