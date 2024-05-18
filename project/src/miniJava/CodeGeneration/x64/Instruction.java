package miniJava.CodeGeneration.x64;

import java.io.ByteArrayOutputStream;

public abstract class Instruction {
	protected ByteArrayOutputStream opcodeBytes = new ByteArrayOutputStream();
	protected ByteArrayOutputStream immBytes = new ByteArrayOutputStream();
	protected boolean rexW = false;
	protected boolean rexR = false;
	protected boolean rexX = false;
	protected boolean rexB = false;
	public int startAddress;
	public int listIdx;
	private int _size = -1;
	
	// caching could be done better here, instructions are "kinda" immutable
	public int size() {
		if( _size == -1 )
			_size = getBytes().length;
		return _size;
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		Byte rex = getRex();
		if( rex != null )
			b.write(rex);
		if( opcodeBytes.size() > 0 )
			x64.writeBytes(b, opcodeBytes.toByteArray());
		if( immBytes.size() > 0 )
			x64.writeBytes(b, immBytes.toByteArray() );
		byte[] retBytes = b.toByteArray();
		_size = retBytes.length;
		return retBytes;
	}
	
	private Byte getRex() {
		if( !( rexW || rexX || rexB || rexR ) )
			return null;
		return (byte)((4 << 4) | (rexW ? 1 << 3 : 0) | (rexR ? 1 << 2 : 0) | (rexX ? 1 << 1 : 0) | (rexB ? 1 : 0));
	}
	
	protected void importREX(ModRMSIB rm64) {
		rexW = rexW || rm64.getRexW();
		rexR = rexR || rm64.getRexR();
		rexX = rexX || rm64.getRexX();
		rexB = rexB || rm64.getRexB();
	}
}
