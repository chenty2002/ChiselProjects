package Elevator.Elevator_parameterized

import chisel3._
import chisel3.util._

import scala.math.log

class Elevator(car_N: Int, level_N: Int) extends Module {
  val level_logN = log(level_N - 1).toInt + 1

  val io = IO(new Bundle() {
    val stop_next = Input(Bool())
    val continue = Input(Bool())
    val random_push = Input(UInt(level_N.W))
    val random = Input(Bool())
    val r_stop = Input(Bool())
    val init = Input(UInt(level_logN.W))
    val inc = Output(Bool())
    val dec = Output(Bool())
  })

  import Dir._
  import Dr._
  import Mov._
  import Onoff._

  val buttons = RegInit(VecInit.fill(level_N)(OFF))
  val random_push = WireDefault(io.random_push)
  val init = WireDefault(io.init)
  val location = RegInit(io.init)
  val direction = RegInit(UP)
  val movement = RegInit(STOPPED)
  val door = RegInit(OPEN)
  val open_next = RegInit(false.B)
  val button_above = WireDefault(false.B)
  val button_below = WireDefault(false.B)
  val top = WireDefault(VecInit.fill(level_N)(false.B))
  val bottom = WireDefault(VecInit.fill(level_N)(false.B))

  for (i <- 1 until level_N) {
    if (i == 1) bottom(i) := buttons(i - 1) === ON
    else bottom(i) := bottom(i - 1) || buttons(i - 1) === ON
//    button_below := button_below || (location === i.asUInt && bottom(i))
  }
  val conditions_below = for(i <- 1 until level_N) yield location === i.asUInt && bottom(i)
  button_below := conditions_below.reduce(_ || _)

  for (i <- level_N - 2 to 0 by -1) {
    if (i == level_N - 2) top(i) := buttons(i + 1) === ON
    else top(i) := top(i + 1) || buttons(i + 1) === ON
//    button_above := button_above || (location === i.asUInt && top(i))
  }
  val conditions_above = for(i <- 1 until level_N) yield location === i.asUInt && top(i)
  button_above := conditions_above.reduce(_ || _)

  for (i <- 0 until level_N) {
    when(location === i.U) {
      buttons(i) := OFF
    }.elsewhen(random_push(i)) {
      buttons(i) := ON
    }
  }

  when(io.stop_next) {
    when(direction === UP) {
      buttons(location + 1.U) := ON
    }.otherwise {
      buttons(location - 1.U) := ON
    }
  }

  when(door =/= CLOSED) {
    open_next := false.B
  }.elsewhen(movement === MOVING &&
    (io.stop_next ||
      (direction === UP && buttons(location + 1.U) === ON) ||
      (direction === DOWN && buttons(location - 1.U) === ON))) {
    open_next := true.B
  }

  switch(door) {
    is(CLOSED) {
      when(open_next && movement === STOPPED) {
        door := OPENING
      }
    }
    is(OPENING) {
      when(io.random) {
        door := OPEN
      }
    }
    is(OPEN) {
      when(io.random) {
        door := CLOSING
      }
    }
    is(CLOSING) {
      when(io.random) {
        door := CLOSED
      }
    }
  }

  val stop_moving = Wire(Bool())
  val start_moving = Wire(Bool())

  start_moving := (io.continue || button_above && direction === UP) || (button_below && direction === DOWN)
  stop_moving := io.r_stop && movement === MOVING
  io.inc := stop_moving && direction === UP
  io.dec := stop_moving && direction === DOWN

  when(door === CLOSED) {
    switch(movement) {
      is(STOPPED) {
        when(door === CLOSED && start_moving && !open_next) {
          movement := MOVING
        }
      }
      is(MOVING) {
        when(stop_moving) {
          movement := STOPPED
        }
        when(direction === UP) {
          location := location + 1.U
        }
        when(direction === DOWN) {
          location := location - 1.U
        }
      }
    }
  }

  switch(direction) {
    is(UP) {
      when(!button_above && !io.continue) {
        direction := DOWN
      }
    }
    is(DOWN) {
      when(!button_below && !io.continue) {
        direction := UP
      }
    }
  }

  when(location === (level_N - 1).U) {
    direction := DOWN
  }
  when(location === 0.U) {
    direction := UP
  }
}
