package Short

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import chisel3.util.random.FibonacciLFSR

object Status extends ChiselEnum {
  val ready, busy = Value
}

class Short extends Module {
  val io = IO(new Bundle() {
    val request = Output(Bool())
  })
  import Status._

  val state = RegInit(ready)
  val nond_state = Wire(Status())
  nond_state := Status.apply(FibonacciLFSR.maxPeriod(8)(0))
  io.request := FibonacciLFSR.maxPeriod(8)(0)

  switch(state) {
    is(ready) {
      when(io.request === 1.U) {
        state := busy
      }.otherwise {
        state := nond_state
      }
    }
    is(busy) {
      state := nond_state
    }
  }
}

object Main extends App {
  (new ChiselStage).emitVerilog(new Short(), Array("--target-dir", "generated"))
}
