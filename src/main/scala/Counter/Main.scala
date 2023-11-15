package Counter

import chisel3._
import chiselFv._

class Counter extends Module with Formal {
  val io = IO(new Bundle() {
    val out = Output(Bool())
  })

  val bit0 = Module(new CounterCell)
  val bit1 = Module(new CounterCell)
  val bit2 = Module(new CounterCell)

  bit0.io.carry_in := true.B
  bit1.io.carry_in := bit0.io.carry_out
  bit2.io.carry_in := bit1.io.carry_out
  io.out := bit2.io.carry_out

  assertNextStepWhen(io.out, !io.out)
  for(i <- 2 until 8) {
    assertAfterNStepWhen(io.out, i, !io.out)
  }
  assertAfterNStepWhen(io.out, 8, io.out)
}

class CounterCell extends Module {
  val io = IO(new Bundle() {
    val carry_in = Input(Bool())
    val carry_out = Output(Bool())
  })

  val value = RegInit(false.B)

  io.carry_out := value & io.carry_in

  when(value) {
    value := !io.carry_in
  }.otherwise {
    value := io.carry_in
  }
}

object Main extends App {
  println("-------------- Main Starts --------------")
  Check.bmc(() => new Counter(), 50)
  emitVerilog(new Counter(), Array("--target-dir", "generated"))
}