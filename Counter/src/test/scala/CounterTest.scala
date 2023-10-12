import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CounterTest extends AnyFlatSpec with ChiselScalatestTester {
  "Counter" should "pass" in {
    test(new Counter) { counter =>
      println("out = " + counter.io.out.peekBoolean())
    }
  }
}
