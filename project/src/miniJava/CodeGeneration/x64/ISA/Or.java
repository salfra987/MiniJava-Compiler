package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.ModRMSIB;

public class Or extends SimpleMathInstruction {
	@Override
	protected SimpleMathOp _thisOp() {
		return SimpleMathOp.OR;
	}
	
	public Or(ModRMSIB modrmsib) {
		super(modrmsib);
	}

	public Or(ModRMSIB modrmsib, int imm) {
		super(modrmsib,imm);
	}
}
