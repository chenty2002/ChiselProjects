import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MicrowaveTest extends AnyFlatSpec with ChiselScalatestTester {
  "Microwave" should "pass" in {
    test(new Microwave()) { microwave =>
//      microwave.clock.step()
//      microwave.io.closeDoor.poke(false.B)
//      microwave.io.error.expect(true.B)
//      microwave.io.heat.expect(false.B)

      microwave.io.closeDoor.poke(true.B)
      microwave.clock.step()
      microwave.io.error.expect(false.B)
      microwave.io.heat.expect(false.B)

      microwave.io.openDoor.poke(false.B)
      microwave.clock.step()
      microwave.io.heat.expect(false.B)
      microwave.io.error.expect(false.B)

      microwave.clock.step()
      microwave.io.heat.expect(true.B)
      microwave.io.error.expect(false.B)

      microwave.clock.step()
      microwave.io.heat.expect(true.B)
      microwave.io.error.expect(false.B)

      microwave.io.openDoor.poke(false.B)
      microwave.io.done.poke(true.B)
      microwave.clock.step()
      microwave.io.heat.expect(false.B)
      microwave.io.error.expect(false.B)
    }
  }
}
