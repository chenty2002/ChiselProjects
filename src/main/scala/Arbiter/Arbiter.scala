package Arbiter

import Selection._
import chisel3._
import chisel3.util._

class Arbiter extends Module {
  val io = IO(new Bundle() {
    val active = Input(Bool())
    val sel = Output(Selection())
  })

  val state = RegInit(A)
  io.sel := Mux(io.active, state, X)

  when(io.active) {
    switch(state) {
      is(A) {
        state := B
      }
      is(B) {
        state := C
      }
      is(C) {
        state := A
      }
    }
  }
}