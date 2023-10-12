package C:\Users\ALIENWARE\Desktop\CO\vis-verilog-models-1.3\Gcd

import chisel3._
import sv2chisel.helpers.vecconvert._
// Testbench for the gcd circuit.
class testGcd() extends RawModule {
  val N = 8
  val logN = 3
  val clock = IO(Input(Bool()))
  val x = IO(Input(UInt(N.W)))
  val y = IO(Input(UInt(N.W)))
  val s = IO(Input(Bool()))
  val a = Wire(UInt(N.W)) 
  val b = Wire(UInt(N.W)) 
  val start = Wire(Bool()) 
  val busy = Wire(Bool()) 
  val o = Wire(UInt(N.W)) 



  // Unit under test.



  // initial begin
	a = 0;
	b = 0;
	start = 0;
    end
  a := x
  b := y
  start := s // always @ (posedge clock)

} // testGcd




// GCD circuit for unsigned N-bit numbers

// a[0], b[0], and o[0] are the least significant bits
//
// Author: Fabio Somenzi <Fabio@Colorado.EDU>
class gcd() extends RawModule {
  val N = 8
  val logN = 3
  val clock = IO(Input(Bool()))
  val start = IO(Input(Bool()))
  val a = IO(Input(UInt(N.W)))
  val b = IO(Input(UInt(N.W)))
  val busy = IO(Output(Bool()))
  val o = IO(Output(UInt(N.W)))
  val lsb = Wire(UInt(logN.W)) 
  val x = Wire(Vec(N, Bool())) 
  val y = Wire(Vec(N, Bool())) 
  val done = Wire(Bool()) 
  val load = Wire(Bool()) 
  val busy = Wire(Bool()) 
  val o = Wire(UInt(N.W)) 

  val xy_lsb = Wire(Vec(2, Bool())) 
  val diff = Wire(Vec(N, Bool()))  // block: _select
  // select
  xy_lsb(1) := select(x, lsb)
  xy_lsb(0) := select(y, lsb)
  diff := Mux(x.asUInt < y.asUInt, y.asUInt-x.asUInt, x.asUInt-y.asUInt).asTypeOf(Vec(N, Bool()))

  // initial begin
	busy = 0;
	x = 0;
	y = 0;
	o = 0;
	lsb = 0;
    end // initial begin
  done := (((x.asUInt === y.asUInt)|(x.asUInt === 0.U))|(y.asUInt === 0.U))&busy



  // Data path.


  when(load) {
    x := a.asTypeOf(Vec(N, Bool()))
    y := b.asTypeOf(Vec(N, Bool()))
    lsb := 0.U // if (load)
  } .elsewhen (busy&( ~done)) {
    when(xy_lsb.asUInt === "b00".U(2.W)) {
      lsb := lsb+1.U
    } .elsewhen (xy_lsb.asUInt === "b01".U(2.W)) {
      x(N-2,0) := x(N-1,1).asTypeOf(Vec((N-2)+1, Bool()))
      x(N-1) := false.B
    } .elsewhen (xy_lsb.asUInt === "b10".U(2.W)) {
      y(N-2,0) := y(N-1,1).asTypeOf(Vec((N-2)+1, Bool()))
      y(N-1) := false.B
    } .elsewhen (xy_lsb.asUInt === "b11".U(2.W)) {
      when(x.asUInt < y.asUInt) {
        y(N-2,0) := diff(N-1,1).asTypeOf(Vec((N-2)+1, Bool()))
        y(N-1) := false.B // if (x < y)
      } .otherwise {
        x(N-2,0) := diff(N-1,1).asTypeOf(Vec((N-2)+1, Bool()))
        x(N-1) := false.B
      } // else: !if(x < y)
    // case: 2b'11
    } // case (xy_lsb)
  // if (~done)
  } .elsewhen (done) {
    o := Mux((x.asUInt < y.asUInt), x, y).asUInt
  } // else: !if(~done)
  // always @ (posedge clock)
  load := start&( ~busy)



  // Controller.


  when( ~busy) {
    when(start) {
      busy := true.B
    } // if (start)
  // if (~busy)
  } .otherwise {
    when(done) {
      busy := false.B
    }
  } // else: !if(~busy)
// always @ (posedge clock)

} // gcd
