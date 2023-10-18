package Elevator1_4

import chisel3._
import chisel3.util._

class Elevator extends Module {
  val io = IO(new Bundle() {
    val stop_next = Input(Bool())
    val continue = Input(Bool())
    val random_push = Input(UInt(4.W))
    val random = Input(Bool())
    val r_stop = Input(Bool())
    val init = Input(UInt(2.W))
    val inc = Output(Bool())
    val dec = Output(Bool())
  })

  import Dir._
  import Dr._
  import Mov._
  import Onoff._

  val buttons = RegInit(VecInit(OFF, OFF, OFF, OFF))
  val random_push = WireDefault(io.random_push)
  val init = WireDefault(io.init)
  val location = RegInit(io.init)
  val direction = RegInit(UP)
  val movement = RegInit(STOPPED)
  val door = RegInit(OPEN)
  val open_next = RegInit(false.B)
  val button_above = Wire(Bool())
  val button_below = Wire(Bool())
  val top = WireDefault(VecInit(false.B, false.B, false.B, false.B))
  val bottom = WireDefault(VecInit(false.B, false.B, false.B, false.B))

  bottom(1) := buttons(0) === ON
  bottom(2) := bottom(1) || buttons(1) === ON
  bottom(3) := bottom(2) || buttons(2) === ON
  top(2) := buttons(3) === ON
  top(1) := top(2) || buttons(2) === ON
  top(0) := top(1) || buttons(1) === ON
  button_below := (location === 3.U && bottom(3)) || (location === 2.U && bottom(2)) || (location === 1.U && bottom(1))
  button_above := (location === 0.U && top(0)) || (location === 1.U && top(1)) || (location === 2.U && top(2))

  for (i <- 0 to 3) {
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

  when(location === 3.U) {
    direction := DOWN
  }
  when(location === 0.U) {
    direction := UP
  }
}
