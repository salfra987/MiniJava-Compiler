package miniJava.CodeGeneration.x64;

public enum Reg32 implements Reg {
	EAX(0),
	ECX(1),
	EDX(2),
	EBX(3),
	ESP(4),
	EBP(5),
	ESI(6),
	EDI(7),
	R8D(8),
	R9D(9),
	R10D(10),
	R11D(11),
	R12D(12),
	R13D(13),
	R14D(14),
	R15D(15);
	
	public final int idx;
	private Reg32(int idx) {
		this.idx = idx;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public int size() {
		return 4;
	}
	
	public static Reg RegFromIdx(int idx, boolean r8Base) {
		if( r8Base ) {
			switch(idx) {
				case 0: return R8D;
				case 1: return R9D;
				case 2: return R10D;
				case 3: return R11D;
				case 4: return R12D;
				case 5: return R13D;
				case 6: return R14D;
				case 7: return R15D;
			};
			return null;
		}
		
		switch(idx) {
			case 0: return EAX;
			case 1: return ECX;
			case 2: return EDX;
			case 3: return EBX;
			case 4: return ESP;
			case 5: return EBP;
			case 6: return ESI;
			case 7: return EDI;
		};
		return null;
	}
	
	public static Reg RegFromIdx(int idx) {
		return RegFromIdx(idx,false);
	}
}
