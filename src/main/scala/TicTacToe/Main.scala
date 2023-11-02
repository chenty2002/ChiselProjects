package TicTacToe

import chisel3._
import chisel3.stage.ChiselStage

object Content extends ChiselEnum {
  val EMPTY, X, O = Value
}

class TicTacToe extends Module {
  val io = IO(new Bundle() {
    val imove = Input(UInt(4.W))
    val winX = Output(Bool())
    val winO = Output(Bool())
    val finished = Output(Bool())
  })
  import Content._

  val b = RegInit(VecInit.fill(9)(EMPTY))
  val turn = RegInit(X)
  val move = RegInit(0.U(4.W))
  val full = Wire(Bool())

  move := Mux(io.imove < 9.U, io.imove, 0.U)
  when(!io.finished && b(move) === EMPTY) {
    b(move) := turn
    turn := Mux(turn === X, O, X)
  }

  io.winX := b(0) === X && (b(1) === X && b(2) === X || b(3) === X && b(6) === X) ||
    b(8) === X && (b(7) === X && b(6) === X || b(5) === X && b(2) === X) ||
    b(4) === X &&
      (b(0) === X && b(8) === X ||
        b(2) === X && b(6) === X ||
        b(1) === X && b(7) === X ||
        b(3) === X && b(5) === X)
  io.winO := b(0) === O && (b(1) === O && b(2) === O || b(3) === O && b(6) === O) ||
    b(8) === O && (b(7) === O && b(6) === O || b(5) === O && b(2) === O) ||
    b(4) === O &&
      (b(0) === O && b(8) === O ||
        b(2) === O && b(6) === O ||
        b(1) === O && b(7) === O ||
        b(3) === O && b(5) === O)
  full := b.map(_ =/= EMPTY).reduce(_ && _)
  io.finished := io.winX || io.winO || full
}

object Main extends App {
  (new ChiselStage).emitVerilog(new TicTacToe(), Array("--target-dir", "generated"))
}
