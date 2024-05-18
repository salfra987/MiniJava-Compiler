package miniJava.CodeGeneration.x64;

public enum Reg64 implements Reg {
	RAX(0),
	RCX(1),
	RDX(2),
	RBX(3),
	RSP(4),
	RBP(5),
	RSI(6),
	RDI(7),
	R8(8),
	R9(9),
	R10(10),
	R11(11),
	R12(12),
	R13(13),
	R14(14),
	R15(15),
	RIP(-1); // special purpose, actual value is 5 in special cases

	public final int regIdx;
	private Reg64(int idx) {
		regIdx = idx;
	}
	
	public int getIdx() {
		return regIdx;
	}
	
	public int size() {
		return 8;
	}
	
	public static Reg RegFromIdx(int idx, boolean r8Base) {
		if( r8Base ) {
			switch( idx ) {
				case 0: return R8;
				case 1: return R9;
				case 2: return R10;
				case 3: return R11;
				case 4: return R12;
				case 5: return R13;
				case 6: return R14;
				case 7: return R15;
			};
			return null;
		}
		
		switch(idx) {
			case 0: return RAX;
			case 1: return RCX;
			case 2: return RDX;
			case 3: return RBX;
			case 4: return RSP;
			case 5: return RBP;
			case 6: return RSI;
			case 7: return RDI;
		};
		return null;
	}
	
	public static Reg RegFromIdx(int idx) {
		return RegFromIdx(idx,false);
	}
}
