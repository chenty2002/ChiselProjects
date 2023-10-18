package Elevator_parameterized

import chisel3._

import scala.math.log

class MainControl(car_N: Int, level_N: Int) extends Module {
  val level_logN = log(level_N - 1).toInt + 1

  val io = IO(new Bundle() {
    val inc = Input(Vec(car_N, Bool()))
    val dec = Input(Vec(car_N, Bool()))
    val stop_next = Output(Vec(car_N, Bool()))
    val continue = Output(Vec(car_N, Bool()))
    val random_up = Input(UInt(level_N.W))
    val random_down = Input(UInt(level_N.W))
    val init = Input(Vec(car_N, UInt(level_logN.W)))
    //    val init1 = Input(UInt(2.W))
  })

  import Dir._
  import Onoff._

  val locations = RegInit(io.init)
  val up_floor_buttons = RegInit(VecInit.fill(level_N)(OFF))
  val down_floor_buttons = RegInit(VecInit.fill(level_N)(OFF))
  val buttons = WireDefault(VecInit.fill(level_N)(false.B))
  val top = WireDefault(VecInit.fill(level_N)(false.B))
  val bottom = WireDefault(VecInit.fill(level_N)(false.B))
  val button_above = WireDefault(VecInit.fill(car_N)(false.B))
  val button_below = WireDefault(VecInit.fill(car_N)(false.B))
  val direction = RegInit(VecInit.fill(car_N)(UP))

  for (i <- 0 until level_N)
    buttons(i) := up_floor_buttons(i) === ON || down_floor_buttons(i) === ON

  for (i <- 1 until level_N) {
    if (i == 1) bottom(i) := buttons(i - 1)
    else bottom(i) := bottom(i - 1) || buttons(i - 1)
  }
  for (car <- 0 until car_N) {
//    button_below(car) := button_below(car) || (locations(car) === i.asUInt && bottom(i))
    val conditions_below = for (i <- 1 until level_N) yield locations(car) === i.asUInt && bottom(i)
    button_below(car) := conditions_below.reduce(_ || _)
  }

  for (i <- level_N - 2 to 0 by -1) {
    if (i == level_N - 2) top(i) := buttons(i + 1)
    else top(i) := top(i + 1) || buttons(i + 1)
  }
  for (car <- 0 until car_N) {
//    button_above(car) := button_above(car) || (locations(car) === i.asUInt && top(i))
    val conditions_above = for (i <- 1 until level_N) yield locations(car) === i.asUInt && top(i)
    button_above(car) := conditions_above.reduce(_ || _)
  }

  for (car <- 0 until car_N) {
    io.continue(car) := button_above(car) && direction(car) === UP || button_below(car) && direction(car) === DOWN
    io.stop_next(car) := Mux(locations(car) =/= (level_N - 1).U && direction(car) === UP,
      up_floor_buttons(locations(car) + 1.U) === ON,
      Mux(locations(car) =/= 0.U && direction(car) === DOWN,
        down_floor_buttons(locations(car) - 1.U) === ON,
        false.B))
  }

  for (i <- 0 until level_N) {
    when(io.random_up(i)) {
      up_floor_buttons(i) := ON
    }
    when(io.random_down(i)) {
      down_floor_buttons(i) := ON
    }
  }

  for (i <- 0 until car_N) {
    when(locations(i) =/= 3.U && direction(i) === UP) {
      when(up_floor_buttons(locations(i) + 1.U) === ON) {
        up_floor_buttons(locations(i) + 1.U) := OFF
      }
    }
    when(locations(i) =/= 0.U && direction(i) === DOWN) {
      when(down_floor_buttons(locations(i) - 1.U) === ON) {
        down_floor_buttons(locations(i) - 1.U) := OFF
      }
    }
  }

  for (i <- 0 until car_N) {
    when(locations(i) === 3.U) {
      direction(i) := DOWN
    }
    when(locations(i) === 0.U) {
      direction(i) := UP
    }
    when(io.inc(i)) {
      locations(i) := locations(i) + 1.U
      direction(i) := UP
    }
    when(io.dec(i)) {
      locations(i) := locations(i) - 1.U
      direction(i) := DOWN
    }
  }
}
