package Swap

import chisel3._
import chisel3.stage.ChiselStage


class Swap(K: Int, Nm1: Int) extends Module {
  val io = IO(new Bundle() {
    val i = Input(UInt(K.W))
    val x = Output(Vec(Nm1+1, UInt(K.W)))
  })

  val x = RegInit(VecInit((0 to Nm1).map(_.U(K.W))))
  val tmp = RegInit(0.U(K.W))
  val m = Wire(UInt(K.W))
  val p = Wire(UInt(K.W))

  io.x := x

  p := Mux(io.i >= Nm1.U, Nm1.U, io.i)
  m := Mux(p === 0.U, Nm1.U, p - 1.U)
  tmp := x(p)
  x(p) := x(m)
  x(m) := tmp
}

object Main extends App {
  (new ChiselStage).emitVerilog(
    new Swap(3, 7),
    Array("--target-dir", "generated")
  )
}
