package miniJava.CodeGeneration;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import miniJava.ErrorReporter;
import miniJava.CodeGeneration.x64.x64;

// position independent ELF maker
public class ELFMaker {
	private ErrorReporter _errors;

	private ELF elf = new ELF();
	private ArrayList<ELFSection> sections = new ArrayList<ELFSection>();
	private ArrayList<ELFSegment> segments = new ArrayList<ELFSegment>();
	private ELFSection text = new ELFSection();
	private ELFSection bss = new ELFSection();
	private ELFSection shstrtab = new ELFSection();
	private ELFSegment phdr = new ELFSegment();
	private ELFSegment textSeg = new ELFSegment();
	private long phStartAddress = 0x40;
	private long shStartAddress;
	private long sdataStartAddress;
	private long bssOffset;
	
	public ELFMaker(ErrorReporter errors, long textSize, long bssSize) {
		this._errors = errors;
		
		// first section is the null section
		sections.add( makeNullSection() );
		
		segments.add(phdr);
		segments.add(textSeg);
		
		// next is the .text
		text.sectionName = ".text";
		text.sh_size = textSize;
		text.sh_flags = ??; // TODO: what flags does the text section get?
		text.sh_type = ??; // TODO: what type is the text section?
		text.data = new byte[1]; // placeholder, do not change
		sections.add( text );
		
		// make .bss
		bss.sectionName = ".bss";
		bss.data = null;
		bss.sh_size = bssSize;
		bss.sh_type = ??; // TODO: what type is the bss section?
		bss.sh_flags = ??; // TODO: what are the flags of the bss section?
		sections.add( bss );
		
		// make .shstrtab
		shstrtab.sectionName = ".shstrtab";
		shstrtab.sh_type = ??; // TODO: what is the type of the shstrtab section?
		shstrtab.sh_flags = ??; // TODO: what are the flags of this section?
		sections.add( shstrtab );
		shstrtab.data = makeSectionStrings(sections);
		shstrtab.sh_size = shstrtab.data.length;
		
		// -=-=-=-=-=-
		// No new segments/sections beyond this point
		// -=-=-=-=-=-
		
		shStartAddress = phStartAddress + ( elf.e_phentsize * segments.size() );
		sdataStartAddress = shStartAddress + ( elf.e_shentsize * sections.size() );
		long pSectionSizes = 0;
		long vSectionSizes = 0;
		int secIdx = 0;
		for( ELFSection sh : sections ) {
			sh.sh_addr = sdataStartAddress + vSectionSizes;
			sh.sh_offset = sdataStartAddress + pSectionSizes;
			sh.secIdx = secIdx;
			secIdx++;
			if( sh.data != null )
				pSectionSizes += sh.sh_size;
			vSectionSizes += sh.sh_size;
		}
		
		// I mean, it isn't really bss, it's got/plt, but it will eventually
		// resolve to bss when we check RIP+(entry-textStart+textSize)
		bssOffset = bss.sh_addr - text.sh_addr;
	}
	
	// entrypoint offset from start of text section
	public void outputELF(String fname, byte[] textSection, long entrypoint) {
		if( textSection.length != text.sh_size )
			throw new IllegalArgumentException("Passed text section does not match earlier sh_size");
		
		// -=-=-=-=-=-=-=-=-=-=-
		//    NO MORE CHANGES BEYOND THIS POINT
		// -=-=-=-=-=-=-=-=-=-=-
		
		text.data = textSection;
		
		phdr.p_type = ??; // TODO: what is the type of the program header segment?
		phdr.p_flags = ??; // TODO: what are the flags of the program header segment?
		phdr.p_offset = phStartAddress;
		phdr.p_vaddr = phStartAddress;
		phdr.p_paddr = phStartAddress;
		phdr.p_filesz = segments.size() * elf.e_phentsize;
		phdr.p_memsz = phdr.p_filesz;
		
		textSeg.p_type = ??; // TODO: type of the text segment?
		textSeg.p_flags = ??; // TODO: flags for the text segment?
		textSeg.p_offset = text.sh_offset;
		textSeg.p_vaddr = text.sh_addr;
		textSeg.p_paddr = text.sh_addr;
		textSeg.p_filesz = text.sh_size;
		textSeg.p_memsz = text.sh_size;
		
		elf.e_entry = text.sh_addr + entrypoint;
		elf.e_shoff = shStartAddress;
		elf.e_phnum = (short)segments.size();
		elf.e_shnum = (short)sections.size();
		elf.e_shstrndx = (short)shstrtab.secIdx;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeELF(out,elf);
		for( ELFSegment ph : segments )
			writeSegment(out,ph);
		for( ELFSection sh : sections )
			writeSection(out,sh);
		
		// dump out each section's data now
		for( ELFSection sh : sections ) {
			if( sh.data == null || sh.sh_size == 0 )
				continue;
			write(out,sh.data);
		}
		
		try {
			FileOutputStream f = new FileOutputStream(fname);
			f.write( out.toByteArray() );
			f.close();
		} catch( FileNotFoundException e) {
			_errors.reportError("FileNotFoundException: " + e);
		} catch (IOException e) {
			_errors.reportError("IOException: " + e);
		}
	}
	
	public long getBssOffset() {
		return bssOffset;
	}
	
	private byte[] makeSectionStrings(ArrayList<ELFSection> sections) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		_b = b;
		for( ELFSection e : sections ) {
			e.sh_name = b.size();
			if( e.sectionName != null )
				write(e.sectionName);
			else
				write((byte)0);
		}
		_b = null;
		return b.toByteArray();
	}
	
	private class ELF {
		public int ei_magic = 0x464C457F; // 7F, 'E', 'L', 'F'
		public byte ei_class = 0x02; // 64bit
		public byte ei_data = 0x01; // little endian
		public byte ei_version = 0x01; // elf latest
		public byte ei_osabi = 0x00; // 0= Unix-System V, 3= Linux
		public byte ei_abiversion = 0x00;
		public byte[] ei_padding = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		public short e_type = 0x03; // 3=shared object, 2= executable
		public short e_machine = 0x3E; // x86_64
		public int e_version = 0x01; // 1= original ELF
		public long e_entry; // where is entrypoint
		public long e_phoff = 0x40; // where is prog header
		public long e_shoff; // where is start of section table
		public int e_flags = 0x00; // no flags
		public short e_ehsize = 0x40; // pe64's elf header size
		public short e_phentsize = 0x38; // prog header entry size
		public short e_phnum; // number of program headers
		public short e_shentsize = 0x40; // section header entry size
		public short e_shnum; // number of sections
		public short e_shstrndx; // index of the shstrtab section
	}
	
	private class ELFSegment {
		public int p_type = 1; // 1=PT_LOAD, loadable segment, 6= PT_PHDR
		public int p_flags; // 1, 2, 4 respectively X, W, R
		public long p_offset = 0; // offset of the segment in the file image
		public long p_vaddr; // virtual address of this segment
		public long p_paddr; // physical address
		public long p_filesz = 0; // size of segment in the file image
		public long p_memsz = 0; // size of the bytes in memory
		public long p_align = 0; // alignment in memory
	}
	
	private class ELFSection {
		public int sh_name; // offset in .shstrtab section
		public int sh_type; // 0x01= program data, 0x03= string table, 0x08=SHT_NOBITS
		public long sh_flags; // 0x01= write, 0x02= allocated, 0x04= exec, 0x20= null terminated strings, 0= n/a
		public long sh_addr; // virt address of section in memory, must be loaded segment
		public long sh_offset; // offset of the section in the file image
		public long sh_size; // size in bytes
		public int sh_link = 0; // section index, not needed
		public int sh_info = 0; // extra info, not needed
		public long sh_addralign = 0; // section alignment, must be power of 2
		public long sh_entsize = 0; // size of each entry for sections that have fixed-size entries
		
		// these aren't a part of the table entry
		public String sectionName;
		public byte[] data;
		public int secIdx;
	}
	
	private void writeELF(ByteArrayOutputStream b, ELF e) {
		_b = b;
		write(e.ei_magic);
		write(e.ei_class);
		write(e.ei_data);
		write(e.ei_version);
		write(e.ei_osabi);
		write(e.ei_abiversion);
		write(e.ei_padding);
		write(e.e_type);
		write(e.e_machine);
		write(e.e_version);
		write(e.e_entry);
		write(e.e_phoff);
		write(e.e_shoff);
		write(e.e_flags);
		write(e.e_ehsize);
		write(e.e_phentsize);
		write(e.e_phnum);
		write(e.e_shentsize);
		write(e.e_shnum);
		write(e.e_shstrndx);
		_b = null;
	}
	
	private void writeSegment(ByteArrayOutputStream b, ELFSegment e) {
		_b = b;
		write(e.p_type);
		write(e.p_flags);
		write(e.p_offset);
		write(e.p_vaddr);
		write(e.p_paddr);
		write(e.p_filesz);
		write(e.p_memsz);
		write(e.p_align);
		_b = null;
	}
	
	private void writeSection(ByteArrayOutputStream b, ELFSection e) {
		_b = b;
		write(e.sh_name);
		write(e.sh_type);
		write(e.sh_flags);
		write(e.sh_addr);
		write(e.sh_offset);
		write(e.sh_size);
		write(e.sh_link);
		write(e.sh_info);
		write(e.sh_addralign);
		write(e.sh_entsize);
		_b = null;
	}
	
	// Below are methods to maximize laziness
	private ByteArrayOutputStream _b = null;
	private void write(int v) {
		write(_b,v);
	}
	private void write(long v) {
		write(_b,v);
	}
	private void write(byte v) {
		write(_b,v);
	}
	private void write(byte[] v) {
		write(_b,v);
	}
	private void write(short v) {
		write(_b,v);
	}
	private void write(String v) {
		write(_b,v);
	}
	private void write(ByteArrayOutputStream b, int v) {
		x64.writeInt(b, v);
	}
	private void write(ByteArrayOutputStream b, long v) {
		x64.writeLong(b, v);
	}
	private void write(ByteArrayOutputStream b, byte v) {
		b.write(v);
	}
	private void write(ByteArrayOutputStream b, byte[] v) {
		x64.writeBytes(b, v);
	}
	private void write(ByteArrayOutputStream b, short v) {
		x64.writeShort(b, v);
	}
	private void write(ByteArrayOutputStream b, String v) {
		x64.writeString(b, v);
	}
	
	private static final int PT_NULL 	= 0;
	private static final int PT_LOAD 	= 1;
	private static final int PT_DYNAMIC = 2;
	private static final int PT_NOTE 	= 4;
	//private static final int PT_SHLIB = 5;
	private static final int PT_PHDR 	= 6;
	private static final int PT_TLS 	= 7;
	
	private static final int PF_X = 1;
	private static final int PF_W = 2;
	private static final int PF_R = 4;
	
	private static final int SHT_NULL 	= 0x00;
	private static final int SHT_PROGBITS = 0x01;
	private static final int SHT_SYMTAB = 0x02;
	private static final int SHT_STRTAB = 0x03;
	private static final int SHT_RELA 	= 0x04;
	private static final int SHT_DYNAMIC = 0x06;
	private static final int SHT_NOTE 	= 0x07;
	private static final int SHT_NOBITS = 0x08;
	private static final int SHT_REL 	= 0x09;
	private static final int SHT_DYNSYM = 0x0B;
	
	private static final long SHF_WRITE 	= 0x01;
	private static final long SHF_ALLOC 	= 0x02;
	private static final long SHF_EXECINSTR = 0x04;
	private static final long SHF_STRINGS 	= 0x20;
	private static final long SHF_INFO_LINK = 0x40;
	private static final long SHF_LINK_ORDER = 0x80;
	
	private ELFSection makeNullSection() {
		ELFSection e = new ELFSection();
		e.sectionName = null;
		e.data = null;
		
		e.sh_name = 0;
		e.sh_type = ??; // TODO: what type is the null section?
		e.sh_flags = 0;
		e.sh_addr = 0;
		e.sh_offset = 0;
		e.sh_size = 0;
		return e;
	}
}
