package miniJava.CodeGeneration.x64;

public enum Reg8 implements Reg {
	AL(0),
	CL(1),
	DL(2),
	BL(3),
	AH(4),
	CH(5),
	DH(6),
	BH(7);
	
	public final int idx;
	private Reg8(int idx) {
		this.idx = idx;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public int size() {
		return 1;
	}
	
	public static Reg RegFromIdx(int idx) {
		switch(idx) {
			case 0: return AL;
			case 1: return CL;
			case 2: return DL;
			case 3: return BL;
			case 4: return AH;
			case 5: return CH;
			case 6: return BH;
			case 7: return DH;
		};
		return null;
	}
}
