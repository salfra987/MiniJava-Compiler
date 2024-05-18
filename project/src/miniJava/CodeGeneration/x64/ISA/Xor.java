package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.ModRMSIB;

public class Xor extends SimpleMathInstruction {
	@Override
	protected SimpleMathOp _thisOp() {
		return SimpleMathOp.XOR;
	}
	
	public Xor(ModRMSIB modrmsib) {
		super(modrmsib);
	}

	public Xor(ModRMSIB modrmsib, int imm) {
		super(modrmsib,imm);
	}
}
