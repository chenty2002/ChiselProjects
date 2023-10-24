package Elevator.Elevator1_4

import chisel3._

class MainControl extends Module {
  val io = IO(new Bundle() {
    val inc = Input(Bool())
    val dec = Input(Bool())
    val stop_next = Output(Bool())
    val continue = Output(Bool())
    val random_up = Input(UInt(4.W))
    val random_down = Input(UInt(4.W))
    val init1 = Input(UInt(2.W))
  })

  import Dir._
  import Onoff._

  val locations = RegInit(VecInit(io.init1))
  val up_floor_buttons = RegInit(VecInit(OFF, OFF, OFF, OFF))
  val down_floor_buttons = RegInit(VecInit(OFF, OFF, OFF, OFF))
  val buttons = WireDefault(VecInit(false.B, false.B, false.B, false.B))
  val top = WireDefault(VecInit(false.B, false.B, false.B, false.B))
  val bottom = WireDefault(VecInit(false.B, false.B, false.B, false.B))
  val button_above = Wire(Bool())
  val button_below = Wire(Bool())
  val direction = RegInit(VecInit(UP))

  buttons(0) := up_floor_buttons(0) === ON || down_floor_buttons(0) === ON
  buttons(1) := up_floor_buttons(1) === ON || down_floor_buttons(1) === ON
  buttons(2) := up_floor_buttons(2) === ON || down_floor_buttons(2) === ON
  buttons(3) := up_floor_buttons(3) === ON || down_floor_buttons(3) === ON

  bottom(1) := buttons(0)
  bottom(2) := bottom(1) || buttons(1)
  bottom(3) := bottom(2) || buttons(2)
  top(2) := buttons(3)
  top(1) := top(2) || buttons(2)
  top(0) := top(1) || buttons(1)

  button_below := (locations(0) === 3.U && bottom(3)) ||
    (locations(0) === 2.U && bottom(2)) ||
    (locations(0) === 1.U && bottom(1))
  button_above := (locations(0) === 0.U && top(0)) ||
    (locations(0) === 1.U && top(1)) ||
    (locations(0) === 2.U && top(2))
  io.continue := button_above && direction(0) === UP || button_below && direction(0) === DOWN

  val left = Wire(Bool())
  left := up_floor_buttons(locations(0) + 1.U) === ON
  val right = Wire(Bool())
  val right_left = Wire(Bool())
  right_left := down_floor_buttons(locations(0) - 1.U) === ON
  right := Mux(locations(0) =/= 0.U && direction(0) === DOWN, right_left, false.B)
  io.stop_next := Mux(locations(0) =/= 3.U && direction(0) === UP, left, right)

  for (i <- 0 to 3) {
    when(io.random_up(i)) {
      up_floor_buttons(i) := ON
    }
    when(io.random_down(i)) {
      down_floor_buttons(i) := ON
    }
  }

  for (i <- 0 until 1) {
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

  for (i <- 0 until 1) {
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
