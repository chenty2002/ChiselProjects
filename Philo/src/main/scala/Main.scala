import State._
import chisel3._
import chisel3.util._

import scala.util.Random

class Philo4 extends Module {
  val io = IO(new Bundle() {
    val st0 = Output(UInt(2.W))
    val st1 = Output(UInt(2.W))
    val st2 = Output(UInt(2.W))
    val st3 = Output(UInt(2.W))
  })

//  val st = Wire(Vec(4, UInt(2.W)))

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
  ph0.io.init := reading

  ph1.io.left := io.st2
  ph1.io.right := io.st0
  ph1.io.init := thinking

  ph2.io.left := io.st3
  ph2.io.right := io.st1
  ph2.io.init := thinking

  ph3.io.left := io.st0
  ph3.io.right := io.st2
  ph3.io.init := thinking
}

class Philosopher extends Module {
  val io = IO(new Bundle {
    val left = Input(UInt(2.W))
    val right = Input(UInt(2.W))
    val init = Input(UInt(2.W))
    val out = Output(UInt(2.W))
  })
  val left = WireDefault(io.left)
  val right = WireDefault(io.right)

  val self = Reg(UInt(2.W))

  val coin = RegInit(Random.nextBoolean().asBool)
  coin := !coin

  when(reset.asBool) {
    self := io.init
  }

  io.out := self

  switch(self) {
    is(reading) {
      when(left === thinking) {
        self := thinking
      }
    }
    is(thinking) {
      when(coin && right === reading) {
        self := reading
      }.otherwise {
        self := Mux(coin, thinking, hungry)
      }
    }
    is(eating) {
      self := Mux(coin, thinking, eating)
    }
    is(hungry) {
      when(left =/= eating && right =/= hungry && right =/= eating) {
        self := eating
      }
    }
  }
}

object Main extends App {
  println("-------------- Main Starts --------------")
  emitVerilog(new Philosopher(), Array("--target-dir", "generated"))
}