import chisel3._
import chisel3.util._

import scala.util.Random

object State extends ChiselEnum {
  val THINKING, READING, EATING, HUNGRY = Value
}

class Philo4 extends Module {
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

  ph0.io.coin := Random.nextBoolean().asBool
  ph1.io.coin := Random.nextBoolean().asBool
  ph2.io.coin := Random.nextBoolean().asBool
  ph3.io.coin := Random.nextBoolean().asBool

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
}

class Philosopher extends Module {
  val io = IO(new Bundle {
    val left = Input(State())
    val right = Input(State())
    val init = Input(State())
    val coin = Input(Bool())
    val out = Output(State())
  })
  val left = WireDefault(io.left)
  val right = WireDefault(io.right)

  val self = RegInit(io.init)

  io.out := self

  switch(self) {
    is(State.READING) {
      when(left === State.THINKING) {
        self := State.THINKING
      }
    }
    is(State.THINKING) {
      when(io.coin && right === State.READING) {
        self := State.READING
      }.otherwise {
        self := Mux(io.coin, State.THINKING, State.HUNGRY)
      }
    }
    is(State.EATING) {
      self := Mux(io.coin, State.THINKING, State.EATING)
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
  emitVerilog(new Philosopher(), Array("--target-dir", "generated"))
}