package Arbiter

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class Test extends AnyFlatSpec with ChiselScalatestTester {
  "Arbiter" should "pass" in {
    test(new Main()) { arbiter =>
      for(i <- 0 until 20) {
        println("i = " + i +
          ", ackA = " + arbiter.io.ackA.peekBoolean() +
          ", ackB = " + arbiter.io.ackB.peekBoolean() +
          ", ackC = " + arbiter.io.ackC.peekBoolean())
        arbiter.clock.step()
      }
    }
  }
}
