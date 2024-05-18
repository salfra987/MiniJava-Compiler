package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;
import miniJava.CodeGeneration.x64.ModRMSIB;
import miniJava.CodeGeneration.x64.x64;

public class Call extends Instruction {
	public Call(int offset) {
		opcodeBytes.write(0xE8);
		x64.writeInt(immBytes,offset);
	}
	
	public Call(int curAddr, int destAddr) {
		opcodeBytes.write(0xE8);
		x64.writeInt(immBytes, destAddr - curAddr - 5);
	}
	
	public Call(ModRMSIB modrmsib) {
		opcodeBytes.write(0xFF);
		
		modrmsib.SetRegR(x64.mod543ToReg(2));
		byte[] rmsib = modrmsib.getBytes();
		importREX(modrmsib);
		x64.writeBytes(immBytes,rmsib);
	}
}
