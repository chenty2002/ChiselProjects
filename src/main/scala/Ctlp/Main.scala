package Ctlp

import chisel3.util._
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.random.FibonacciLFSR


object State extends ChiselEnum {
  val THINKING, READING, EATING, HUNGRY = Value
}

class Diners extends Module {
  val io = IO(new Bundle() {
    val s0 = Output(State())
    val s1 = Output(State())
    val s2 = Output(State())
    val str = Output(Bool())
  })
  import State._

  val ph0 = Module(new Philosopher)
  val ph1 = Module(new Philosopher)
  val ph2 = Module(new Philosopher)
  val str = Module(new Starvation)

  ph0.io.left := io.s1
  ph0.io.right := io.s2
  ph0.io.init := EATING
  io.s0 := ph0.io.out

  ph1.io.left := io.s2
  ph1.io.right := io.s0
  ph1.io.init := READING
  io.s1 := ph1.io.out

  ph2.io.left := io.s0
  ph2.io.right := io.s1
  ph2.io.init := HUNGRY
  io.s2 := ph2.io.out

  str.io.starv := io.s0
  io.str := str.io.starving
}

class Philosopher extends Module {
  val io = IO(new Bundle {
    val left = Input(State())
    val right = Input(State())
    val init = Input(State())
    val out = Output(State())
  })
  val left = WireDefault(io.left)
  val right = WireDefault(io.right)
  val init = WireDefault(io.init)
  val state = RegInit(io.init)
  val r0_state = Wire(State())
  val r1_state = Wire(State())

  r0_state := State.apply(FibonacciLFSR.maxPeriod(2))
  r1_state := State.apply(FibonacciLFSR.maxPeriod(2))

  io.out := state

  switch(state) {
    is(State.READING) {
      when(left === State.THINKING) {
        state := State.THINKING
      }
    }
    is(State.THINKING) {
      when(right === State.READING) {
        state := State.READING
      }.otherwise {
        state := r0_state
      }
    }
    is(State.EATING) {
      state := r1_state
    }
    is(State.HUNGRY) {
      when(left =/= State.EATING && right =/= State.HUNGRY && right =/= State.EATING) {
        state := State.EATING
      }
    }
  }
}

class Starvation extends Module {
  val io = IO(new Bundle() {
    val starv = Input(State())
    val starving = Output(Bool())
  })
  import State._

  val state = RegInit(false.B)
  io.starving := state

  switch(state) {
    is(false.B) {
      when(io.starv === HUNGRY) {
        state := true.B
      }
    }
    is(true.B) {
      when(io.starv === THINKING) {
        state := false.B
      }
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(
    new Diners(), Array("--target-dir", "generated")
  )
}
