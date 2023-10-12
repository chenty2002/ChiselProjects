import chisel3._

// THINKING: 00, READING: 01, EATING: 10, HUNGRY: 11
object State {
  val thinking = 0.U(2.W)
  val reading = 1.U(2.W)
  val eating = 2.U(2.W)
  val hungry = 3.U(2.W)
}