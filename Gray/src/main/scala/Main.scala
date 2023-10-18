import chisel3._

class Gray extends Module {
  val io = IO(new Bundle() {
    val i = Input(Bool())
    val z = Output(Bool())
  })

  val p = RegInit(false.B)
  val q = RegInit(false.B)
  val r = RegInit(false.B)

  val w = Wire(Bool())

  r := io.z
  q := p
  p := io.i

  w := p ^ q
  io.z := w ^ r
}

object Main extends App {
  println("-------------- Main Starts --------------")
  emitVerilog(new Gray(), Array("--target-dir", "generated"))
}