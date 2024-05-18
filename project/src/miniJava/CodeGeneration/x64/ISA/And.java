package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.ModRMSIB;

public class And extends SimpleMathInstruction {
	@Override
	protected SimpleMathOp _thisOp() {
		return SimpleMathOp.AND;
	}

	public And(ModRMSIB modrmsib) {
		super(modrmsib);
	}

	public And(ModRMSIB modrmsib, int imm) {
		super(modrmsib,imm);
	}
	
	public And(ModRMSIB modrmsib, int imm, boolean signExtend) {
		super(modrmsib,imm,signExtend);
	}
}
