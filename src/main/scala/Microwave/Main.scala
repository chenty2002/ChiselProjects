package Microwave

import chisel3._
import chisel3.util._
import chiselFv._

class Microwave extends Module with Formal {
  val io = IO(new Bundle {
    val openDoor = Input(Bool())
    val closeDoor = Input(Bool())
    val done = Input(Bool())
    val heat = Output(Bool())
    val error = Output(Bool())
  })

  val Start = RegInit(false.B)
  val Close = RegInit(false.B)
  val Heat = RegInit(false.B)
  val Error = RegInit(false.B)

  io.heat := Heat
  io.error := Error

  switch(Cat(Error, Heat, Close, Start)) {
    is("b0000".U) {
      when(io.closeDoor) {
        Close := true.B
      }.otherwise {
        Error := true.B
        Start := true.B
      }
    }
    is("b1001".U) {
      Close := true.B
    }
    is("b1011".U) {
      when(reset.asBool) {
        Error := false.B
        Start := false.B
      }.otherwise {
        Close := false.B
      }
    }
    is("b0010".U) {
      when(io.openDoor) {
        Close := false.B
      }.otherwise {
        Start := true.B
      }
    }
    is("b0011".U) {
      Heat := true.B
    }
    is("b0111".U) {
      Start := false.B
    }
    is("b0110".U) {
      when(io.openDoor) {
        Heat := false.B
        Close := false.B
      }.elsewhen(io.done) {
        Heat := false.B
      }
    }
  }
// 0000 - 1001 - 1011 - 1001
//      |             - 0000
//      - 0010 - 0000
//             - 0011 - 0111 - 0110 - 0000
//                                  - 0010
  assert(!(Error && Heat))
}

object Main extends App {
  println("-------------- Main Starts --------------")
  Check.bmc(() => new Microwave())
  emitVerilog(new  Microwave(), Array("--target-dir", "generated"))
}