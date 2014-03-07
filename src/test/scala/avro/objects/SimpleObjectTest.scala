import org.scalatest._

import com.gensler.scalavro.types.AvroType
import com.gensler.scalavro.io.AvroTypeIO

import com.puffin.objects.Volume
import com.puffin.avro.objects._

class SimpleObjectTest extends FlatSpec with Matchers {
  val volume = new Volume(3, 3, 3)
  volume.put(1, 1, 1, 1)
  volume.put(0, 0, 0, 1)
  volume.put(2, 2, 2, 1)
  val deflatedVol = Volume.deflate(volume)
  "Volumes" should "serialize correctly" in {
    deflatedVol.points.size should be (3) 
    deflatedVol.objectType should be (ObjectType.VOLUME)
  }

  "SimpleObjects" should "serialize and deserialize correctly" in {
    SimpleObjectInflater.deflateObj(volume, Volume, "/tmp/voltest.out") 
    val obj = SimpleObjectInflater.inflateFile("/tmp/voltest.out")
    val data = obj.getData
    data.get(1, 1, 1) should be (1)
    data.get(0, 0, 0) should be (1)
    data.get(2, 2, 2) should be (1)
    data.get(0, 1, 2) should be (0)

    data.dimX should be (3)
    data.dimY should be (3)
    data.dimZ should be (3)
  }
}
