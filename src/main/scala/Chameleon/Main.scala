package Chameleon

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

import scala.util.Random

object Color {
  val red :: green :: blue :: Nil = Enum(3)

  def rand(): UInt = {
    Random.nextInt(3) match {
      case 0 => red
      case 1 => green
      case 2 => blue
    }
  }
}

class Chameleon(bits: Int) extends Module {
  val msb = bits - 1
  val n = 1<<bits

  val io = IO(new Bundle() {
    val first = Input(UInt(bits.W))
    val cham = Output(Vec(n, UInt(bits.W)))
    val stable = Output(Bool())
  })
  import Color._

  val cham = RegInit(VecInit.fill(n)(rand()))
  val select = RegInit(io.first)
  val second = Wire(UInt(bits.W))
  val stable = RegInit(true.B)

  io.cham := cham
  io.stable := stable

  stable := cham.slice(0, n-1).zip(cham.slice(1, n)).map{
    case (a, b) => a === b
  }.reduce(_ && _)
  second := io.first + 1.U(bits.W)

  select := io.first
  switch(cham(io.first)) {
    is(red) {
      when(cham(second) === green) {
        cham(io.first) := blue
        cham(second) := blue
      }.elsewhen(cham(second) === blue) {
        cham(io.first) := green
        cham(second) := green
      }
    }
    is(green) {
      when(cham(second) === red) {
        cham(io.first) := blue
        cham(second) := blue
      }.elsewhen(cham(second) === blue) {
        cham(io.first) := red
        cham(second) := red
      }
    }
    is(blue) {
      when(cham(second) === red) {
        cham(io.first) := green
        cham(second) := green
      }.elsewhen(cham(second) === green) {
        cham(io.first) := red
        cham(second) := red
      }
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(
    new Chameleon(3),
    Array("--target-dir", "generated"))
}
