package Arbiter

import ControllerState._
import chisel3._
import chisel3.util._

class Controller extends Module {
  val io = IO(new Bundle() {
    val req = Input(Bool())
    val sel = Input(Selection())
    val id = Input(Selection())
    val ack = Output(Bool())
    val pass_token = Output(Bool())
  })

  val sel = WireDefault(io.sel)
  val id = WireDefault(io.id)
  val ack = RegInit(false.B)
  io.ack := ack
  val pass_token = RegInit(true.B)
  io.pass_token := pass_token
  val state = RegInit(IDLE)
  val is_selected = Wire(Bool())
  is_selected := sel === id

  switch(state) {
    is(IDLE) {
      when(is_selected) {
        when(io.req) {
          state := READY
          pass_token := false.B
        }.otherwise {
          pass_token := true.B
        }
      }.otherwise {
        pass_token := false.B
      }
    }
    is(READY) {
      state := BUSY
      ack := true.B
    }
    is(BUSY) {
      when(!io.req) {
        state := IDLE
        ack := false.B
        pass_token := true.B
      }
    }
  }
}
