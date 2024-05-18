package miniJava.CodeGeneration.x64.ISA;

import miniJava.CodeGeneration.x64.Instruction;

public class Syscall extends Instruction {
	public Syscall() {
		// TODO: syscall is two bytes
		opcodeBytes.write(0x0f);
		opcodeBytes.write(0x05);
	}
}
