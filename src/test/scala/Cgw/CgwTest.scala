package Cgw

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CgwTest extends AnyFlatSpec with ChiselScalatestTester {
  "Cgw" should "pass" in {
    test(new Cgw()) { cgw =>
      cgw.io.select.poke(Passenger.NONE)
      cgw.clock.step()
      println("safe = " + cgw.io.safe.peekBoolean())
      println("fin = " + cgw.io.fin.peekBoolean())

      cgw.io.select.poke(Passenger.CABBAGE)
      cgw.clock.step()
      println("safe = " + cgw.io.safe.peekBoolean())
      println("fin = " + cgw.io.fin.peekBoolean())

      cgw.io.select.poke(Passenger.GOAT)
      cgw.clock.step()
      println("safe = " + cgw.io.safe.peekBoolean())
      println("fin = " + cgw.io.fin.peekBoolean())

      cgw.io.select.poke(Passenger.WOLF)
      cgw.clock.step()
      println("safe = " + cgw.io.safe.peekBoolean())
      println("fin = " + cgw.io.fin.peekBoolean())
    }
  }
}
