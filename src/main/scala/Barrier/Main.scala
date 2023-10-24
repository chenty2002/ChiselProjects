package Barrier

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage
import scala.util.Random

object LOC extends ChiselEnum {
  val L0, L1, L2, L3, L4, L5, L6 = Value
}

class Barrier extends Module {
  val io = IO(new Bundle() {
    val select = Input(UInt(1.W))
    val pause = Input(Bool())
    val pc = Output(Vec(2, LOC()))
  })
  import LOC._

  val rel = RegInit(Random.nextBoolean().asBool)
  val self = RegInit(io.select)
  val pc = RegInit(VecInit(L0, L0))
  val count = RegInit(0.U(2.W))

  io.pc := pc

  self := io.select
  switch(pc(self)) {
    is(L0) {
      when(!io.pause) {
        pc(self) := L1
      }
    }
    is(L1) {
      rel := 0.U
      pc(self) := L2
    }
    is(L2) {
      count := count + 1.U
      pc(self) := L3
    }
    is(L3) {
      when(count === 2.U) {
        pc(self) := L4
      }.otherwise {
        pc(self) := L6
      }
    }
    is(L4) {
      count := 0.U
      rel := 1.U
      pc(self) := L5
    }
    is(L5) {
      pc(self) := L0
    }
    is(L6) {
      when(rel) {
        pc(self) := L5
      }
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(new Barrier, Array("--target-dir", "generated"))
}