import chisel3._
import chisel3.util._

class Cex extends Module {
  val io = IO(new Bundle() {
    val i = Input(Bool())
    val p = Output(Bool())
    val q = Output(Bool())
  })

  val state = RegInit(0.U(2.W))

  when(reset.asBool) {
    state := 0.U
  }

  switch(state) {
    is(0.U) {
      state := 1.U
    }
    is(1.U) {
      state := Mux(io.i, 2.U, 0.U)
    }
    is(2.U) {
      state := Mux(io.i, 3.U, 2.U)
    }
    is(3.U) {
      state := 3.U
    }
  }

  io.p := state === 1.U
  io.q := state === 2.U
}

object Main extends App {
  println("-------------- Main Starts --------------")
  emitVerilog(new Cex(), Array("--target-dir", "generated"))
}