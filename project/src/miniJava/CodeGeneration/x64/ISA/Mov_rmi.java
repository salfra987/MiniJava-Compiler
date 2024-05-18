package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.x64;

public class Mov_rmi extends Instruction {
	// rm,imm32 variants
	public Mov_rmi(ModRMSIB modrmsib, int imm) {
		modrmsib.SetRegR(x64.mod543ToReg(0));
		byte[] modrmsibBytes = modrmsib.getBytes();
		importREX(modrmsib);
		
		if( x64.isOneByte(imm) && modrmsib.IsRegRM_R8() ) {
			// mov rm8, imm8
			opcodeBytes.write(0xC6);
			x64.writeBytes(immBytes,modrmsibBytes);
			immBytes.write(imm);
			return;
		}
		
		// mov rm64, imm32
		opcodeBytes.write(0xC7);
		x64.writeBytes(immBytes,modrmsibBytes);
		x64.writeInt(immBytes,imm);
	}
}
