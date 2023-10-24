package Philo

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PhiloTest extends AnyFlatSpec with ChiselScalatestTester{
  "Philo" should "pass" in {
    test(new Philo4()) { philo =>
      println("Philo1 state = " + philo.io.st0.expect(State.READING))
      println("Philo2 state = " + philo.io.st1.expect(State.THINKING))
      println("Philo3 state = " + philo.io.st2.expect(State.THINKING))
      println("Philo4 state = " + philo.io.st3.expect(State.THINKING))
    }
  }
}
