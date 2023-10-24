package Gcd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class GcdTest extends AnyFlatSpec with ChiselScalatestTester {
  "GCD" should "pass" in {
    test(new Gcd()) { gcd =>
      gcd.io.start.poke(true.B)
      gcd.io.a.poke(30.U)
      gcd.io.b.poke(24.U)
      gcd.clock.step()

      while (gcd.io.busy.peekInt() == 1)
        gcd.clock.step()

      gcd.io.o.expect(6.U)

      gcd.io.start.poke(true.B)
      gcd.io.a.poke(13.U)
      gcd.io.b.poke(27.U)
      gcd.clock.step()

      while (gcd.io.busy.peekInt() == 1)
        gcd.clock.step()

      gcd.io.o.expect(1.U)

      gcd.io.start.poke(true.B)
      gcd.io.a.poke(20.U)
      gcd.io.b.poke(20.U)
      gcd.clock.step()

      while (gcd.io.busy.peekInt() == 1)
        gcd.clock.step()

      gcd.io.o.expect(20.U)
    }
  }
}
