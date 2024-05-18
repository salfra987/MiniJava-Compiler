package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.ModRMSIB;

public class Add extends SimpleMathInstruction {
	@Override
	protected SimpleMathOp _thisOp() {
		return SimpleMathOp.ADD;
	}
	
	public Add(ModRMSIB modrmsib) {
		super(modrmsib);
	}

	public Add(ModRMSIB modrmsib, int imm) {
		super(modrmsib,imm);
	}
}
