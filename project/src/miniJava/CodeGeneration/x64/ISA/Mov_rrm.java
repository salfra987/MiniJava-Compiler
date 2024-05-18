package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.x64;

public class Mov_rrm extends Instruction {
	// r,rm variants
	public Mov_rrm(ModRMSIB modrmsib) {
		byte[] modrmsibBytes = modrmsib.getBytes();
		importREX(modrmsib);
		opcodeBytes.write(0x8B);
		x64.writeBytes(immBytes,modrmsibBytes);
	}
}
