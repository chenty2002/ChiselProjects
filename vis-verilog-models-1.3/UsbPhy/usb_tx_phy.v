// Adapted for vl2mv by Fabio Somenzi <Fabio@Colorado.EDU>

/////////////////////////////////////////////////////////////////////
////                                                             ////
////  USB 1.1 PHY                                                ////
////  TX                                                         ////
////                                                             ////
////                                                             ////
////  Author: Rudolf Usselmann                                   ////
////          rudi@asics.ws                                      ////
////                                                             ////
////                                                             ////
////  Downloaded from: http://www.opencores.org/cores/usb_phy/   ////
////                                                             ////
/////////////////////////////////////////////////////////////////////
////                                                             ////
//// Copyright (C) 2000-2002 Rudolf Usselmann                    ////
////                         www.asics.ws                        ////
////                         rudi@asics.ws                       ////
////                                                             ////
//// This source file may be used and distributed without        ////
//// restriction provided that this copyright statement is not   ////
//// removed from the file and that any derivative work contains ////
//// the original copyright notice and the associated disclaimer.////
////                                                             ////
////     THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY     ////
//// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED   ////
//// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   ////
//// FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL THE AUTHOR      ////
//// OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         ////
//// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES    ////
//// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE   ////
//// GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR        ////
//// BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  ////
//// LIABILITY, WHETHER IN  CONTRACT, STRICT LIABILITY, OR TORT  ////
//// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  ////
//// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE         ////
//// POSSIBILITY OF SUCH DAMAGE.                                 ////
////                                                             ////
/////////////////////////////////////////////////////////////////////

//  CVS Log
//
//  $Id: usb_tx_phy.v,v 1.1.1.1 2002/09/16 14:27:02 rudi Exp $
//
//  $Date: 2002/09/16 14:27:02 $
//  $Revision: 1.1.1.1 $
//  $Author: rudi $
//  $Locker:  $
//  $State: Exp $
//
// Change History:
//               $Log: usb_tx_phy.v,v $
//               Revision 1.1.1.1  2002/09/16 14:27:02  rudi
//               Created Directory Structure
//
//
//
//
//
//
//

typedef enum {
	      IDLE,
	      SOP,
	      DATA,
	      EOP1,
	      EOP2,
	      WAIT
	      } TxState;

module usb_tx_phy(
		clk, rst, fs_ce, phy_mode,

		// Transciever Interface
		txdp, txdn, txoe,	

		// UTMI Interface
		DataOut_i, TxValid_i, TxReady_o
		);

    input		clk;
    input 		rst;
    input 		fs_ce;
    input 		phy_mode;
    output 		txdp, txdn, txoe;
    input [7:0] 	DataOut_i;
    input 		TxValid_i;
    output 		TxReady_o;

    ///////////////////////////////////////////////////////////////////
    //
    // Local Wires and Registers
    //

    reg 		TxReady_o;
    TxState reg 	state;
    reg 		tx_ready;
    reg 		tx_ready_d;
    reg 		ld_sop_d;
    reg 		ld_data_d;
    reg 		ld_eop_d;
    reg 		tx_ip;
    reg 		tx_ip_sync;
    reg [2:0] 		bit_cnt;
    reg [7:0] 		hold_reg;
    reg 		sd_raw_o;
    wire 		hold;
    reg 		data_done;
    reg 		sft_done;
    reg 		sft_done_r;
    wire 		sft_done_e;
    reg 		ld_data;
    wire 		eop_done;
    reg [2:0] 		one_cnt;
    wire 		stuff;
    reg 		sd_bs_o;
    reg 		sd_nrzi_o;
    reg 		append_eop;
    reg 		append_eop_sync1;
    reg 		append_eop_sync2;
    reg 		append_eop_sync3;
    reg 		txdp, txdn;
    reg 		txoe_r1, txoe_r2;
    reg 		txoe;

    initial begin
	TxReady_o = 1'b0;
	tx_ip = 1'b0;
	tx_ip_sync = 1'b0;
	data_done = 1'b0;
	bit_cnt = 3'h0;
	one_cnt = 3'h0;
	sd_bs_o = 1'h0;
	sd_nrzi_o = 1'b1;
	append_eop = 1'b0;
	append_eop_sync1 = 1'b0;
	append_eop_sync2 = 1'b0;
	append_eop_sync3 = 1'b0;
	txoe_r1 = 1'b0;
	txoe_r2 = 1'b0;
	txoe = 1'b1;
	txdp = 1'b1;
	txdn = 1'b0;
	state = IDLE;
	ld_data = 0;
	sft_done = 0;
	sd_raw_o = 0;
	ld_eop_d = 0;
	hold_reg = 0;
	sft_done_r = 0;
	tx_ready = 0;
	tx_ready_d = 0;
	ld_data_d = 0;
	ld_sop_d = 0;
    end


    ///////////////////////////////////////////////////////////////////
    //
    // Misc Logic
    //

    always @(posedge clk)
      tx_ready = tx_ready_d;

    always @(posedge clk)
      if(!rst)	TxReady_o = 1'b0;
      else	TxReady_o = tx_ready_d & TxValid_i;

    always @(posedge clk)
      ld_data = ld_data_d;

    ///////////////////////////////////////////////////////////////////
    //
    // Transmit in progress indicator
    //

    always @(posedge clk)
      if(!rst)		tx_ip = 1'b0;
      else
	if(ld_sop_d)	tx_ip = 1'b1;
	else
	  if(eop_done)	tx_ip = 1'b0;

    always @(posedge clk)
      if(!rst)		tx_ip_sync = 1'b0;
      else
	if(fs_ce)	tx_ip_sync = tx_ip;

    // data_done helps us to catch cases where TxValid drops due to
    // packet end and then gets re-asserted as a new packet starts.
    // We might not see this because we are still transmitting.
    // data_done should solve those cases ...
    always @(posedge clk)
      if(!rst)			data_done = 1'b0;
      else
	if(TxValid_i & ! tx_ip)	data_done = 1'b1;
	else
	  if(!TxValid_i)	data_done = 1'b0;

    ///////////////////////////////////////////////////////////////////
    //
    // Shift Register
    //

    always @(posedge clk)
      if(!rst)			bit_cnt = 3'h0;
      else
	if(!tx_ip_sync)		bit_cnt = 3'h0;
	else
	  if(fs_ce & !hold)	bit_cnt = bit_cnt + 3'h1;

    assign hold = stuff;

    always @(posedge clk)
      if(!tx_ip_sync)		sd_raw_o = 1'b0;
      else
	case(bit_cnt)
	  3'h0: sd_raw_o = hold_reg[0];
	  3'h1: sd_raw_o = hold_reg[1];
	  3'h2: sd_raw_o = hold_reg[2];
	  3'h3: sd_raw_o = hold_reg[3];
	  3'h4: sd_raw_o = hold_reg[4];
	  3'h5: sd_raw_o = hold_reg[5];
	  3'h6: sd_raw_o = hold_reg[6];
	  3'h7: sd_raw_o = hold_reg[7];
	endcase

    always @(posedge clk)
      sft_done = !hold & (bit_cnt == 3'h7);

    always @(posedge clk)
      sft_done_r = sft_done;

    assign sft_done_e = sft_done & !sft_done_r;

    // Out Data Hold Register
    always @(posedge clk)
      if(ld_sop_d)	hold_reg = 8'h80;
      else
	if(ld_data)	hold_reg = DataOut_i;

    ///////////////////////////////////////////////////////////////////
    //
    // Bit Stuffer
    //

    always @(posedge clk)
      if(!rst)				one_cnt = 3'h0;
      else
	if(!tx_ip_sync)			one_cnt = 3'h0;
	else
	  if(fs_ce)
	    begin
		if(!sd_raw_o | stuff)	one_cnt = 3'h0;
		else			one_cnt = one_cnt + 3'h1;
	    end

    assign stuff = (one_cnt==3'h6);

    always @(posedge clk)
      if(!rst)		sd_bs_o = 1'h0;
      else
	if(fs_ce)	sd_bs_o = !tx_ip_sync ? 1'b0 :
				  (stuff ? 1'b0 : sd_raw_o);

    ///////////////////////////////////////////////////////////////////
    //
    // NRZI Encoder
    //

    always @(posedge clk)
      if(!rst)				sd_nrzi_o = 1'b1;
      else
	if(!tx_ip_sync | !txoe_r1)	sd_nrzi_o = 1'b1;
	else
	  if(fs_ce)		sd_nrzi_o = sd_bs_o ? sd_nrzi_o : ~sd_nrzi_o;

    ///////////////////////////////////////////////////////////////////
    //
    // EOP append logic
    //

    always @(posedge clk)
      if(!rst)			append_eop = 1'b0;
      else
	if(ld_eop_d)		append_eop = 1'b1;
	else
	  if(append_eop_sync2)	append_eop = 1'b0;

    always @(posedge clk)
      if(!rst)		append_eop_sync1 = 1'b0;
      else
	if(fs_ce)	append_eop_sync1 = append_eop;

    always @(posedge clk)
      if(!rst)		append_eop_sync2 = 1'b0;
      else
	if(fs_ce)	append_eop_sync2 = append_eop_sync1;

    always @(posedge clk)
      if(!rst)		append_eop_sync3 = 1'b0;
      else
	if(fs_ce)	append_eop_sync3 = append_eop_sync2;

    assign eop_done = append_eop_sync3;

    ///////////////////////////////////////////////////////////////////
    //
    // Output Enable Logic
    //

    always @(posedge clk)
      if(!rst)		txoe_r1 = 1'b0;
      else
	if(fs_ce)	txoe_r1 = tx_ip_sync;

    always @(posedge clk)
      if(!rst)		txoe_r2 = 1'b0;
      else
	if(fs_ce)	txoe_r2 = txoe_r1;

    always @(posedge clk)
      if(!rst)		txoe = 1'b1;
      else
	if(fs_ce)	txoe = !(txoe_r1 | txoe_r2);

    ///////////////////////////////////////////////////////////////////
    //
    // Output Registers
    //

    always @(posedge clk)
      if(!rst)		txdp = 1'b1;
      else
	if(fs_ce)	txdp = phy_mode ?
			       (!append_eop_sync3 &  sd_nrzi_o) :
			       sd_nrzi_o;

    always @(posedge clk)
      if(!rst)		txdn = 1'b0;
      else
	if(fs_ce)	txdn = phy_mode ?
			       (!append_eop_sync3 & ~sd_nrzi_o) :
			       append_eop_sync3;

    ///////////////////////////////////////////////////////////////////
    //
    // Tx state machine
    //

    always @(posedge clk) begin
	if(!rst)	
	  state = IDLE;
	else begin
	    tx_ready_d = 1'b0;

	    ld_sop_d = 1'b0;
	    ld_data_d = 1'b0;
	    ld_eop_d = 1'b0;

	    case(state)
	      IDLE: begin
		  if(TxValid_i)
		    begin
			ld_sop_d = 1'b1;
			state = SOP;
		    end
	      end
	      SOP: begin
		  if(sft_done_e)
		    begin
			tx_ready_d = 1'b1;
			ld_data_d = 1'b1;
			state = DATA;
		    end
	      end
	      DATA: begin
		  if(!data_done & sft_done_e)
		    begin
			ld_eop_d = 1'b1;
			state = EOP1;
		    end

		  if(data_done & sft_done_e)
		    begin
			tx_ready_d = 1'b1;
			ld_data_d = 1'b1;
		    end
	      end
	      EOP1: begin
		  if(eop_done)		state = EOP2;
	      end
	      EOP2: begin
		  if(!eop_done & fs_ce)	state = WAIT;
	      end
	      WAIT: begin
		  if(fs_ce)			state = IDLE;
	      end
	    endcase
	end
    end

endmodule // usb_tx_phy

