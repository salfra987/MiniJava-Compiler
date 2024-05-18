package miniJava.CodeGeneration.x64;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class InstructionList {
	private List<Instruction> _instructions = new ArrayList<Instruction>();
	private int _currentSize = 0;
	private int _currentIdx = 0;
	private int _markStart = -1;
	
	public int getSize() {
		return _currentSize;
	}
	
	public int add(Instruction ins) {
		ins.startAddress = _currentSize;
		ins.listIdx = _currentIdx;
		_instructions.add(ins);
		_currentIdx++;
		_currentSize += ins.size();
		return ins.listIdx;
	}
	
	public Instruction get(int idx) {
		return _instructions.get(idx);
	}
	
	public void markOutputStart() {
		_markStart = _currentIdx;
	}
	
	public void outputFromMark() {
		if( _markStart < 0 ) return;
		
		for( int i = _markStart; i < _currentIdx; ++i ) {
			Instruction ins = _instructions.get(i);
			byte[] barr = ins.getBytes();
			System.out.printf( "%04X %s\t", ins.startAddress, ins.getClass().getSimpleName() );
			renderBytes(barr);
			System.out.println();
		}
		
		markOutputStart(); // next invocation will start after the output bytes
	}
	
	public void patch(int idx, Instruction newIns) {
		Instruction old = _instructions.get(idx);
		int newLen = newIns.size();
		int oldLen = old.size();
		if( oldLen != newLen ) {
			// very dangerous, maybe even worth reporting
			_currentSize += newLen - oldLen;
		}
		newIns.startAddress = old.startAddress;
		newIns.listIdx = idx;
		_instructions.set(idx, newIns);
	}
	
	public byte[] getBytes() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		
		for( Instruction op : _instructions )
			x64.writeBytes(b, op.getBytes());
		
		return b.toByteArray();
	}
	
	private void renderBytes(byte[] bArray) {
		for( int i = 0; i < bArray.length; ++i )
			System.out.printf("%02X ", bArray[i]);
	}
}
