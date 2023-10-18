package Elevator_parameterized

import chisel3._

import scala.math.log

class ElevatorMain(car_N: Int, level_N: Int) extends Module {
  val level_logN = log(level_N - 1).toInt + 1

  val io = IO(new Bundle() {
    val random_up = Input(UInt(level_N.W))
    val random_down = Input(UInt(level_N.W))
    val random = Input(Vec(car_N, Bool()))
    val r_stop = Input(Vec(car_N, Bool()))
    val random_push = Input(Vec(car_N, UInt(level_N.W)))
    val init = Input(Vec(car_N, UInt(level_logN.W)))
  })

  val stop_next = Wire(Vec(car_N, Bool()))
  val inc = Wire(Vec(car_N, Bool()))
  val dec = Wire(Vec(car_N, Bool()))
  val continue = Wire(Vec(car_N, Bool()))
  val init = Wire(Vec(car_N, UInt(level_logN.W)))

  val elevators = Seq.fill(car_N)(Module(new Elevator(car_N, level_N)))

  for (car <- 0 until car_N) {
    init(car) := Mux(io.init(car) >= level_N.U, (level_N - 1).U, io.init(car))

    elevators(car).io.stop_next := stop_next(car)
    elevators(car).io.continue := continue(car)
    elevators(car).io.random_push := io.random_push(car)
    elevators(car).io.random := io.random(car)
    elevators(car).io.r_stop := io.r_stop(car)
    elevators(car).io.init := init(car)
    inc(car) := elevators(car).io.inc
    dec(car) := elevators(car).io.dec
  }

  val main_control = Module(new MainControl(car_N, level_N))
  main_control.io.inc := inc
  main_control.io.dec := dec
  main_control.io.random_up := io.random_up
  main_control.io.random_down := io.random_down
  main_control.io.init := init
  stop_next := main_control.io.stop_next
  continue := main_control.io.continue
}

object Main extends App {
  println("-------------- Elevator1_4.Main Starts --------------")
  emitVerilog(new ElevatorMain(1, 4), Array("--target-dir", "generated"))
}