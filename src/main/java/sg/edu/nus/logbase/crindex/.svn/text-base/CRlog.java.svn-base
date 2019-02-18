package sg.edu.nus.logbase.crindex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.regionserver.wal.LogEntryOffset;
import org.apache.hadoop.hbase.util.Bytes;

public class CRlog {
	
	private final List<CRrecord> records;		// set of CR-records in the CR-log
	private long num_records;			// the number of CR-records
	
	// for disk operations
	private final boolean onDisk;		// the structure is disk-based or not
	private final String location;		// the path of the on-disk file
	private final String fileName = "CRlog.log";	// the file name of the structure
	private File file;
	private FileOutputStream out;
	private RandomAccessFile in;
	
	private final int holeSize;	// the hole slot number in a CR-record
	
	public CRlog(boolean disk, String path, int hole){
		records = new ArrayList<CRrecord>();
		num_records = 0;
		
		onDisk = disk;
		location = path;
		
		holeSize = hole;
		
		if (onDisk){
			File dir = new File(path);
			
			if (!dir.exists()) dir.mkdir();
			
			for (File f : dir.listFiles()){
				f.delete();
			}
			
			file = new File(location+"/"+fileName);
			
			try{
				file.createNewFile();
			}catch(Exception e){
				LogIndexDebug.printError(e+"");
			}
			
		}
	}
	
//	private long cid;	// sequence ID of CR-record
//	private long block_length;	// number of tuples inside the block
//	private double minV, maxV;	// boundary pair of CR-record
//	private LogEntryOffset block_location;	// location of the represented block

	private byte[] buffer = null;
	
	
	private static final int id_offset = 0;
	private static final int length_offset = (Long.SIZE) >> 3;
	private static final int minV_offset = (Long.SIZE*2) >> 3;
	private static final int maxV_offset = (Long.SIZE*2+Double.SIZE) >> 3;
	private static final int entry_file_offset = (Long.SIZE*2+Double.SIZE*2) >> 3;
	private static final int entry_size_offset = (Long.SIZE*2+Double.SIZE*2+Integer.SIZE) >> 3;
	private static final int entry_offset_offset = (Long.SIZE*2+Double.SIZE*2+Integer.SIZE*2) >> 3;
	private static final int hole_offset = (Long.SIZE*2+Double.SIZE*2+Integer.SIZE*3) >> 3;
	private static final int fixed_record_length = (Long.SIZE*2+Double.SIZE*2+Integer.SIZE*4) >> 3;
	
	private static final int hole_length = (Double.SIZE*2) >> 3;
	
	//get byte representation of the record
	private byte[] recordToBytes(CRrecord r){
//		byte[] ret = new byte[fixed_record_length + hole_length*holeSize];
		if (buffer == null) buffer = new byte[fixed_record_length + hole_length*holeSize];
		
		Bytes.putLong(buffer, id_offset, r.getCid());
		Bytes.putLong(buffer, length_offset, r.getBlockLength());
		Bytes.putDouble(buffer, minV_offset, r.getMinValue());
		Bytes.putDouble(buffer, maxV_offset, r.getMaxValue());
		Bytes.putInt(buffer, entry_file_offset, r.getBlockLocation().getFileNum());
		Bytes.putInt(buffer, entry_size_offset, r.getBlockLocation().getSize());
		Bytes.putInt(buffer, entry_offset_offset, r.getBlockLocation().getOffset());
		
		//print hole information
		Bytes.putInt(buffer, hole_offset, r.getValidHole());
		
		for (int i = 0; i < r.getValidHole(); ++i){
			Bytes.putDouble(buffer, fixed_record_length + hole_length*i, r.getHoleMinV(i));
			Bytes.putDouble(buffer, fixed_record_length + hole_length*i + (hole_length>>1), r.getHoleMaxV(i));
		}
		
		return buffer;
	}
	
	// get record from byte representation
	private CRrecord bytesToRecord(byte[] b){
		
		long id = Bytes.toLong(b, id_offset);
		long length = Bytes.toLong(b, length_offset);
		double minV = Bytes.toDouble(b, minV_offset);
		double maxV = Bytes.toDouble(b, maxV_offset);
		int file = Bytes.toInt(b, entry_file_offset);
		int size = Bytes.toInt(b, entry_size_offset);
		int offset = Bytes.toInt(b, entry_offset_offset);
		
		int validHole = Bytes.toInt(b, hole_offset);
		
		CRrecord ret = new CRrecord(id, minV, maxV, new LogEntryOffset(file, size, offset), length, holeSize);
		
		for (int i = 0; i < validHole; ++i){
			ret.insertHole(Bytes.toDouble(b, fixed_record_length + hole_length*i), Bytes.toDouble(b, fixed_record_length + hole_length*i + (hole_length>>1)));
		}
		
		return ret;
	}
	
	public void add(CRrecord r){
		if (onDisk == false) records.add(r);
		else{	// on disk
			
			try{
				if (out == null) out = new FileOutputStream(file, true);
				out.write(recordToBytes(r));
				
			}catch(Exception e){
				LogIndexDebug.printError(e+"");
			}
			
		}
		++num_records;
	}
	
	public void flush(){
		
		try{
			if (out != null) out.flush();
		}catch(Exception e){
			LogIndexDebug.printError(e+"");
		}
	}
	
	public void close(){
		
		flush();
		
		try{
			if (in != null) in.close();
			in = null;
			if (out != null) out.close();
			out = null;
			
		}catch(Exception e){
			LogIndexDebug.printError(e+"");
		}
	}
	
	public CRrecord getRecord(int i){
		
		CRrecord ret = null;
		
		if (buffer == null) buffer = new byte[fixed_record_length + hole_length*holeSize];
		
		if (onDisk == false) ret = records.get(i);
		else{	// on disk
			try{
				if (in == null){
					in = new RandomAccessFile(file, "rw");
				}
				
				long pos = i*(fixed_record_length + hole_length*holeSize);
				
				if (in.getFilePointer() != pos){
					in.seek(pos);
				}
				
				in.read(buffer);
				ret = bytesToRecord(buffer);
				
			}catch(Exception e){
				LogIndexDebug.printError(e+"");
			}
		}
		
		return ret;
	}
	
	//wangsheng 2013-6-25: for hole information update
	public void updateRecord(int i, CRrecord r){
		
		if (onDisk == false) records.set(i, r);
		else{	// on disk
			try{
				if (in == null){
					in = new RandomAccessFile(file, "rw");
				}
				
				long pos = i*(fixed_record_length + hole_length*holeSize);
				
				if (in.getFilePointer() != pos){
					in.seek(pos);
				}
				
				in.write(recordToBytes(r));
				
			}catch(Exception e){
				LogIndexDebug.printError(e+"");
			}
		}
		
	}
	
	public long getNumberOfRecords() {return num_records;}
}
