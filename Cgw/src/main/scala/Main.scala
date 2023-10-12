import chisel3._
import Passenger._
import Side._

class Cgw extends Module {
  val io = IO(new Bundle() {
    val select = Input(Bool())
    val safe = Output(Bool())
    val fin = Output(Bool())
  })

  val select = Wire(io.select)

  val boat = RegInit(0.U(1.W))
  val cabbage = RegInit(0.U(1.W))
  val goat = RegInit(0.U(1.W))
  val wolf = RegInit(0.U(1.W))

  when(reset.asBool) {
    boat := LEFT
    cabbage := LEFT
    goat := LEFT
    wolf := LEFT
  }

  when(select === CABBAGE && boat === cabbage) {
    cabbage := Mux(cabbage === RIGHT, LEFT, RIGHT)
  }.elsewhen(select === GOAT && boat === goat) {
    goat := Mux(goat === RIGHT, LEFT, RIGHT)
  }.elsewhen(select === WOLF && boat === wolf) {
    wolf := Mux(wolf === RIGHT, LEFT, RIGHT)
  }

  when(select === NONE || select === CABBAGE && cabbage === boat ||
    select === GOAT && goat === boat || select === WOLF && wolf === boat) {
    boat := Mux(boat === RIGHT, LEFT, RIGHT)
  }

  io.safe := boat === goat || (goat =/= wolf && goat =/= cabbage)

  io.fin := goat === RIGHT && wolf === RIGHT && cabbage === RIGHT && boat === RIGHT
}

object Main extends App {
  println("-------------- Main Starts --------------")
  emitVerilog(new Cgw(), Array("--target-dir", "generated"))
}