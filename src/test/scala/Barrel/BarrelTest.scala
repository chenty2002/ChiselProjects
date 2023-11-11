package Barrel

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BarrelTest extends AnyFlatSpec with ChiselScalatestTester {
  "Barrel" should "pass" in {
    test(new Barrel(4, 4)) { barrel => {
      barrel.clock.step()
    }}
  }
}
