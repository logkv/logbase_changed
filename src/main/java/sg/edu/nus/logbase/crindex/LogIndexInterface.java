package sg.edu.nus.logbase.crindex;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.wal.LogEntryOffset;

/**
 * Created by: WangSheng 2013-4-3
 * This is the interface for a index structure building on a store of LogBase
 * New added index should follow this interface to let the test class to evaluate the performance
 */

public interface LogIndexInterface {
	
	/**
	 * This interface will be called when a list of key-value pairs are stored in the logBase
	 * The cells added in a same list will be automatically regarded as in a same tuple
	 * @param kvs		:	set of key-value records
	 * @param offsets	:	set of positions of kvs
	 */
	public void add(List<KeyValue> kvs, List<LogEntryOffset> offsets);
	
	/**
	 * This interface will be called when a range query is issued
	 * @param columns	:	set of queried columns
	 * @param minV		:	lower bound of the queried range
	 * @param maxV		:	upper bound of the queried range
	 * @return			;	set of satisfied Key-Value pairs
	 * @throws IOException 
	 */
	
	public List<KeyValue> rangeQueryByIndex(List<byte[]> columns, List<Double> minV, List<Double> maxV) throws IOException;
	
	/**
	 * This interface will be called when a range query is issued
	 * @param columns	:	set of queried columns
	 * @param minV		:	lower bound of the queried range
	 * @param maxV		:	upper bound of the queried range
	 * @return			;	set of satisfied Key-Value pairs
	 * @throws IOException 
	 */
	
	public List<KeyValue> rangeQueryByScan(List<byte[]> columns, List<Double> minV, List<Double> maxV) throws IOException;
	
	/**
	 * Used for testing index performance
	 * @param columns	:	set of queried columns
	 * @param minV		:	lower bound of the queried range
	 * @param maxV		:	upper bound of the queried range
	 * @return			;	set of satisfied Key-Value pairs
	 * @throws IOException 
	 */
	public void indexLookupOnly(List<byte[]> columns, List<Double> minV, List<Double> maxV) throws IOException;
	
	/**
	 * This interface will be called when the index need to be synchronized
	 * @param sync		:	run the flush sequentially if true, in background if no
	 */
	public void flush(boolean sync);
	
	/**
	 * This interface will be called when the index need to be dump into disks
	 */
	public void close();
}
