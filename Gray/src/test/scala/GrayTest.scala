import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class GrayTest extends AnyFlatSpec with ChiselScalatestTester {
  "Gray" should "pass" in {
    test(new Gray()) { gray =>
      var v = "0000"
      for(i <- v.toCharArray) {
        val b = i-'0'
        gray.io.i.poke(b.B)
        gray.clock.step()
        println("Input i = " + b + ", Output z = " + gray.io.z.peekBoolean())
      }
    }
  }
}
