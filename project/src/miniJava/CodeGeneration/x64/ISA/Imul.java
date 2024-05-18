package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.Reg;
import miniJava.CodeGeneration.x64.x64;

public class Imul extends Instruction {
	// imul rm, where RDX:RAX:= RAX * rm
	public Imul(ModRMSIB rm) {
		rm.SetRegR(x64.mod543ToReg(5));
		opcodeBytes.write(0xF7);
		byte[] rmsib = rm.getBytes();
		importREX(rm);
		x64.writeBytes(immBytes,rmsib);
	}
	
	// imul r,rm, where r:= r * rm
	public Imul(Reg r, ModRMSIB rm) {
		opcodeBytes.write(0x0F);
		opcodeBytes.write(0xAF);
		rm.SetRegR(r);
		byte[] rmsib = rm.getBytes();
		importREX(rm);
		x64.writeBytes(immBytes,rmsib);
	}
	
	// imul r,rm,imm, where r:= rm * imm
	public Imul(Reg r, ModRMSIB rm, int imm) {
		rm.SetRegR(r);
		byte[] rmsib = rm.getBytes();
		importREX(rm);
		x64.writeBytes(immBytes,rmsib);
		
		if( x64.isOneByte(imm) ) {
			opcodeBytes.write(0x6B);
			immBytes.write((int)imm);
		} else  {
			opcodeBytes.write(0x69);
			x64.writeInt(immBytes,imm);
		}
	}
}
