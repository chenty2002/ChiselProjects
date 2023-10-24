package Gcd

import chisel3._
import chisel3.util._

class testGcd(N: Int = 8, logN: Int = 3) extends Module {
  val io = IO(new Bundle {
    // val clock = Input(Clock())
    val x = Input(UInt(N.W))
    val y = Input(UInt(N.W))
    val s = Input(Bool())
  })
  val a = RegInit(0.U(N.W))
  val b = RegInit(0.U(N.W))
  val start = RegInit(0.U(N.W))

  val gcd = Module(new Gcd(N, logN))
  // gcd.io.clock := io.clock
  gcd.io.start := start
  gcd.io.a := a
  gcd.io.b := b

  when(reset.asBool) {
    a := 0.U(N.W)
    b := 0.U(N.W)
    start := false.B
  }.otherwise {
    a := io.x
    b := io.y
    start := io.s
  }
}


class Gcd(N: Int = 8, logN: Int = 3) extends Module {
  val io = IO(new Bundle() {
    // val clock = Input(Clock())
    val start = Input(Bool())
    val a = Input(UInt(N.W))
    val b = Input(UInt(N.W))
    val busy = Output(Bool())
    val o = Output(UInt(N.W))
  })
  val done = Wire(Bool())
  val load = Wire(Bool())
  val xy_lsb = Wire(new Bundle() {
    val x = UInt(1.W)
    val y = UInt(1.W)
  })
  val diff = Wire(UInt(N.W))

  val lsb = RegInit(0.U(logN.W))
  val x = RegInit(0.U(N.W))
  val y = RegInit(0.U(N.W))
  val busy = RegInit(false.B)
  val o = RegInit(0.U(N.W))

  def select(z: UInt, lsb: UInt): UInt = {
     z(lsb)
//    val sel = WireDefault(z(7))
//    switch(lsb) {
//      is("d0".U(3.W)) {
//        sel := z(0)
//      }
//      is("d1".U(3.W)) {
//        sel := z(1)
//      }
//      is("d2".U(3.W)) {
//        sel := z(2)
//      }
//      is("d3".U(3.W)) {
//        sel := z(3)
//      }
//      is("d4".U(4.W)) {
//        sel := z(4)
//      }
//      is("d5".U(3.W)) {
//        sel := z(5)
//      }
//      is("d6".U(3.W)) {
//        sel := z(6)
//      }
//    }
//    sel
  }

  xy_lsb.x := select(x, lsb)
  xy_lsb.y := select(y, lsb)
  diff := Mux(x < y, y - x, x - y)

  when(reset.asBool) {
    busy := false.B
    x := 0.U(N.W)
    y := 0.U(N.W)
    o := 0.U(N.W)
    lsb := 0.U(logN.W)
  }

  done := ((x === y) || (x === 0.U) || (y === 0.U)) && busy

  when(load) {
    x := io.a
    y := io.b
    lsb := 0.U
  }.elsewhen(busy && !done) {
    switch(xy_lsb.asUInt) {
      is("b00".U) {
        lsb := lsb + 1.U
      }
      is("b01".U) {
        x := x >> 1.U
      }
      is("b10".U) {
        y := y >> 1.U
      }
      is("b11".U) {
        when(x < y) {
          y := diff >> 1.U
        }.otherwise {
          x := diff >> 1.U
        }
      }
    }
  }.elsewhen(done) {
    o := Mux(x < y, x, y)
  }

  load := io.start && !busy

  when(!busy) {
    when(io.start) {
      busy := true.B
    }
  }.otherwise {
    when(done) {
      busy := false.B
    }
  }

  io.busy := busy
  io.o := o
}

object Main extends App {
  println("-------------- Main Starts --------------")
  emitVerilog(new Gcd(), Array("--target-dir", "generated"))
}