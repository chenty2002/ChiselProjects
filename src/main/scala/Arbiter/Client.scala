package Arbiter

import ClientState._
import chisel3._
import chisel3.util._
import chisel3.util.random._

class Client extends Module {
  val io = IO(new Bundle() {
    val ack = Input(Bool())
    val req = Output(Bool())
  })

  val req = RegInit(false.B)
  io.req := req
  val state = RegInit(NO_REQ)

  val rand_choice = FibonacciLFSR.maxPeriod(8)(0).asBool

  switch(state) {
    is(NO_REQ) {
      when(rand_choice) {
        req := true.B
        state := REQ
      }
    }
    is(REQ) {
      when(io.ack) {
        state := HAVE_TOKEN
      }
    }
    is(HAVE_TOKEN) {
      when(rand_choice) {
        req := false.B
        state := NO_REQ
      }
    }
  }
}
