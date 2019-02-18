package org.apache.hadoop.hbase.regionserver.wal;

import java.io.DataInput;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.util.StringUtils;

public class LogStoreCache {

	public static final Log LOG = LogFactory.getLog(LogStoreCache.class);
	
	private static long cacheHeapSize = 64*1024*1024;
	private static int blockSize = 64*1024;
	private static boolean enable = false; //future work.  original value here is true.
	private static float cachePercentage = 0.2f;
	private int blockNum = 0;
	
	private class CacheInf{
//		private int cacheID;
		private BlockInf blk = null;
		private long timeStamp = 0;
		
		
		private CacheInf(BlockInf b, long t){
			blk = b;
			timeStamp = t;
		}
	}
	
	private class BlockInf{
		private int file;
		private int block;
		
		private BlockInf(int f, int b){
			file = f;
			block = b;
		}
		public String toString(){
			return Integer.toString(file)+"|"+Integer.toString(block);
		}
	}

	private Map<String,Integer>block2ID = null;
	private CacheInf[] cacheInf = null;
	private byte[][] blockBuffer = null;
	private PriorityQueue<CacheInf>cacheQueue = null;
	private Comparator<CacheInf>cmp = new Comparator<CacheInf>(){
		public int compare(CacheInf a, CacheInf b){
			if (a.timeStamp < b.timeStamp) return -1;
			if (a.timeStamp == b.timeStamp) return 0;
			return 1;
		}
	};
	
	private int currentFileNum = 0;
	private int currentOffset = 0;
	private int currentSize = 0;
	
	public static void setCacheHeapSize(long size){
		cacheHeapSize = size;
	}
	
	public static void setBlockSize(int size){
		blockSize = size;
	}
	
	public static void setCacheEnable(boolean flag){
		enable = flag;
	}
	
	public static boolean isEnable(){
		return enable;
	}
	
	public void setFileNum(int value){
		currentFileNum = value;
	}
	
	public void setOffset(int value){
		currentOffset = value;
	}
	
	public void setSize(int value){
		currentSize = value;
	}
	
//	public String blockInf(int file, int block){
//		return Integer.toString(file)+"|"+Integer.toString(block);
//	}
	
	public LogStoreCache(){
		
		if (enable){
			MemoryUsage mu = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		    cacheHeapSize = (long)(mu.getMax() * cachePercentage);
			blockNum = (int)(cacheHeapSize/blockSize);
			
			cacheInf = new CacheInf[blockNum];
			blockBuffer = new byte[blockNum][blockSize];
			cacheQueue = new PriorityQueue<CacheInf>(blockNum, cmp);
			block2ID = new HashMap<String,Integer>();
			
			System.out.println("Log Cache Has Set Up!");
			System.out.println("Cache Size = "+StringUtils.humanReadableInt(cacheHeapSize));
			System.out.println("Cache block size = "+blockSize);
		}
		
	}
	
	
	public boolean allInCache(){

		LOG.info("call allInCache");
		
		boolean allInCache = true;
		
		int i;
		int front = currentOffset/blockSize;
		int rear = (currentOffset+currentSize-1)/blockSize;
		
//		synchronized(cacheQueue){
		
		for (i = front; i <= rear; ++i){
			BlockInf blk = new BlockInf(currentFileNum, i);
			if (block2ID.containsKey(blk.toString()) == false){
				allInCache = false;
				continue;
			}
			int id = block2ID.get(blk.toString());
			cacheQueue.remove(cacheInf[id]);
			cacheInf[id].timeStamp = System.currentTimeMillis();
			cacheQueue.add(cacheInf[id]);
		}
//		}
		return allInCache;
	}
	
//	public boolean isAllInCache(){
//		return allInCache;
//	}
	private void loadBlock(byte[] data, BlockInf blk, SequenceFileLogReader currentFileReader){

//		synchronized (currentFileReader){
		try{
			currentFileReader.seek(blk.block*blockSize);
		}catch(Exception e){
			System.out.println("Log Cache can not read the file");
		}
		DataInput in = currentFileReader.reader.getInputStream();
		int len = 0;
		try{
			len = (int)(currentFileReader.reader.getDataEnd()-currentFileReader.reader.getPosition());
		}catch(Exception e){
			System.out.println("Log Cache can not read the file");
		}
		if (len > blockSize) len = blockSize;
		try{
			in.readFully(data, 0, len);
		}catch(Exception e){
			System.out.println("Log Cache can not read the file");
		}
//		}
	}
	
	public void loadCache(SequenceFileLogReader currentFileReader){


		LOG.info("call loadCache");

		int i;
		int front = currentOffset/blockSize;
		int rear = (currentOffset+currentSize-1)/blockSize;
		
//		synchronized (cacheQueue){
		for (i = front; i <= rear; ++i){
			BlockInf blk = new BlockInf(currentFileNum, i);
			if (block2ID.containsKey(blk.toString()) == true) continue;
			
			if (cacheQueue.size() < blockNum){
				int id = cacheQueue.size();
				block2ID.put(blk.toString(), id);
				cacheInf[id] = new CacheInf(blk, System.currentTimeMillis());
				cacheQueue.add(cacheInf[id]);
				loadBlock(blockBuffer[id], blk, currentFileReader);
			}
			else{
				CacheInf c = cacheQueue.poll();
				if (c == null) System.out.println("!");
//				System.out.println("block ("+c.blk.file+","+c.blk.block+") has removed from cache");
				int id = block2ID.get(c.blk.toString());
				block2ID.remove(c.blk.toString());
				block2ID.put(blk.toString(), id);
				cacheInf[id] = new CacheInf(blk, System.currentTimeMillis());
				cacheQueue.add(cacheInf[id]);
				loadBlock(blockBuffer[id], blk, currentFileReader);
			}
//			System.out.println("block ("+blk.file+","+blk.block+") has load into cache");
		}
//		}
	}
	
	public void loadKeyValue(KeyValue kv){

		LOG.info("call loadKeyValue");
		
		int length = currentSize-4;
//		System.out.println("length = "+length);
		byte[] bytes = new byte[length];
		
		int pos = 0;
		int block = (currentOffset+4)/blockSize;
		int offset = (currentOffset+4)%blockSize;
		
//		synchronized (cacheQueue){
		while (pos < length){
//			if (pos != 0) System.out.println("load another block");
			int len = length-pos;
			int len2 = blockSize-offset;
			if (len > len2) len = len2;
//			System.out.println("need to load block ("+currentFileNum+","+block+") from cache");
			int id = block2ID.get(new BlockInf(currentFileNum, block).toString());
			System.arraycopy(blockBuffer[id], offset, bytes, pos, len);
			pos += len;
			++block;
			offset = 0;
		}
//		}
		
		kv.setValue(bytes, length);
	}
	
}
