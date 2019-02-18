package org.apache.hadoop.hbase.regionserver.wal;

import java.io.IOException;
import java.util.SortedMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.MemIndex;
import org.apache.hadoop.io.LongWritable;

import sg.edu.nus.test.Debug;

public class LogScannerByKey extends LogScanner {
	
	int size;
	MemIndex index = null; 
	KeyValue kv = new KeyValue();
	
	public LogScannerByKey() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf, MemIndex index, LogStoreCache cache) throws IOException {
		initial(outputfiles, fs, conf);
		this.index = index;
		this.logCache = cache;
//		setReaders(log);
	}
	
	@Override
	public void init(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf, MemIndex index) throws IOException {
		initial(outputfiles, fs, conf);
		this.index = index;
//		setReaders(log);
	}

	public void seek(KeyValue.Key key) throws IOException{
		LogEntryOffset logEntry = this.index.getOffset(key);
		this.size = logEntry.size;
		
		this.logCache.setFileNum(logEntry.filenum);
		this.logCache.setSize(logEntry.size);
		this.logCache.setOffset(logEntry.offset);
		
		//TODO: WangSheng: can use cache before seek and reduce the cost of seek time
		
		
		if (LogStoreCache.isEnable()){
			if (logCache.allInCache() == false){
//				System.out.println("need load cache");
				this.currentFileNum.set(logEntry.getFileNum());
				this.currentFileReader = this.getReader(this.currentFileNum);
//				this.currentFileReader.seek(logEntry.offset);
				logCache.loadCache(currentFileReader);
			}
		}
		else{
			this.currentFileNum.set(logEntry.getFileNum());
			this.currentFileReader = this.getReader(this.currentFileNum);
			this.currentFileReader.seek(logEntry.offset);
		}
		
		
	}
	
	/*
	public void seek(KeyValue.Key key) throws IOException{
		LogEntryOffset logEntry = this.index.getOffset(key);
		//VHTam
		if (logEntry == null){
			this.size = -1;//show that do not contain this key
		}
		else {
			this.size = logEntry.size;
			this.currentFileNum.set(logEntry.getFileNum());
			this.currentFileReader = this.getReader(this.currentFileNum);
			this.currentFileReader.seek(logEntry.offset);
			//Debug.debug("read filenum: " + currentFileNum);
		}
	}
	*/
	
	//WangSheng
	public void seek(LogEntryOffset logEntry) throws IOException{
		this.size = logEntry.size;
		
		this.logCache.setFileNum(logEntry.filenum);
		this.logCache.setSize(logEntry.size);
		this.logCache.setOffset(logEntry.offset);
		
		//TODO: WangSheng: can use cache before seek and reduce the cost of seek time
		
		
		if (LogStoreCache.isEnable()){
			if (logCache.allInCache() == false){
//				System.out.println("need load cache");
				this.currentFileNum.set(logEntry.getFileNum());
				this.currentFileReader = this.getReader(this.currentFileNum);
//				this.currentFileReader.seek(logEntry.offset);
				logCache.loadCache(currentFileReader);
			}
		}
		else{
			this.currentFileNum.set(logEntry.getFileNum());
			this.currentFileReader = this.getReader(this.currentFileNum);
			this.currentFileReader.seek(logEntry.offset);
		}
		
		
	}
	
	/*
	public void seek(LogEntryOffset logEntry) throws IOException {
		this.size = logEntry.size;
		this.currentFileNum.set(logEntry.getFileNum());
		this.currentFileReader = this.getReader(this.currentFileNum);
		this.currentFileReader.seek(logEntry.offset);
		//Debug.debug("read filenum: " + currentFileNum);
	}
	*/
	
	//WangSheng
	@Override
	public KeyValue next() throws IOException {
		//VHTam
		if (this.size == -1) {
			return null;
		} else {
			kv = new KeyValue();
			if (LogStoreCache.isEnable()) this.currentFileReader.next(this.kv, this.size, this.logCache);
			else this.currentFileReader.next(this.kv, this.size);
			return this.kv;
		}
	}
	
	/*
	@Override
	public KeyValue next() throws IOException {
		//VHTam
		if (this.size == -1) {
			return null;
		} else {
			this.currentFileReader.next(this.kv, this.size);
			return this.kv;
		}
	}
	*/

} 
