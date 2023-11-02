package El2Omega

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class El2Omega(ROWMSB: Int) extends Module {
  val ROWBITS = ROWMSB + 1
  val COLMSB = ROWMSB + 2
  val COLBITS = COLMSB + 1
  val DIGMSB = 1

  val io = IO(new Bundle() {
    val pause = Input(Bool())
    val rchoice = Input(UInt((ROWMSB+1).W))
    val cchoice = Input(UInt((COLMSB+1).W))
    val dchoice = Input(UInt((DIGMSB+1).W))
    val colmsb = Output(UInt(1.W))
    val collsb = Output(UInt(1.W))
  })
  val row = RegInit(io.rchoice)
  val col = RegInit(0.U((COLMSB+1).W))
  val digit = RegInit(0.U((DIGMSB+1).W))

  io.colmsb := col(COLMSB)
  io.collsb := col(0)

  when(col + Cat(0.U(1.W), row, 0.U(1.W)) =/= Fill(COLBITS, 1.U(1.W))) {
    when(col <= (1 << COLBITS - 1).U) {
      when(io.cchoice > col && io.cchoice <= (1 << COLBITS).U) {
        col := io.cchoice
      }.otherwise {
        col := col+1.U
      }
    }.otherwise {
      when(col(0) === 0.U || io.pause === 0.U) {
        when(io.cchoice > col &&
          Cat(0.U(1.W), io.cchoice) + Cat(0.U(2.W), row, 0.U(1.W)) <= (1<<(COLBITS+1)-1).U) {
          col := io.cchoice
        }.otherwise {
          col := col + 1.U
        }
        when(col(0) === 1.U) {
          digit := io.dchoice
        }.otherwise {
          digit := 0.U
        }
      }
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(new El2Omega(1), Array("--target-dir", "generated"))
}
