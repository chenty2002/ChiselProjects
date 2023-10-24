package Cex

import chisel3._
import chisel3.util._

object State extends ChiselEnum {
  val State0, State1, State2, State3 = Value
}

class Cex extends Module {
  val io = IO(new Bundle() {
    val i = Input(Bool())
    val p = Output(Bool())
    val q = Output(Bool())
  })
  import State._

  val state = RegInit(State0)

  switch(state) {
    is(State0) {
      state := State1
    }
    is(State1) {
      state := Mux(io.i, State2, State0)
    }
    is(State2) {
      state := Mux(io.i, State3, State2)
    }
    is(State3) {
      state := State3
    }
  }

  io.p := state === State1
  io.q := state === State2
}

object Main extends App {
  println("-------------- Main Starts --------------")
  emitVerilog(new Cex(), Array("--target-dir", "generated"))
}