package SyncArb

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class SyncArb extends Module {
  val io = IO(new Bundle() {
    val req = Input(Vec(16, UInt(1.W)))
    val ack = Output(Vec(16, UInt(1.W)))
  })

  val lreq = RegInit(VecInit.fill(16)(1.U(1.W)))
  val token = Wire(Vec(17, UInt(1.W)))
  val grant = Wire(Vec(17, UInt(1.W)))
  val overRide = Wire(Vec(17, UInt(1.W)))
  val tokenInit = WireDefault(VecInit.fill(16)(1.U(1.W)))

  lreq := io.req
  tokenInit(15) := 1.U
  overRide(16) := 0.U
  grant(0) := ~overRide(0)
  token(0) := token(16)

  val cells = Seq.fill(16)(Module(new Cell))
  for(i <- cells.indices) {
    cells(i).io.req := lreq(i)
    cells(i).io.tokenIn := token(i)
    cells(i).io.tokenInit := tokenInit(i)
    cells(i).io.grantIn := grant(i)
    cells(i).io.overrideIn := overRide(i+1)
    io.ack(i) := cells(i).io.ack
    token(i+1) := cells(i).io.tokenOut
    grant(i+1) := cells(i).io.grantOut
    overRide(i) := cells(i).io.overrideOut
  }
}

class Cell extends Module {
  val io = IO(new Bundle() {
    val req = Input(UInt(1.W))
    val ack = Output(UInt(1.W))
    val tokenIn = Input(UInt(1.W))
    val tokenOut = Output(UInt(1.W))
    val tokenInit = Input(UInt(1.W))
    val grantIn = Input(UInt(1.W))
    val grantOut = Output(UInt(1.W))
    val overrideIn = Input(UInt(1.W))
    val overrideOut = Output(UInt(1.W))
  })

  val token = RegInit(io.tokenInit)
  val waiting = RegInit(0.U(1.W))
  val tw = Wire(UInt(1.W))

  waiting := io.req & (waiting | token)
  token := io.tokenIn
  tw := token & waiting
  io.ack := io.req & (io.grantIn | tw)
  io.tokenOut := token
  io.grantOut := io.grantIn & (~io.req).asUInt
  io.overrideOut := io.overrideIn | tw
}

object Main extends App {
  (new ChiselStage).emitVerilog(new SyncArb(), Array("--target-dir", "generated"))
}
