package Barrel

import chisel3._
import chisel3.stage.ChiselStage

import scala.util.Random


class Barrel(Width: Int, N: Int) extends Module {
  val io = IO(new Bundle() {
    val b = Output(Vec(N, UInt(Width.W)))
    val r = Output(Vec(N, UInt(Width.W)))
  })

  def rand(): UInt = {
    Random.nextInt(1 << Width).asUInt(Width.W)
  }

  def valid(bb: Vec[UInt], rr: Vec[UInt]): Bool = {
    val result = WireDefault(true.B)
    for (indexb <- 0 to Width) {
      for (indexr <- 0 to Width) {
        val nextb = (indexb + 1) % Width
        val nextr = (indexr + 1) % Width
        when(bb(indexb) === rr(indexr)) {
          when(bb(nextb) =/= rr(nextr)) {
            result := false.B
          }
        }
      }
    }
    result
  }

  val b = RegInit(VecInit.fill(N)(rand()))
  val r = RegInit(VecInit.fill(N)(rand()))

  io.b := b
  io.r := r

  when(reset.asBool) {
    when(!valid(b, r)) {
      for (i <- 0 until N) {
        b(i) := 0.U(Width.W)
        r(i) := 0.U(Width.W)
      }
    }
  }

  for (i <- 0 until N) {
    val nexti = (i + 1) % Width
    when(valid(r, b)) {
      b(i) := b(nexti)
    }
  }
}

object Main extends App {
//  emitVerilog(new Barrel(4, 4), Array("--target-dir", "generated"))
  (new ChiselStage).emitVerilog(new Barrel(2, 4), Array("--target-dir", "generated"))
}