package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.ModRMSIB;

public class Sub extends SimpleMathInstruction {
	@Override
	protected SimpleMathOp _thisOp() {
		return SimpleMathOp.SUB;
	}
	
	public Sub(ModRMSIB modrmsib) {
		super(modrmsib);
	}

	public Sub(ModRMSIB modrmsib, int imm) {
		super(modrmsib,imm);
	}
}
