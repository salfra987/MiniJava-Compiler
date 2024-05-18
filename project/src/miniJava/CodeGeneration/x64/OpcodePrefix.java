package miniJava.CodeGeneration.x64;

// Won't need this for PA4, but PA5 may be helpful
public enum OpcodePrefix {
	LOCK(0xF0),
	REPNE(0xF2), REPNZ(0xF2),
	REP(0xF3), REPE(0xF3), REPZ(0xF3),
	CS(0x2E), SS(0x36), DS(0x3E), ES(0x26), // these won't work when we're in 64-bit mode
	FS(0x64), GS(0x65), BranchNotTaken(0x3E), BranchTaken(0x3E),
	OperandSizeOverride(0x66),
	AddressSizeOverride(0x67);
	
	public int prefix;
	private OpcodePrefix(int prefix) {
		this.prefix = prefix;
	}
}
