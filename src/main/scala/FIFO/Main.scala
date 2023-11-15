package FIFO

import chisel3._
import chiselFv._

class CompareFIFOs(MSBD: Int, LAST: Int, MSBA: Int) extends Module with Formal {
  val io = IO(new Bundle {
    val dataIn = Input(UInt((MSBD+1).W))
    val push = Input(Bool())
    val pop = Input(Bool())
    val equal = Output(Bool())
  })
  val sr = Module(new srFIFO(MSBD, LAST, MSBA))
  val rb = Module(new rbFIFO(MSBD, LAST, MSBA))
  sr.io.dataIn := io.dataIn
  sr.io.push := io.push
  sr.io.pop := io.pop
  rb.io.dataIn := io.dataIn
  rb.io.push := io.push
  rb.io.pop := io.pop

  io.equal := (sr.io.full === rb.io.full) &&
    (sr.io.empty === rb.io.empty) &&
    (sr.io.empty || sr.io.dataOut === rb.io.dataOut)
  assert(sr.io.full === rb.io.full)
  assert(sr.io.empty === rb.io.empty)
  assert(sr.io.empty || sr.io.dataOut === rb.io.dataOut)
}

class srFIFO(MSBD: Int, LAST: Int, MSBA: Int) extends Module {
  val io = IO(new Bundle() {
    val dataIn = Input(UInt((MSBD + 1).W))
    val push = Input(Bool())
    val pop = Input(Bool())
    val dataOut = Output(UInt((MSBD + 1).W))
    val full = Output(Bool())
    val empty = Output(Bool())
  })
  val mem = RegInit(VecInit.fill(LAST + 1)(0.U((MSBD + 1).W)))
  val tail = RegInit(0.U((MSBD + 1).W))
  val empty = RegInit(true.B)

  when(io.push && !io.full) {
    for (i <- LAST until 0 by -1) {
      mem(i) := mem(i - 1)
    }
    mem(0) := io.dataIn
    when(!empty) {
      tail := tail + 1.U
    }
    empty := false.B
  }.elsewhen(io.pop && !empty) {
    when(tail === 0.U) {
      empty := true.B
    }.otherwise {
      tail := tail - 1.U
    }
  }
  io.dataOut := mem(tail)
  io.full := tail === LAST.U
  io.empty := empty
}


class rbFIFO(MSBD: Int, LAST: Int, MSBA: Int) extends Module {
  val io = IO(new Bundle() {
    val dataIn = Input(UInt((MSBD + 1).W))
    val push = Input(Bool())
    val pop = Input(Bool())
    val dataOut = Output(UInt((MSBD + 1).W))
    val full = Output(Bool())
    val empty = Output(Bool())
  })
  val mem = RegInit(VecInit.fill(LAST + 1)(0.U((MSBD + 1).W)))
  val head = RegInit(0.U((MSBD + 1).W))
  val tail = RegInit(0.U((MSBD + 1).W))
  val empty = RegInit(true.B)

  when(io.push && !io.full) {
    mem(head) := io.dataIn
    head := head + 1.U
    empty := false.B
  }.elsewhen(io.pop && !empty) {
    tail := tail + 1.U
    when(tail === head) {
      empty := true.B
    }
  }
  io.dataOut := mem(tail)
  io.full := tail === head && !empty
  io.empty := empty
}

object Main extends App {
  Check.bmc(() => new CompareFIFOs(3, 15, 3))
  emitVerilog(new CompareFIFOs(3, 15, 3), Array("--target-dir", "generated"))
}
