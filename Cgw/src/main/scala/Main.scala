import chisel3._

object Passenger extends ChiselEnum {
  val NONE, CABBAGE, GOAT, WOLF = Value
}

object Side extends ChiselEnum {
  val LEFT, RIGHT = Value
}

class Cgw extends Module {
  val io = IO(new Bundle() {
    val select = Input(Passenger())
    val safe = Output(Bool())
    val fin = Output(Bool())
  })
  import Passenger._
  import Side._

  val select = WireDefault(io.select)

  val boat = RegInit(LEFT)
  val cabbage = RegInit(LEFT)
  val goat = RegInit(LEFT)
  val wolf = RegInit(LEFT)

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