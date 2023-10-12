import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PhiloTest extends AnyFlatSpec with ChiselScalatestTester{
  "Philo" should "pass" in {
    test(new Philo4()) { philo =>
      for(i <- 1 to 20) {
        println("CLOCK = " + i)
        println("Philo1 state = " + philo.io.st0.peekInt())
        println("Philo2 state = " + philo.io.st1.peekInt())
        println("Philo3 state = " + philo.io.st2.peekInt())
        println("Philo4 state = " + philo.io.st3.peekInt())
        philo.clock.step()
      }
    }
  }
}
