package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.x64;

public class Ret extends Instruction {
	public Ret() {
		opcodeBytes.write( 0xC3 ); // TODO: what is the opcode for return with no size
	}
	
	public Ret(short imm16, short mult) {
		opcodeBytes.write( 0xC2 ); // TODO: what is the opcode for return with some size
		x64.writeShort(immBytes,imm16*mult);
	}
	
	public Ret(short imm16) {
		this(imm16,(short)8);
	}
}
