package Peterson

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

object Loc extends ChiselEnum {
  val L0, L1, L2, L3, L4, L5 = Value
}

class Peterson extends Module {
  val io = IO(new Bundle() {
    val select = Input(UInt(1.W))
    val pause = Input(Bool())
    val pc = Output(Vec(2, Loc()))
  })

  import Loc._

  val interested = RegInit(VecInit(false.B, false.B))
  val turn = RegInit(false.B)
  val self = RegInit(0.U(1.W))
  val pc = RegInit(VecInit(L0, L0))

  io.pc := pc
  self := io.select
  switch(pc(self)) {
    is(L0) {
      when(!io.pause) {
        pc(self) := L1
      }
    }
    is(L1) {
      interested(self) := true.B
      pc(self) := L2
    }
    is(L2) {
      turn := (~self).asUInt
      pc(self) := L3
    }
    is(L3) {
      when(!interested((~self).asUInt) || turn === self) {
        pc(self) := L4
      }
    }
    is(L4) {
      when(!io.pause) {
        pc(self) := L5
      }
    }
    is(L5) {
      interested(self) := false.B
      pc(self) := L0
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(new Peterson(), Array("--target-dir", "generated"))
}
