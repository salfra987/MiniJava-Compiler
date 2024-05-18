package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.x64;

public class Jmp extends Instruction {
	// jmp (some register combination)
	public Jmp(ModRMSIB modrmsib) {
		opcodeBytes.write(0xFF);
		
		modrmsib.SetRegR(x64.mod543ToReg(4));
		byte[] rmsib = modrmsib.getBytes();
		importREX(modrmsib);
		x64.writeBytes(immBytes,rmsib);
	}
	
	// jmp imm32 (offset from next instruction)
	public Jmp(int offset) {
		opcodeBytes.write(0xE9);
		x64.writeInt(immBytes,offset);
	}
	
	// jmp imm8 (offset from next instruction)
	public Jmp(byte offset) {
		opcodeBytes.write(0xEB);
		immBytes.write(offset);
	}
	
	// jmp imm8/32 (offset calculated)
	public Jmp(int curAddr, int destAddr, boolean asByte) {
		if( asByte ) {
			opcodeBytes.write(0xEB);
			immBytes.write( destAddr - curAddr - 2 );
			return;
		}
		opcodeBytes.write(0xE9);
		x64.writeInt(immBytes, destAddr - curAddr - 5);
	}
}
