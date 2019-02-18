package sg.edu.nus.logbase.crindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.MyKVStore;
import org.apache.hadoop.hbase.regionserver.Store;
import org.apache.hadoop.hbase.regionserver.wal.LogEntryOffset;
import org.apache.hadoop.hbase.regionserver.wal.LogScannerByFile;
import org.apache.hadoop.hbase.util.Bytes;

public class LogIndexCRIndexSingleDimensionFullScan implements LogIndexInterface{
	
	//private static final int INF = 1<<29;
	
	private final Store c_kvstore;
	private final LogIndexConfigurationSet c_config;
	private final byte[] c_column;
	private final int c_tuplePerBlock;
	private final CRlog c_log;
	private final int holeSlots;
	
	private CRrecord m_activeRecord;
	private int m_nextBlockID;

	/**
	 * Create an instance of CR-Index in 1-dimension
	 * @param columns
	 */
	public LogIndexCRIndexSingleDimensionFullScan(byte[] column, Store kvstore, LogIndexConfigurationSet config){
		c_config = config;
		c_kvstore = kvstore;
		
		boolean CRlogOnDisk = config.getBoolean("crlog_on_disk", false);
		String CRlogPath = config.getString("crlog_path", "/tmp/crlog");
		
		holeSlots = config.getInt("crlog_hole_slots", 5);
		
		c_log = new CRlog(CRlogOnDisk, CRlogPath, holeSlots);
		
		c_column = column;
		c_tuplePerBlock = c_config.getInt("tuple_per_block", 128);

		m_nextBlockID = 0;
		
		//wangsheng need to re-scan the column and build index on the existing records
		
		
		
	}
	
	/**
	 * Add a new tuple into the index
	 * the list of key-value pairs are regarded as different cells of a single tuple
	 */
	public void add(List<KeyValue> kvs, List<LogEntryOffset> offsets) {
		
		// Check if contains data
		if (kvs == null || kvs.size() == 0) return;
		
		// Check which cell is indexed
		int indexedColumn = -1;
		for (int i = 0; i < kvs.size(); ++i){
			if (isIndexedColumn(kvs.get(i).getQualifier()) == true){
				indexedColumn = i;
				break;
			}
		}
		
		// Check if have indexed data
		if (indexedColumn == -1) return;
		KeyValue kv = kvs.get(indexedColumn);
		double v = Bytes.toDouble(kv.getValue());
		
		// 2013-12-13 wangsheng
		System.out.println("k = "+Bytes.toString(kv.getRow())+" c = "+Bytes.toString(kv.getQualifier())+" v = "+v);
		
		// The first tuple of a new block
		if (m_activeRecord == null){
			m_activeRecord = new CRrecord(m_nextBlockID++, v, v, offsets.get(0), 1, holeSlots);
		}
		else { // Update CR-record
			m_activeRecord.insert(v);
		}
		
		// The current block is full
		// Add it into the CR-log
		if (m_activeRecord.getBlockLength() == c_tuplePerBlock){
			flush(true);
		}
	}

	public List<KeyValue> rangeQueryByIndex(List<byte[]> columns, List<Double> minV, List<Double> maxV) throws IOException {

		List<KeyValue> ret = new ArrayList<KeyValue>();
		
//		int cnt = 0;
		
		/**
		 * Get the indexed column
		 */
		int indexedColumn = -1;
		for (int i = 0; i < columns.size(); ++i){
			if (isIndexedColumn(columns.get(i))){
				indexedColumn = i;
				break;
			}
		}
		if (indexedColumn == -1) return ret;
		
		double low = minV.get(indexedColumn);
		double up = maxV.get(indexedColumn);
		
		/**
		 *  Find all block id containing the range
		 */
		long indexLookupTime = -System.nanoTime();
		
		List<CRrecord> blks = new ArrayList<CRrecord>();
		
		seekCandicateBlocks(low, up, blks);
		
		//Sort the array to optimize the disk read performance
		//already ordered I think, so do not need sort
		//Collections.sort(blks);
		
		LogIndexDebug.printInfo("# of candidate blocks: "+blks.size());
		
		indexLookupTime += System.nanoTime();
		LogIndexDebug.printInfo("index lookup spends " + indexLookupTime*1e-9 + " seconds");
		/**
		 * using block information to access the data
		 */
		long dataAccessTime = -System.nanoTime();
		
		LogScannerByFile scanner = (LogScannerByFile)c_kvstore.getLogScannerByFile();
		
		int validBlockCnt = 0;
		
		int cellPerTuple = c_config.getInt("cell_per_tuple", 4);
		KeyValue[] tuple = new KeyValue[cellPerTuple];
		
		for (CRrecord r: blks){
			LogEntryOffset offset = r.getBlockLocation();
			
			scanner.setPosition(offset);
			long remainTuple = r.getBlockLength();
			boolean containResult = false;
			
			double hole_min = -1e99;
			double hole_max = 1e99;
			
			while (remainTuple > 0){
				
				--remainTuple;
				for (int i = 0; i < cellPerTuple; ++i){
					tuple[i] = scanner.next();
					if (tuple[i] == null) break;
				}
				
				// If reach end of file
				if (tuple[0] == null) break;
				
				KeyValue kv = tuple[indexedColumn];
				double v = Bytes.toDouble(kv.getValue());
				
				// If tuple is in this range
				if (low <= v && v <= up){
					containResult = true;
					
					//2013-12-13 wangsheng
					for (KeyValue x : tuple){
						ret.add(x);
					}
					//ret.add(kv);
				}
				else if (v < low){
					if (hole_min < v) hole_min = v;
				}
				else if (v > up){
					if (hole_max > v) hole_max = v;
				}
			}
			
			if (containResult == true) validBlockCnt++;
			else{
				r.insertHole(hole_min, hole_max);
				c_log.updateRecord((int)(r.getCid()), r);
			}
		}
		
		scanner.close();
		
		LogIndexDebug.printInfo("# of valid blocks: " + validBlockCnt);
		
		dataAccessTime += System.nanoTime();
		LogIndexDebug.printInfo("data access spends " + dataAccessTime*1e-9 + " seconds");
		
		return ret;
	}
	
	public void indexLookupOnly(List<byte[]> columns, List<Double> minV, List<Double> maxV) throws IOException {

		/**
		 * Get the indexed column
		 */
		int indexedColumn = -1;
		for (int i = 0; i < columns.size(); ++i){
			if (isIndexedColumn(columns.get(i))){
				indexedColumn = i;
				break;
			}
		}
		if (indexedColumn == -1) return;
		
		double low = minV.get(indexedColumn);
		double up = maxV.get(indexedColumn);
		
		/**
		 *  Find all block id containing the range
		 */
		long indexLookupTime = -System.nanoTime();
		
		List<CRrecord> blks = new ArrayList<CRrecord>();
		
		seekCandicateBlocks(low, up, blks);
		
		LogIndexDebug.printInfo("# of candidate blocks: "+blks.size());
		
		indexLookupTime += System.nanoTime();
		LogIndexDebug.printInfo("index lookup spends " + indexLookupTime*1e-9 + " seconds");
		
	}
	
	private void seekCandicateBlocks(double minV, double maxV, List<CRrecord> result){
		
		int size = (int)(c_log.getNumberOfRecords());
		
		for (int i = 0; i < size; ++i){
			
			CRrecord r = c_log.getRecord(i);
			
			//no value in it
			if (minV > r.getMaxValue() || maxV < r.getMinValue()) continue;
			
			boolean valid = true;
			for (int j = 0; j < r.getValidHole(); ++j){
				if (r.getHoleMinV(j) < minV && maxV < r.getHoleMaxV(j)){
					valid = false;
					break;
				}
			}
			
			if (valid) result.add(r);
		}
	}

	public List<KeyValue> rangeQueryByScan(List<byte[]> columns, List<Double> minV, List<Double> maxV) throws IOException {
		
		List<KeyValue> ret = new ArrayList<KeyValue>();

		int indexedColumn = -1;
		for (int i = 0; i < columns.size(); ++i){
			if (isIndexedColumn(columns.get(i))){
				indexedColumn = i;
				break;
			}
		}
		if (indexedColumn == -1) return ret;
		
		double low = minV.get(indexedColumn);
		double up = maxV.get(indexedColumn);
		
		/**
		 * scan the data
		 */
		LogScannerByFile scanner = (LogScannerByFile)c_kvstore.getLogScannerByFile();
		int cellPerTuple = c_config.getInt("cell_per_tuple", 4);
		KeyValue[] tuple = new KeyValue[cellPerTuple];
		
		while (true){
			for (int i = 0; i < cellPerTuple; ++i){
				tuple[i] = scanner.next();
				if (tuple[i] == null) break;
			}
			
			// If reach end of file
			if (tuple[0] == null) break;
			
			KeyValue kv = tuple[indexedColumn];
			double v = Bytes.toDouble(kv.getValue());
			
			// It tuple is in this range
			if (low <= v && v <= up){
				ret.add(kv);
			}
		}
		
		scanner.close();
		
		return ret;
	}
	
	public void flush(boolean sync) {

		// check if the flush is valid
		if (m_activeRecord == null){
			c_log.flush();
			return;
		}
		
		c_log.add(m_activeRecord);
		c_log.flush();
		
		m_activeRecord = null;
	}

	public void close() {
		
		flush(true);
		c_log.close();
	}
	
	private boolean isIndexedColumn(byte[] col){
		return Bytes.equals(col, c_column);
	}
	
}
