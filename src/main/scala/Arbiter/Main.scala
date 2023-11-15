package Arbiter

import Selection._
import chisel3._
import chiselFv._

class Main extends Module with Formal {
  val io = IO(new Bundle() {
    val ackA = Output(Bool())
    val ackB = Output(Bool())
    val ackC = Output(Bool())
  })

  val controllerA = Module(new Controller)
  val controllerB = Module(new Controller)
  val controllerC = Module(new Controller)
  val arbiter = Module(new Arbiter)

  val clientA = Module(new Client)
  val clientB = Module(new Client)
  val clientC = Module(new Client)

  controllerA.io.req := clientA.io.req
  clientA.io.ack := controllerA.io.ack
  controllerB.io.req := clientB.io.req
  clientB.io.ack := controllerB.io.ack
  controllerC.io.req := clientC.io.req
  clientC.io.ack := controllerC.io.ack

  controllerA.io.sel := arbiter.io.sel
  controllerA.io.id := A
  io.ackA := controllerA.io.ack

  controllerB.io.sel := arbiter.io.sel
  controllerB.io.id := B
  io.ackB := controllerB.io.ack

  controllerC.io.sel := arbiter.io.sel
  controllerC.io.id := C
  io.ackC := controllerC.io.ack

  arbiter.io.active := controllerA.io.pass_token ||
    controllerB.io.pass_token ||
    controllerC.io.pass_token

  assert((!io.ackA && !io.ackB) || (!io.ackB && !io.ackC) || (!io.ackA && !io.ackC))
}

object Main extends App {
  Check.bmc(() => new Main(), 50)
  emitVerilog(new Main, Array("--target-dir", "generated"))
}