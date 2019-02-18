package org.apache.hadoop.hbase.regionserver;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.wal.LogEntryOffset;
import org.apache.hadoop.hbase.regionserver.wal.LogScanner;
import org.apache.hadoop.hbase.regionserver.wal.LogScannerByKey;

public class MyKVStore {

	/**
	 * this factor will be multiply with the default block size of file system
	 * default block size ubuntu: 32MB
	 */
	float LOG_MULTIPLIER = 0.95f;

	public Store store = null;
	Path tableDir = null;
	HColumnDescriptor columnDescription = null;

	HRegion emptyRegion = null;

	//boolean local = false;
	Configuration conf = null; 
	FileSystem fs = null;

	byte[] family = null;

	String tableName = "table";

	//AtomicLong numRecord = new AtomicLong(0);
	//AtomicLong dataSize = new AtomicLong(0);

	boolean deleteOldFolder = true;

	LogScannerByKey seeker = null;


	public MyKVStore(Configuration conf, String tableName, String colFamily, boolean deleteOldFolder, float logMutiplier) throws IOException {
		
		this.conf = conf;
		this.tableName = tableName;
		//this.local = local;
		this.deleteOldFolder = deleteOldFolder;
		this.family = colFamily.getBytes();
		
		this.LOG_MULTIPLIER = logMutiplier;
		this.conf.set("hbase.regionserver.logroll.multiplier", ""+this.LOG_MULTIPLIER);
		
		init();
	}

	public void init() throws IOException {

//		if (this.local) {
//			fs = FileSystem.getLocal(conf);
//		} else {
//			//fs = FileSystem.get(URI.create("hdfs://awan-1-23-0:12345/"), conf);
//			conf.addResource(new Path("/home/epic/HadoopInstall/hadoop-config/core-site.xml"));
//			Debug.debug("hdfs: " + conf.get("fs.default.name", "xxx"));
//			
//			fs = FileSystem.get(conf);
//		}
		
		fs = FileSystem.get(conf);
		
		
		String tableFolder = conf.get("table.dir", "/tmp");
		this.tableDir = new Path(tableFolder, tableName);
		
		if (deleteOldFolder) {
			fs.delete(tableDir, true);
		}
		
		columnDescription = new HColumnDescriptor(family);
		emptyRegion = new HRegion(tableDir, null, fs, conf, new HRegionInfo(),
				null);

		store = new Store(new Path(tableDir, new String(family)), emptyRegion,
				columnDescription, fs, conf);

		//System.out.println("family length: " + familyLength);

		seeker = (LogScannerByKey) store.getLogScannerByKey();
		// this.printLogFiles();
	}

	public void close() throws IOException {
		store.close();
		seeker.close();
	}

//	public void printStoreInfo() {
//		System.out.println("home dir: " + store.getHomedir() + "\nregion: "
//				+ store.getHRegion() + "\nregionInfo: "
//				+ store.getHRegionInfo());
//	}

	public void put(byte[] id, byte[] qualifier, byte[] value)
			throws IOException {

		KeyValue record = createKeyValue(id, family, qualifier, value);

		List<KeyValue> kvs = new ArrayList<KeyValue>();
		kvs.add(record);
		store.add(kvs);

	}
	
	public void put(byte[][] id, byte[][] qualifier, byte[][] value) throws IOException {

		if (id.length != qualifier.length || id.length != value.length){
			System.out.println("Put denied! each vector should have same length");
			return;
		}
		
		KeyValue record;
		List<KeyValue> kvs = new ArrayList<KeyValue>();
		
		for (int i = 0; i < id.length; ++i){
			record = createKeyValue(id[i], family, qualifier[i], value[i]);
			kvs.add(record);
		}

		store.add(kvs);

	}

	static private Object lock = new Object();
	
	public KeyValue get(byte[] id, byte[] qualifier) throws IOException {
		KeyValue kv = null;
		KeyValue.Key key = null;
		kv = this.createKeyValue(id, this.family, qualifier, new byte[1]);
		key = kv.getKeyForLog();

		// check whether memindex contains key or not
		LogEntryOffset logEntry = store.memIndex.getOffset(key);
		if (logEntry == null) {
			return null;//found no key
		}

		// check whether it belongs to current files (not rolled out yet)
		if (logEntry.getFileNum() == store.getLog().getCurrentFileNumber()) {
			// now return null;
			//TODO: in the future can check in the write cache.
			return null;
		}

		KeyValue ret = null;
		
		synchronized (lock){
			seeker.seek(logEntry);
			ret = seeker.next();
		}
		
		return ret;

	}

	public ArrayList<KeyValue> get(byte[] startid, byte[] endid, byte[] qualifier) throws IOException {
		// now return null;
		//TODO: in the future, search memindex from startid to endid, and follow these pointers to get records		
		return null;
	}
	
	public void persistLatestData() throws Exception {
		// close the current log files so that the file is seen by other also
		// usefull when bulkload data, before perform 100% read
		store.getLog().rollWriter();
	}

	public LogScanner getLogScannerByFile() throws IOException {
		return store.getLogScannerByFile();
	}

	public void printLogFiles() {
		seeker.printLogFiles();
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	/**
	 * Here just create the keyvalue, without any initialization
	 * 
	 * @param row
	 * @param columnFamily
	 * @param columnQualifier
	 * @param value
	 * @return
	 */
	public KeyValue createKeyValue(byte[] row, byte[] columnFamily,
			byte[] columnQualifier, byte[] value) {
		return new KeyValue(row, columnFamily, columnQualifier, value);
	}

}
