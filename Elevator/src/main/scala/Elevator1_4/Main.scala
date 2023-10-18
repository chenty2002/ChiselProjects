package Elevator1_4

import chisel3._

class ElevatorMain extends Module {
  val io = IO(new Bundle() {
    val random_up = Input(UInt(4.W))
    val random_down = Input(UInt(4.W))
    val random = Input(Bool())
    val r_stop = Input(Bool())
    val random_push1 = Input(UInt(4.W))
    val init11 = Input(UInt(2.W))
  })

  val stop_next = Wire(Bool())
  val inc = Wire(Bool())
  val dec = Wire(Bool())
  val continue = Wire(Bool())
  val init1 = Wire(UInt(2.W))

  init1 := io.init11

  val e1 = Module(new Elevator)
  e1.io.stop_next := stop_next
  e1.io.continue := continue
  e1.io.random_push := io.random_push1
  e1.io.random := io.random
  e1.io.r_stop := io.r_stop
  e1.io.init := init1
  inc := e1.io.inc
  dec := e1.io.dec

  val main_control = Module(new MainControl)
  main_control.io.inc := inc
  main_control.io.dec := dec
  main_control.io.random_up := io.random_up
  main_control.io.random_down := io.random_down
  main_control.io.init1 := init1
  stop_next := main_control.io.stop_next
  continue := main_control.io.continue
}

object Main extends App {
  println("-------------- Elevator1_4.Main Starts --------------")
  emitVerilog(new ElevatorMain(), Array("--target-dir", "generated"))
}