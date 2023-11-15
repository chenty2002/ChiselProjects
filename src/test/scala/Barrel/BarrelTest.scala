package Barrel

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class BarrelTest extends AnyFlatSpec with ChiselScalatestTester {
  "Barrel" should "pass" in {
    test(new Barrel(4, 4)) { barrel => {
      for(i <- 0 until 20) {
        print("i = " + i + ", b = ")
        for(j <- 0 until 4) {
          print(barrel.io.b(j).peekInt() + ", ")
        }
        print("r = ")
        for(j <- 0 until 4) {
          print(barrel.io.r(j).peekInt() + ", ")
        }
        println()
        barrel.clock.step()
      }
    }}
  }
}
