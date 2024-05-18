package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.ModRMSIB;

public class Cmp extends SimpleMathInstruction {
	@Override
	protected SimpleMathOp _thisOp() {
		return SimpleMathOp.CMP;
	}

	public Cmp(ModRMSIB modrmsib) {
		super(modrmsib);
	}

	public Cmp(ModRMSIB modrmsib, int imm) {
		super(modrmsib,imm);
	}
}
