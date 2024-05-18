package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.x64;

public class Not extends Instruction {
	public Not(ModRMSIB modrmsib) {
		opcodeBytes.write(0xF7);
		modrmsib.SetRegR(x64.mod543ToReg(2));
		byte[] rmsib = modrmsib.getBytes();
		importREX(modrmsib);
		x64.writeBytes(immBytes,rmsib);
	}
}
