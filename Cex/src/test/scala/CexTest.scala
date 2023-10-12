import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CexTest extends AnyFlatSpec with ChiselScalatestTester {
  "Cex" should "pass" in {
    test(new Cex) { cex =>
      cex.io.i.poke(false.B)
      cex.clock.step()
      cex.io.p.expect(true.B)
      cex.io.q.expect(false.B)

      cex.io.i.poke(true.B)
      cex.clock.step()
      cex.io.p.expect(false.B)
      cex.io.q.expect(true.B)

      cex.io.i.poke(false.B)
      cex.clock.step()
      cex.io.p.expect(false.B)
      cex.io.q.expect(true.B)

      cex.io.i.poke(true.B)
      cex.clock.step()
      cex.io.p.expect(false.B)
      cex.io.q.expect(false.B)
    }
  }
}
