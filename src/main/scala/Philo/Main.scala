package Philo

import chisel3._
import chisel3.util._
import chisel3.util.random.FibonacciLFSR
import chiselFv._

object State extends ChiselEnum {
  val THINKING, READING, EATING, HUNGRY = Value
}

class Philo4 extends Module with Formal {
  val io = IO(new Bundle() {
    val st0 = Output(State())
    val st1 = Output(State())
    val st2 = Output(State())
    val st3 = Output(State())
  })

  val ph0 = Module(new Philosopher)
  val ph1 = Module(new Philosopher)
  val ph2 = Module(new Philosopher)
  val ph3 = Module(new Philosopher)

  io.st0 := ph0.io.out
  io.st1 := ph1.io.out
  io.st2 := ph2.io.out
  io.st3 := ph3.io.out

  ph0.io.left := io.st1
  ph0.io.right := io.st3
  ph0.io.init := State.READING

  ph1.io.left := io.st2
  ph1.io.right := io.st0
  ph1.io.init := State.THINKING

  ph2.io.left := io.st3
  ph2.io.right := io.st1
  ph2.io.init := State.THINKING

  ph3.io.left := io.st0
  ph3.io.right := io.st2
  ph3.io.init := State.THINKING

  assert(!(ph0.io.out === State.EATING && ph1.io.out === State.EATING))
  assert(!(ph1.io.out === State.EATING && ph2.io.out === State.EATING))
  assert(!(ph2.io.out === State.EATING && ph3.io.out === State.EATING))
  assert(!(ph3.io.out === State.EATING && ph0.io.out === State.EATING))
  assert(!Seq(ph0, ph1, ph2, ph3).map(_.io.out === State.HUNGRY).reduce(_ && _)) // FAIL
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
  val coin = Reg(Bool())

  val self = RegInit(io.init)

  io.out := self
  coin := FibonacciLFSR.maxPeriod(8)(0)

  switch(self) {
    is(State.READING) {
      when(left === State.THINKING) {
        self := State.THINKING
      }
    }
    is(State.THINKING) {
      when(coin && right === State.READING) {
        self := State.READING
      }.otherwise {
        self := Mux(coin, State.THINKING, State.HUNGRY)
      }
    }
    is(State.EATING) {
      self := Mux(coin, State.THINKING, State.EATING)
    }
    is(State.HUNGRY) {
      when(left =/= State.EATING && right =/= State.HUNGRY && right =/= State.EATING) {
        self := State.EATING
      }
    }
  }
}

object Main extends App {
  println("-------------- Main Starts --------------")
  Check.bmc(() => new Philo4, 50)
  emitVerilog(new Philosopher(), Array("--target-dir", "generated"))
}