package Counter

import chisel3._

class Counter extends Module {
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
  emitVerilog(new Counter(), Array("--target-dir", "generated"))
}