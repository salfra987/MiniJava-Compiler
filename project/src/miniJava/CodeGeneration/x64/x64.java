package miniJava.CodeGeneration.x64;

import java.io.ByteArrayOutputStream;

public class x64 {
	public static Reg mod543ToReg(int mod543) {
		if( mod543 > 7 )
			throw new IllegalArgumentException("ModRM543 bits must be in [0,7]");
		return Reg32.RegFromIdx(mod543); // Reg32 so that REX flags don't get accidentally set
	}
	
	public static int getIdx(Reg r) {
		if( r == null ) return 0;
		return r.getIdx() > 7 ? r.getIdx() - 8 : r.getIdx();
	}
	
	public static boolean isOneByte(long v) {
		return v >= Byte.MIN_VALUE && v < Byte.MAX_VALUE; // [-128,127]
	}
	
	public static boolean isOneByte(int v) {
		return v >= Byte.MIN_VALUE && v < Byte.MAX_VALUE; // [-128,127]
	}
	
	public static boolean isInt(long l) {
		return l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE;
	}
	
	public static void writeShort(ByteArrayOutputStream b, int n) {
		writeShort(b,(short)n);
	}
	
	public static void writeShort(ByteArrayOutputStream b, short n) {
		for( int i = 0; i < 2; ++i ) {
			b.write( n & 0xFF );
			n >>= 8;
		}
	}
	
	public static void writeInt(ByteArrayOutputStream b, int n) {
		for( int i = 0; i < 4; ++i ) {
			b.write( n & 0xFF );
			n >>= 8;
		}
	}
	
	public static void writeLong(ByteArrayOutputStream b, long n) {
		for( int i = 0; i < 8; ++i ) {
			b.write( (int)(n & 0xFF) );
			n >>= 8;
		}
	}
	
	public static void writeLong(ByteArrayOutputStream b, int n) {
		writeLong(b,(long)n);
	}
	
	public static void writeBytes(ByteArrayOutputStream b, byte[] bArray) {
		if( bArray == null ) return;
		
		for( int i = 0; i < bArray.length; ++i )
			b.write( bArray[i] );
	}
	
	public static void writeString(ByteArrayOutputStream b, String s) {
		for( int i = 0; i < s.length(); ++i )
			b.write( (int)s.charAt(i) );
		b.write(0);
	}
}
