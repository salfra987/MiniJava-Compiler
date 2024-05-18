package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Condition;
import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.x64;

public class CondJmp extends Instruction {
	public CondJmp(Condition cond, byte rel8) {
		opcodeBytes.write(getImm32Opcode(cond) - 0x10);
		immBytes.write(rel8);
	}
	
	public CondJmp(Condition cond, int rel32) {
		opcodeBytes.write(0x0F);
		opcodeBytes.write(getImm32Opcode(cond));
		x64.writeInt(immBytes,rel32);
	}
	
	public CondJmp(Condition cond, int curAddr, int destAddr, boolean asByte) {
		if( asByte ) {
			opcodeBytes.write( getImm32Opcode(cond) - 0x10 );
			immBytes.write( destAddr - curAddr - 2 );
			return;
		}
		opcodeBytes.write(0x0F);
		opcodeBytes.write(getImm32Opcode(cond));
		x64.writeInt(immBytes, destAddr - curAddr - 6);
	}
	
	// imm32
	// 84, 85: jz, jnz   / je, jne
	// 8C, 8D: jl, jnl   / jnge, jge
	// 8E, 8F: jle, jnle / jng, jg
	// imm8: subtract above by 0x10
	private int getImm32Opcode(Condition cond) {
		switch(cond) {
		case E: return 0x84;
		case NE: return 0x85;
		case LT: return 0x8C;
		case GTE: return 0x8D;
		case LTE: return 0x8E;
		case GT: return 0x8F;
		}
		
		throw new IllegalArgumentException("Illegal operator: " + cond);
	}
}
