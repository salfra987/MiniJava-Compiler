package miniJava.CodeGeneration.x64;

import java.io.ByteArrayOutputStream;

public class ModRMSIB {
	private ByteArrayOutputStream _b;
	private boolean rexW = false;
	private boolean rexR = false;
	private boolean rexX = false;
	private boolean rexB = false;
	
	public boolean getRexW() {
		return rexW;
	}
	
	public boolean getRexR() {
		return rexR;
	}
	
	public boolean getRexX() {
		return rexX;
	}
	
	public boolean getRexB() {
		return rexB;
	}
	
	public byte[] getBytes() {
		_b = new ByteArrayOutputStream();
		// construct
		if( rdisp != null && ridx != null && r != null )
			Make(rdisp,ridx,mult,disp,r);
		else if( ridx != null && r != null )
			Make(ridx,mult,disp,r);
		else if( rdisp != null && r != null )
			Make(rdisp,disp,r);
		else if( rm != null && r != null )
			Make(rm,r);
		else if( r != null )
			Make(disp,r);
		else throw new IllegalArgumentException("Cannot determine ModRMSIB");
		
		return _b.toByteArray();
	}
	
	private Reg64 rdisp = null, ridx = null;
	private Reg rm = null, r = null;
	private int disp = 0, mult = 0;
	
	// [rdisp+ridx*mult+disp],r32/64
	public ModRMSIB(Reg64 rdisp, Reg64 ridx, int mult, int disp, Reg r) {
		SetRegR(r);
		SetRegDisp(rdisp);
		SetRegIdx(ridx);
		SetDisp(disp);
		SetMult(mult);
	}
	
	// r must be set by some mod543 instruction set later
	// [rdisp+ridx*mult+disp]
	public ModRMSIB(Reg64 rdisp, Reg64 ridx, int mult, int disp) {
		SetRegDisp(rdisp);
		SetRegIdx(ridx);
		SetDisp(disp);
		SetMult(mult);
	}
	
	// [rdisp+disp],r
	public ModRMSIB(Reg64 rdisp, int disp, Reg r) {
		SetRegDisp(rdisp);
		SetRegR(r);
		SetDisp(disp);
	}
	
	// r will be set by some instruction to a mod543
	// [rdisp+disp]
	public ModRMSIB(Reg64 rdisp, int disp) {
		SetRegDisp(rdisp);
		SetDisp(disp);
	}
	
	// rm64,r64
	public ModRMSIB(Reg64 rm, Reg r) {
		SetRegRM(rm);
		SetRegR(r);
	}
	
	// rm or r
	public ModRMSIB(Reg64 r_or_rm, boolean isRm) {
		if( isRm )
			SetRegRM(r_or_rm);
		else
			SetRegR(r_or_rm);
	}
	
	public int getRMSize() {
		if( rm == null ) return 0;
		return rm.size();
	}
	
	//public ModRMSIB() {
	//}
	
	public void SetRegRM(Reg rm) {
		if( rm.getIdx() > 7 ) rexB = true;
		rexW = rexW || rm instanceof Reg64;
		this.rm = rm;
	}
	
	public void SetRegR(Reg r) {
		if( r.getIdx() > 7 ) rexR = true;
		rexW = rexW || r instanceof Reg64;
		this.r = r;
	}
	
	public void SetRegDisp(Reg64 rdisp) {
		if( rdisp.getIdx() > 7 ) rexB = true;
		this.rdisp = rdisp;
	}
	
	public void SetRegIdx(Reg64 ridx) {
		if( ridx.getIdx() > 7 ) rexX = true;
		this.ridx = ridx;
	}
	
	public void SetDisp(int disp) {
		this.disp = disp;
	}
	
	public void SetMult(int mult) {
		this.mult = mult;
	}
	
	public boolean IsRegR_R8() {
		return r instanceof Reg8;
	}
	
	public boolean IsRegR_R64() {
		return r instanceof Reg64;
	}
	
	public boolean IsRegRM_R8() {
		return rm instanceof Reg8;
	}
	
	public boolean IsRegRM_R64() {
		return rm instanceof Reg64;
	}
	
	// rm,r
	private void Make(Reg rm, Reg r) {
		int mod = 3;
		
		int regByte = ( mod << 6 ) | ( getIdx(r) << 3 ) | getIdx(rm);
		_b.write( regByte ); 
	}
	
	// [rdisp+disp],r
	private void Make(Reg64 rdisp, int disp, Reg r) {
		// TODO: construct the byte and write to _b
		// Operands: [rdisp+disp],r
		int mod;
		if(disp == 0){
			mod = 0;
		} else if(disp < 128 && disp > -127){
			mod = 1;
		} else {
			mod = 2;
		}

		int regByte = (mod << 6) | (getIdx(r) << 3) | x64.getIdx(rdisp);

		_b.write(regByte);
	}
	
	// [ridx*mult+disp],r
	private void Make( Reg64 ridx, int mult, int disp, Reg r ) {
		if( !(mult == 1 || mult == 2 || mult == 4 || mult == 8) )
			throw new IllegalArgumentException("Invalid multiplier value: " + mult);
		if( ridx == Reg64.RSP )
			throw new IllegalArgumentException("Index cannot be rsp");
		
		// TODO: construct the modrm byte and SIB byte
		// Operands: [ridx*mult + disp], r
		int mod, ss;

		if(disp == 0){
			mod = 0;
		} else if(disp < 128 || disp > -127){
			mod = 1;
		} else {
			mod = 2;
		}

		int regByte = (mod << 6) | (getIdx(r) << 3);


	}
	
	// [rdisp+ridx*mult+disp],r
	private void Make( Reg64 rdisp, Reg64 ridx, int mult, int disp, Reg r ) {
		if( !(mult == 1 || mult == 2 || mult == 4 || mult == 8) )
			throw new IllegalArgumentException("Invalid multiplier value: " + mult);
		if( ridx == Reg64.RSP )
			throw new IllegalArgumentException("Index cannot be rsp");
		
		// TODO: construct the modrm byte and SIB byte
		// Operands: [rdisp + ridx*mult + disp], r
		int mod, ss;

		if(disp == 0){
			mod = 0;
		} else if(disp < 128 || disp > -127){
			mod = 1;
		} else {
			mod = 2;
		}

		int regByte = (mod << 6) | (getIdx(r) << 3) | x64.getIdx(rdisp);

		if(mult==1){
			ss=0;
		} else if(mult == 2){
			ss=1;
		} else if(mult == 4){
			ss=2;
		} else{
			ss=3;
		}


		_b.write(regByte);
		_b.write((ss<<6) | (x64.getIdx(ridx)<<3) | x64.getIdx(rdisp));
	}
	
	// [disp],r
	private void Make( int disp, Reg r ) {
		_b.write( ( getIdx(r) << 3 ) | 4 );
		_b.write( ( 4 << 3 ) | 5 ); // ss doesn't matter
		writeInt(_b,disp);
	}
	
	private int getIdx(Reg r) {
		return x64.getIdx(r);
	}
	
	// TODO: This is a duplicate declaration from x64.writeInt
	//  You should remove this, but the reason it is here is so that
	//  you can immediately see what it does, and so you know what
	//  is available to you in the x64 class.
	private void writeInt(ByteArrayOutputStream b, int n) {
		for( int i = 0; i < 4; ++i ) {
			b.write( n & 0xFF );
			n >>= 8;
		}
	}
}
