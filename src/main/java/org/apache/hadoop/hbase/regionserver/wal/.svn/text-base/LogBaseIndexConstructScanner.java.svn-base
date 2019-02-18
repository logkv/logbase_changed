package org.apache.hadoop.hbase.regionserver.wal;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.io.LongWritable;

//created in 2013-12-30 for CR-index construct - wangsheng
public class LogBaseIndexConstructScanner{
	
	int TempCont=0;
	
	//copyed from LogScanner.java

	protected SortedMap<LongWritable, SequenceFileLogReader> readers = null;
	protected SortedMap<LongWritable, Path> files = null;
	FileSystem fs = null;
	Configuration conf = null;
	
	LongWritable currentFileNum = new LongWritable();
	SequenceFileLogReader currentFileReader = null;
	
	//copyed from LogScannerByKey.java
	
	HLog.Entry entry = new HLog.Entry();
	List<KeyValue> kvs = null;
	List<LogEntryOffset> keyvalueOffset = null;
	//KeyValue.Key tmpKey = new KeyValue.Key();
	int kvIdx = 0;
	long beginOffset;
	
	void initial(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf) throws IOException{
	    if(readers != null){
	      close();
	    }else {
	      readers = new TreeMap<LongWritable, SequenceFileLogReader>();
	    }
	    this.files = outputfiles;
	    this.fs = fs;
	    this.conf = conf;
	  }
	
	//WangSheng
	public LogBaseIndexConstructScanner(HLog currentLog)throws IOException{
		
		SortedMap<LongWritable, Path> outputfiles = currentLog.outputfiles;
		
		FileSystem fs = currentLog.getFileSystem();
		
		Configuration conf = currentLog.getConf();
		initial(outputfiles, fs, conf);
	    this.currentFileNum = this.files.firstKey();	    
	    this.currentFileReader = this.getReader(this.currentFileNum);
	}
	
	protected SequenceFileLogReader getReader(LongWritable logFileNum) throws IOException{
	  	Path path = this.files.get(logFileNum);
	  	if(path == null){
	  		throw new IOException("Invalid file num: " + logFileNum);
	  	}
	  	SequenceFileLogReader ret = this.readers.get(logFileNum);
	  	if(ret == null){
	  		ret = new SequenceFileLogReader();
	  		ret.init(this.fs, path, this.conf);
	  		this.readers.put(new LongWritable(logFileNum.get()), ret);
	  	}
	  	return ret;
	  }
	
	public boolean next(KeyValue kv, LogEntryOffset offset) throws IOException{
		
		while(true)
		{
	  		if(kvs != null && kvIdx < kvs.size())
	  		{
	  			kv = kvs.get(kvIdx);
	  			offset = keyvalueOffset.get(kvIdx);
	  			return true;
	  		}
	  		this.beginOffset = this.currentFileReader.getPosition();//currentFileReader can be updated, beginOffset=entry.offset
	  		HLog.Entry ret = this.currentFileReader.next(entry);
	  		if(ret == null)
	  		{
	  			// reach the final file
	  			if(this.currentFileNum.compareTo(this.files.lastKey()) == 0){
	  				return false;
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

	public void close() {
		
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
}