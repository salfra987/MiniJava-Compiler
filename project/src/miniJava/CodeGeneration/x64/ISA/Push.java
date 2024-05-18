package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.Reg64;
import miniJava.CodeGeneration.x64.x64;

public class Push extends Instruction {
	public Push(int imm) {
		// TODO: how can we do a push imm32?
		opcodeBytes.write(0xFF);
		immBytes.write(imm);
	}
	
	public Push(Reg64 reg) {
		// no need to set rexW, push is always r64 (cannot access ecx/r9d)
		if( reg.getIdx() > 7 )
			rexB = true;
		opcodeBytes.write(0x50 + x64.getIdx(reg));
	}
	
	public Push(ModRMSIB modrmsib) {
		// no need to set rexW, push is always r64 (cannot access ecx/r9d)
		opcodeBytes.write(0xFF);
		
		modrmsib.SetRegR(x64.mod543ToReg(6));
		byte[] rmsib = modrmsib.getBytes();
		importREX(modrmsib);
		x64.writeBytes(immBytes,rmsib);
	}
}
