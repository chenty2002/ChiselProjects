module am2910(I,CCEN_BAR,CC_BAR,RLD_BAR,CI,OEbar,clk,D,Y,PL_BAR,
	      VECT_BAR,MAP_BAR,FULL_BAR);
    input [3:0]	  I;
    input	  CCEN_BAR;
    input	  CC_BAR;
    input	  RLD_BAR;
    input	  CI;
    input	  OEbar;
    input	  clk;
    input [11:0]  D;
    output [11:0] Y;
    output	  PL_BAR;
    output	  VECT_BAR;
    output	  MAP_BAR;
    output	  FULL_BAR;

    reg [2:0]	  sp;			// stack pointer
    wire	  R_sel;
    wire	  D_sel;
    wire	  uPC_sel;
    wire	  stack_sel;
    wire	  decr;
    wire	  load;
    wire	  Rzero_bar;
    wire	  clear;
    wire	  push;
    wire	  pop;

    wire [11:0]   CI_ext;		// vl2mv fix
    wire [11:0]	  Y_temp;
    reg [11:0]	  RE;			// iteration counter
    reg [11:0]	  uPC;			// micro-program counter
    reg [11:0]	  reg_file[5:0];	// micro-program stack
    wire [2:0]	  write_address;
    wire	  fail;

    integer	  i;

    initial begin
	RE = 12'd0;
	uPC = 12'd0;
	sp = 3'd0;
	for (i = 0; i < 6; i = i + 1)
	    reg_file[i] = 12'd0;
    end // initial begin

    assign CI_ext[11:1] = 11'd0;
    assign CI_ext[0] = CI;
    assign Y_temp = R_sel ? RE : D_sel ? D : uPC_sel ? uPC :
	stack_sel ? reg_file[sp] : 12'd0;

    // Ignoring tri-state buffers.
    assign Y = Y_temp;

    always @ (posedge clk) begin
	if (load | ~RLD_BAR)
	    RE = D;
	else if (decr & RLD_BAR)
	    RE = RE - 12'd1;

	if (clear)
	    uPC = 12'd0;
	else
	    uPC = Y_temp + CI_ext;

	if (pop && sp != 3'd0)
	    sp = sp - 3'd1;
	else if (push & sp != 3'd5)
	    sp = sp + 3'd1;
	else if (clear)
	    sp = 3'd0;

	if (push)
	    reg_file[write_address] = uPC;
    end // always @ (posedge clk)

    assign Rzero_bar = |RE[11:0];
    assign write_address = (sp != 3'd5) ? sp + 3'd1 : sp;
    assign FULL_BAR = sp == 5;

    assign fail = CC_BAR & ~ CCEN_BAR;
    assign D_sel = 
	(I == 4'd2) |
	(Rzero_bar & (I == 4'd9)) |
	(~Rzero_bar & fail & (I == 4'd15)) |
	(~fail & ((I == 4'd1) | (I == 4'd3) | (I == 4'd5) | (I == 4'd7) |
		  (I == 4'd11)));
    assign uPC_sel =
	(I == 4'd4) | (I == 4'd12) | (I == 4'd14) |
	(fail & ((I == 4'd1) | (I == 4'd3) | (I == 4'd6) | (I == 4'd10) |
		 (I == 4'd11) | (I == 4'd14))) |
	(~Rzero_bar & ((I == 4'd8) | (I == 4'd9))) |
	(~fail & ((I == 4'd15) | (I == 4'd13)));
    assign stack_sel =
	(Rzero_bar & (I == 4'd8)) |
	(~fail & (I == 4'd10)) |
	(fail & (I == 4'd13)) |
	(Rzero_bar & fail & (I == 4'd15));
    assign R_sel = fail & ((I == 4'd5) | (I == 4'd7));
    assign push = (~fail & (I == 4'd1)) | (I == 4'd4) | (I == 4'd5);
    assign pop =
	(~fail & ((I == 4'd10) | (I == 4'd11) | (I == 4'd13) | (I == 4'd15))) |
	(~Rzero_bar & ((I == 4'd8) | (I == 4'd15)));
    assign load = (I == 4'd12) | (~fail & (I == 4'd4));
    assign decr = Rzero_bar & ((I == 4'd8) | (I == 4'd9) | (I == 4'd15));
    assign MAP_BAR = I == 4'd2;
    assign VECT_BAR = I == 4'd6;
    assign PL_BAR = (I == 4'd2) | (I == 4'd6);
    assign clear = I == 4'd0;

endmodule // am2910
