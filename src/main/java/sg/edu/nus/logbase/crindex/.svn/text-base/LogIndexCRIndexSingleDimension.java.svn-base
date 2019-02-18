package sg.edu.nus.logbase.crindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.MyKVStore;
import org.apache.hadoop.hbase.regionserver.Store;
import org.apache.hadoop.hbase.regionserver.wal.LogEntryOffset;
import org.apache.hadoop.hbase.regionserver.wal.LogScannerByFile;
import org.apache.hadoop.hbase.util.Bytes;

public class LogIndexCRIndexSingleDimension implements LogIndexInterface{
	
	private static final long INF = 1L << 50;
	
	private final Store c_kvstore;
	private final LogIndexConfigurationSet c_config;
	private final byte[] c_column;
	private final int c_tuplePerBlock;
	private final CRlog c_log;
	private final int holeSlots;
	
	// interval indexes
	private final IntervalIndexBtree btree;
	private final IntervalIndexIntervalTree itree;
	
	private CRrecord m_activeRecord;
	private int m_nextBlockID;

	
	
	/**
	 * Create an instance of CR-Index in 1-dimension
	 * @param columns
	 */
	public LogIndexCRIndexSingleDimension(byte[] column, Store kvstore, LogIndexConfigurationSet config){
		c_config = config;
		c_kvstore = kvstore;
		
		boolean CRlogOnDisk = config.getBoolean("crlog_on_disk", false);
		String CRlogPath = config.getString("crlog_path", "/tmp/crlog");
		
		holeSlots = config.getInt("crlog_hole_slots", 5);
		
		c_log = new CRlog(CRlogOnDisk, CRlogPath, holeSlots);
		
		c_column = column;
		c_tuplePerBlock = c_config.getInt("tuple_per_block", 128);

		m_nextBlockID = 0;
		
		btree = new IntervalIndexBtree();
		itree = new IntervalIndexIntervalTree(c_config.getInt("itree_node_capacity", 500));
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
		
//		for (CRrecord r : blks){
//			LogIndexDebug.printDetail("block " + r.getCid()+" ("+r.getMinValue()+","+r.getMaxValue()+")");
//		}

		//Sort the array to optimize the disk read performance
		//already ordered I think, so do not need sort
		//Collections.sort(blks);
		
		LogIndexDebug.printInfo("# of all blocks: "+c_log.getNumberOfRecords());
		LogIndexDebug.printInfo("# of candidate blocks: "+blks.size());
		
		testContinuity(blks);
		
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
//					LogIndexDebug.printDetail("v in index = "+v+" : "+new String(kv.getRow()));
					ret.add(kv);
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
	
	private void testContinuity(List<CRrecord> blks){
		long last = -2;
		int seq = 0;
		for (CRrecord r : blks){
			long id = r.getCid();
			if (id != last+1) ++seq;
			last = id;
		}
		LogIndexDebug.printInfo("number of sequences is " + seq);
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
		
		long[][] group = new long[2][];
		group[0] = btree.get(minV, maxV);
		group[1] = itree.get(minV);
		
		Arrays.sort(group[0]);
		Arrays.sort(group[1]);
		
		int[] pos = {0,0};
		long[] v = new long[2];
		
		while (true){
			
			v[0] = (pos[0] == group[0].length ? INF : group[0][pos[0]]);
			v[1] = (pos[1] == group[1].length ? INF : group[1][pos[1]]);
			
			long id = v[0] < v[1] ? v[0] : v[1];
			
			if (id == INF) break;
			
			CRrecord r = c_log.getRecord((int)id);
			boolean valid = true;
			for (int i = 0; i < r.getValidHole(); ++i){
				if (r.getHoleMinV(i) < minV && maxV < r.getHoleMaxV(i)){
					valid = false;
					break;
				}
			}
			
			if (valid) result.add(r);
			
			for (int i = 0; i < 2; ++i){
				while (pos[i] < group[i].length && group[i][pos[i]] == id) ++pos[i];
//				LogIndexDebug.printDetail("pos["+i+"] = "+pos[i]+" , group["+i+"].length = "+group[i].length);
			}
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
//				LogIndexDebug.printDetail("v in scan = "+v+" : "+new String(kv.getRow()));
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
		
		
		// add CR-record into CR-log
		c_log.add(m_activeRecord);
		c_log.flush();
//		LogIndexDebug.printDetail("all block "+m_activeRecord.getCid()+" ("+m_activeRecord.getMinValue()+","+m_activeRecord.getMaxValue()+")");
		
		// update interval indexes
		btree.add(m_activeRecord.getMinValue(), m_activeRecord.getCid());
		btree.add(m_activeRecord.getMaxValue(), m_activeRecord.getCid());
		itree.add(m_activeRecord.getMinValue(), m_activeRecord.getMaxValue(), m_activeRecord.getCid());
		
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
