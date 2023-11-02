package Dekker

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

import scala.util.Random

object Loc extends ChiselEnum {
  val L0, L1, L2, L3, L4, L5, L6 = Value
}

class Dekker extends Module {
  val io = IO(new Bundle() {
    val select = Input(UInt(1.W))
    val pause = Input(Bool())
    val c = Output(Vec(2, Bool()))
  })
  import Loc._

  val c = RegInit(VecInit(true.B, true.B))
  val turn = RegInit(Random.nextInt(2).asUInt(1.W))
  val self = RegInit(io.select)
  val pc = RegInit(VecInit(L0, L0))

  io.c := c
  self := io.select
  switch(pc(self)) {
    is(L0) {
      when(!io.pause) {
        pc(self) := L1
      }
    }
    is(L1) {
      c(self) := false.B
      pc(self) := L2
    }
    is(L2) {
      when(c((~self).asUInt)) {
        pc(self) := L5
      }.otherwise {
        pc(self) := L3
      }
    }
    is(L3) {
      when(turn === self) {
        pc(self) := L2
      }.otherwise {
        c(self) := 1.U
        pc(self) := L4
      }
    }
    is(L4) {
      when(turn === self) {
        c(self) := 0.U
        pc(self) := L2
      }
    }
    is(L5) {
      when(!io.pause) {
        pc(self) := L6
      }
    }
    is(L6) {
      c(self) := 1.U
      turn := (~self).asUInt
      pc(self) := L0
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(
    new Dekker(), Array("--target-dir", "generated")
  )
}
