package org.apache.hadoop.hbase.regionserver.wal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.regionserver.KeyValueScanner;
import org.apache.hadoop.hbase.regionserver.MemIndex;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import sg.edu.nus.logbase.crindex.LogIndexInterface;

import java.io.IOException;
import java.util.*;

public class LogBaseScanner implements KeyValueScanner, InternalScanner{

	public static final Log LOG = LogFactory.getLog(LogBaseScanner.class);
	
	int TempCont=0;
	
	//copyed from LogScanner.java
	
	MemIndex index = null;
	protected SortedMap<LongWritable, SequenceFileLogReader> readers = null;
	  
	protected SortedMap<LongWritable, Path> files = null;
	FileSystem fs = null;
	Configuration conf = null;
	
	LongWritable currentFileNum = new LongWritable();
	SequenceFileLogReader currentFileReader = null;
	
	//WangSheng
	LogStoreCache logCache = null;
	
	//2013-12-13 wangsheng
	LogIndexInterface CRindex = null;
	
	//copyed from LogScannerByKey.java
	
	HLog.Entry entry = new HLog.Entry();
	List<KeyValue> kvs = null;
	List<LogEntryOffset> keyvalueOffset = null;
	KeyValue.Key tmpKey = new KeyValue.Key();
	int kvIdx = 0;
	long beginOffset;
	
	//wangsheng 2013-4-26
	KeyValue peek = null;
	Scan scan;
	Set<byte[]> cols;
	byte[] family;
	
	//wangsheng 2013-6-1
	//check by file scan or by key scan
	enum Type {byKey, byScan, byIndex};
	final Type type;
	KeyValue.Key nextKey;
	boolean firstTime = true;
	List<KeyValue> kvs_crindex;
	int pos_crindex;

	// longyongchao
	private HLog currentLogTemp;
	
	void initial(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf) throws IOException{
	    if(readers != null){
	      close();
	    }else {
	      readers = new TreeMap<LongWritable, SequenceFileLogReader>();
	    }
	    this.files = outputfiles;
	    this.fs = fs;
	    this.conf = conf;
	    //huanghao
//	    this.kvs = null;
//	    this.kvIdx = 0;
//	    this.keyvalueOffset = null;
//	    this.entry  = new HLog.Entry();
//	    this.currentFileNum = new LongWritable();
//	    this.currentFileReader = null;
//	    this.tmpKey = new KeyValue.Key();
	  }
	
	//WangSheng
	public LogBaseScanner(byte[] family, Scan scan, HLog currentLog, MemIndex index, LogStoreCache cache, LogIndexInterface CRindex)throws IOException{
		//if (scan.blocked==true) System.out.println("++//++//++//++//++//++//++//++//++//++//++//++//");
		SortedMap<LongWritable, Path> outputfiles = currentLog.outputfiles;
		
		FileSystem fs = currentLog.getFileSystem();
		
		Configuration conf = currentLog.getConf();
		initial(outputfiles, fs, conf);
		this.currentLogTemp = currentLog;
		this.scan = scan;
	    this.index = index;
	    this.currentFileNum = this.files.firstKey();	    
	    this.currentFileReader = this.getReader(this.currentFileNum);
	    
	    this.logCache = cache;
	    this.CRindex = CRindex;
	    
	    this.cols = scan.getFamilyMap().get(family);
	    
	    //2013-12-13 wangsheng
	    //System.out.println("LogBaseScanner.init CRindex = "+scan.CRindex+" low = "+scan.low+" high = "+scan.high);
	    
	    //2013-12-13 wangsheng
	    if (scan.CRindex == true) type = Type.byIndex;
	    else if (Bytes.compareTo(scan.getStopRow(), Bytes.toBytes("")) != 0 && Bytes.compareTo(scan.getStartRow(), scan.getStopRow()) == 0){
	    	type = Type.byKey;
	    	KeyValue tmp = new KeyValue(scan.getStartRow(), 0L);
	    	nextKey = tmp.getKeyForLogBaseMemIndex();
	    	nextKey = index.kvIndex.ceilingKey(nextKey);
//	    	LOG.info("LogBaseScanner construct set the nextKey : " + nextKey);
	    }
	    else type = Type.byScan;
	    
	    next();
	}
	
	protected SequenceFileLogReader getReader(LongWritable logFileNum) throws IOException{

		//longyongchao
		Path path = currentLogTemp.getDir();
		if (currentLogTemp.getFilenum() !=  logFileNum.get()) {
			path = this.files.get(logFileNum);
			if(path == null){
				throw new IOException("Invalid file num: " + logFileNum);
			}
		}

	  	SequenceFileLogReader ret = this.readers.get(logFileNum);
	  	if(ret == null){
	  		ret = new SequenceFileLogReader();
	  		ret.init(this.fs, path, this.conf);
	  		this.readers.put(new LongWritable(logFileNum.get()), ret);
	  	}
	  	return ret;
	  }
	
	//huanghao
	protected SequenceFileLogReader getReader(LongWritable logFileNum, long offset) throws IOException{
		//longyongchao
		Path path = currentLogTemp.getDir();
		if (currentLogTemp.getFilenum() !=  logFileNum.get()) {
			path = this.files.get(logFileNum);
			if(path == null){
				throw new IOException("Invalid file num: " + logFileNum);
			}
		}

	  	SequenceFileLogReader ret = this.readers.get(logFileNum);
	  	if(ret == null){
	  		ret = new SequenceFileLogReader();
	  		ret.init(this.fs, path, this.conf, offset);
	  		this.readers.put(new LongWritable(logFileNum.get()), ret);
	  	}
	  	return ret;
	  }
	
	  
	@Override
	public KeyValue peek() {
		
		return peek;
	}
	
	@Override
	public KeyValue next() throws IOException{
		
		KeyValue tmp = peek;
		boolean needContinue = true;
		
		do{
			if (type == Type.byIndex) {
//				LOG.info("Type.byIndex");
				peek = pre_next_byIndex();
			}
			else if (type == Type.byScan) {
//				LOG.info("Type.byScan");
				if (scan.regionalScan){
					peek = pre_next_for_regional_scan();
				}else{
					//TODO:
					peek = pre_next();//for general scan
				}
			}
			else {
//				LOG.info("Type. pre_next_byKey ");
				peek = pre_next_byKey();//for get
			}
			
			
			if (peek == null) {
//				LOG.info("peek == null");
				break;
			}
//			LOG.info("k = " + Bytes.toString(peek.getRow()) + " c = " + Bytes.toString(peek.getQualifier())+ " v = " + Bytes.toString(peek.getValue()));
			
			needContinue = false;
			//System.out.println("needContinue0 = " + needContinue);
			if (cols != null && !cols.isEmpty() && cols.contains(peek.getQualifier()) == false) {
//				LOG.info("needContinue 1: " + cols.contains(peek.getQualifier()));
//				for(byte[] b : cols) {
//					LOG.info("cols: " + new String(b));
//				}
				needContinue = true;
			}
			else if (Bytes.compareTo(peek.getRow(), scan.getStartRow()) < 0) {
//				LOG.info("needContinue 2, peek row: " + new String(peek.getRow()) + " can.getStartRow(): " + new String(scan.getStartRow()));
				needContinue = true;//wangsheng
			}
			//System.out.println("needContinue2 = " + needContinue);
			else if (Bytes.compareTo(scan.getStopRow(), Bytes.toBytes("")) != 0 && Bytes.compareTo(peek.getRow(), scan.getStopRow()) > 0) {
//				LOG.info("NO needContinue 3, scan.getStopRow() " + new String(scan.getStopRow()) +
//						" peek.getRow(): " + new String(peek.getRow()));
//				needContinue = true;//wangsheng
				needContinue = false;
			}
			else{
//				LOG.info("needContinue = false");
				needContinue = false;
			}

		}while (needContinue);
		
		
		return tmp;
	}
	
	private KeyValue pre_next() throws IOException {//for scan
		while(true)
		{
	  		if(kvs != null && kvIdx < kvs.size())
	  		{
	  			KeyValue kv = kvs.get(kvIdx);
	  			tmpKey = kv.getKeyForLogBaseMemIndex();
	  			LogEntryOffset memIndexOffset = this.index.getOffset(tmpKey);
	  			LogEntryOffset currentOffset = this.keyvalueOffset.get(kvIdx);
	  			kvIdx ++;
	  			if(memIndexOffset.compareTo(currentOffset) != 0){
	  				continue;
	  			}else
	  				return kv;
	  		}
	  		this.beginOffset = this.currentFileReader.getPosition();//currentFileReader can be updated, beginOffset=entry.offset
	  		HLog.Entry ret = this.currentFileReader.next(entry);
	  		if(ret == null)
	  		{
	  			// reach the final file
	  			if(this.currentFileNum.compareTo(this.files.lastKey()) == 0){
	  				return null;
	  			}
	  			// move to another log file
	  			LongWritable tmpFileNum = new LongWritable();
	  			tmpFileNum.set(this.currentFileNum.get() + 1);
	  			this.currentFileNum = this.files.tailMap(tmpFileNum).firstKey();
	  			this.currentFileReader = this.getReader(this.currentFileNum);
	  			continue;
	  		}else{
	  			kvs = entry.getEdit().getKeyValues();
	  			keyvalueOffset = entry.getKeyValueOffset(beginOffset, (int)currentFileNum.get(), keyvalueOffset);
	  			kvIdx = 0;
	  			continue;
	  		}
		}//end while
	}
	
	
	//huanghao: for blocked scan
	private KeyValue pre_next_for_regional_scan() throws IOException {
		HLog.Entry ret = null;
		while(true)
		{
	  		if(kvs != null && kvIdx < kvs.size())
	  		{
	  			KeyValue kv = kvs.get(kvIdx);
	  			tmpKey = kv.getKeyForLogBaseMemIndex();
	  			LogEntryOffset memIndexOffset = this.index.getOffset(tmpKey);
	  			LogEntryOffset currentOffset = this.keyvalueOffset.get(kvIdx);
	  			kvIdx ++;
	  			if(memIndexOffset.compareTo(currentOffset) != 0){
	  				continue;
	  			}else
	  				return kv;
	  		}
	  		if (this.TempCont==0){
	  			// get LogEntryOffset by row
	  			this.TempCont ++;
		    	KeyValue tmp = new KeyValue(scan.getStartRow(), 0L);
		    	nextKey = tmp.getKeyForLogBaseMemIndex();
		    	nextKey = index.kvIndex.ceilingKey(nextKey);
		    	LogEntryOffset LEOffset = index.kvIndex.get(nextKey);
		    	
		    	
		    	// update fileReader and fileNum
		    	this.currentFileReader = this.getReader(new LongWritable(LEOffset.filenum));
		    	this.currentFileReader.seek(LEOffset.offset-35);
		    	//this.currentFileReader = this.getReader(new LongWritable(LEOffset.filenum), LEOffset.offset+4);
		    	
		    	
		    	LongWritable tmpFileNum = new LongWritable();
	  			tmpFileNum.set((long)LEOffset.filenum);
	  			this.currentFileNum = this.files.tailMap(tmpFileNum).firstKey();		    	
	    	
		    	// update (1) beginOffset & (2) keyvalueOffset
		    	this.beginOffset = (long)LEOffset.offset - 35;  
		    	
		    	//System.out.println("------[[[ beginOffset"+beginOffset);
		    	//System.out.println("------[[[ LEOffset.offset"+LEOffset.offset);
		    	
		    	keyvalueOffset =new ArrayList<LogEntryOffset>();
		    	keyvalueOffset.add(LEOffset);		    			    			    	
		    	
		    	ret = this.currentFileReader.SetUpEntry(entry, LEOffset.logSeqNum, LEOffset.offset);		    	

	  		}else{
	  			//update beginOffset & HLog.Entry
	  			this.beginOffset = this.currentFileReader.getPosition();
	  			ret = this.currentFileReader.next(entry);
	  		}
	  		
	  		if(ret == null)
	  		{
	  			// reach the final file
	  			if(this.currentFileNum.compareTo(this.files.lastKey()) == 0)  return null;

	  			// move to another log file
	  			LongWritable tmpFileNum = new LongWritable();
	  			tmpFileNum.set(this.currentFileNum.get() + 1);
	  			this.currentFileNum = this.files.tailMap(tmpFileNum).firstKey();
	  			this.currentFileReader = this.getReader(this.currentFileNum);
	  			continue;
	  		}else{
	  			//get kvs, and update keyvalueOffset
	  			kvs = entry.getEdit().getKeyValues();
	  			keyvalueOffset = entry.getKeyValueOffset(beginOffset, (int)currentFileNum.get(), keyvalueOffset);
	  			kvIdx = 0;	  			
	  			continue;
	  		}
		}//end while
	}
	
	
	
	private KeyValue pre_next_byKey() throws IOException {//for get

		if (nextKey == null) {
			return null;
		} else {
			int o = nextKey.getRowOffset();
			short l = nextKey.getRowLength();
			byte[] nextRow = new byte[l];
			System.arraycopy(nextKey.getBuffer(), o, nextRow, 0, l);
//			LOG.info("pre_next_byKey : " + new String(nextRow) + "; and scan stop row: " + new String(scan.getStopRow()));
			if (Bytes.compareTo(nextRow, scan.getStopRow()) > 0) {
				nextKey = null;
				return null;
			}
		}

		LogEntryOffset entry = index.kvIndex.get(nextKey);

		if (entry == null) {
//			LOG.info("pre_next_byKey but null");
			return null;
		} else {
//			LOG.info("pre_next_byKey,  " + entry + "; logSeqNum: " + entry.logSeqNum + "; nextkey:" + nextKey.toString());
		}
		
		KeyValue ret = null;
		
		nextKey  = index.kvIndex.higherKey(nextKey);//update the key for the next next() function
		
		LongWritable currentFileNum = new LongWritable();
		currentFileNum.set(entry.getFileNum());
		this.currentFileReader = this.getReader(new LongWritable(entry.filenum));
//		LOG.info("currentFileReader seek and read next");
		this.currentFileReader.seek(entry.offset);

		ret = this.currentFileReader.next(ret, entry.size);//get the kv here

		if (Bytes.compareTo(scan.getStartRow(),ret.getRow()) > 0) {
			ret = null;
			nextKey = null;
		}
		
		return ret;
	}
	
	private KeyValue pre_next_byIndex() throws IOException {//for indexed scan
		
		if (firstTime == true){
			firstTime = false;
			List<byte[]> list = new ArrayList<byte[]>();
			List<Double> minV = new ArrayList<Double>();
			List<Double> maxV = new ArrayList<Double>();
			
			
			list.add(scan.idxColumn);
			minV.add(scan.low);
			maxV.add(scan.high);
			kvs_crindex = CRindex.rangeQueryByIndex(list, minV, maxV);
			pos_crindex = 0;

		}
		
		if (pos_crindex < kvs_crindex.size()){
			return kvs_crindex.get(pos_crindex++);
		}
		
		return null;
	}

	@Override
	public boolean seek(KeyValue key) throws IOException {
		
		return true;
	}

	@Override
	public boolean reseek(KeyValue key) throws IOException {
		
		return true;
	}

	@Override
	public long getSequenceID() {
		
		return 0;
	}

	@Override
	public void close() {
		//System.out.println("**************************close logbasescanner is running");
		
	  

			// move to another log file
			//LongWritable tmpFileNum = new LongWritable();
			//tmpFileNum.set(this.currentFileNum.get() + 1);
			//this.currentFileNum = this.files.lastKey();//this.files.tailMap(tmpFileNum).firstKey();
			//this.currentFileReader = this.getReader(this.currentFileNum);
		
		
		Collection<SequenceFileLogReader> rs = readers.values();
	    for(SequenceFileLogReader r : rs){
	    	try{
	    		r.close();
	    	}catch(Exception e){
	    		System.err.println(e);
	    	}
	    }
	    readers.clear();
	}

	@Override
	public boolean next(List<KeyValue> results) throws IOException {
		
		return next(results, -1);
	}

	@Override
	public boolean next(List<KeyValue> result, int limit) throws IOException {

//		LOG.info("next(List<KeyValue> result, int limit) : limit: " + limit);

		KeyValue kv = null;
		
		result.clear();
		
		while (limit != 0){
			--limit;
			kv = next();
			if (kv == null) {
//				LOG.info("ext(List<KeyValue> result, int limit), kv == null ,break");
				break;
			}
			result.add(kv);
			if (peek == null) {
//				LOG.info("ext(List<KeyValue> result, int limit), peek == null ,break");
				break;
			}
			if (Bytes.equals(peek.getRow(), kv.getRow()) == false) {
//				LOG.info("ext(List<KeyValue> result, int limit), Bytes.equals(peek.getRow(), kv.getRow()) == false ,break");
				break;
			}

		}
		
		return true;
	}

}