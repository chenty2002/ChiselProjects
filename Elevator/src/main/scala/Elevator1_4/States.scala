package Elevator1_4

import chisel3.ChiselEnum

object Dir extends ChiselEnum {
  val UP, DOWN = Value
}

object Mov extends ChiselEnum {
  val STOPPED, MOVING = Value
}

object Dr extends ChiselEnum {
  val OPEN, OPENING, CLOSED, CLOSING = Value
}

object Onoff extends ChiselEnum {
  val ON, OFF = Value
}