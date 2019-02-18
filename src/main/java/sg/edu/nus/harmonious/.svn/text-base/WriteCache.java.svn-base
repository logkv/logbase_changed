package sg.edu.nus.harmonious;

import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.HeapSize;
import org.apache.hadoop.hbase.util.ClassSize;

public class WriteCache implements HeapSize {

	private static final Log LOG = LogFactory.getLog(WriteCache.class);

	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	volatile ConcurrentNavigableMap<KeyValue.Key, KeyValue> cache;

	// Used to track own heapSize
	final AtomicLong heapsize;
	
	final AtomicLong datasize;

	public final static long FIXED_OVERHEAD = ClassSize.align(ClassSize.OBJECT
			+ (9 * ClassSize.REFERENCE));

	public final static long DEEP_OVERHEAD = ClassSize.align(FIXED_OVERHEAD
			+ ClassSize.REENTRANT_LOCK + (2 * ClassSize.ATOMIC_LONG)
			+ ClassSize.COPYONWRITE_ARRAYSET + ClassSize.COPYONWRITE_ARRAYLIST
			+ (2 * ClassSize.CONCURRENT_SKIPLISTMAP));

	/**
	 * Constructor.
	 * 
	 * @param c
	 *            Comparator
	 */
	public WriteCache() throws IOException {

		this.cache = new ConcurrentSkipListMap<KeyValue.Key, KeyValue>();

		this.heapsize = new AtomicLong(DEEP_OVERHEAD);
		this.datasize = new AtomicLong(0);

	}

	public int getEntriesNum() {
		return cache.size();
	}

	/**
	 * Write an update
	 * 
	 * @param key
	 * @return approximate size of the passed key and value.
	 */
	public long add(final KeyValue.Key key, KeyValue keyvalue) {
		long s = -1;
		this.lock.writeLock().lock();
		try {
			s = heapSizeChange(key, this.cache.put(key, keyvalue) == null);

			this.heapsize.addAndGet(s);
			this.datasize.addAndGet(key.getKeyLength() + keyvalue.getLength());
			
		} finally {
			this.lock.writeLock().unlock();
		}
		return s;
	}

	// TODO: make sure that if it needs the readLock() here
	public KeyValue getKeyValue(KeyValue.Key key) {
		this.lock.readLock().lock();
		try {
			return cache.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

//	/**
//	 * Write a delete
//	 * 
//	 * @param delete
//	 * @return approximate size of the passed key and value.
//	 */
//	long delete(final KeyValue delete, KeyValue keyvalue) {
//		long s = 0;
//		this.lock.writeLock().lock();
//		try {
//			KeyValue previousOffset = this.cache.put(delete.getKeyForLog(),
//					keyvalue);
//			s += heapSizeChange(delete.getKeyForLog(), previousOffset == null);
//
//		} finally {
//			this.lock.writeLock().unlock();
//		}
//		this.heapsize.addAndGet(s);
//		return s;
//	}

	/*
	 * Calculate how the MemStore size has changed. Includes overhead of the
	 * backing Map.
	 * 
	 * @param kv
	 * 
	 * @param notpresent True if the kv was NOT present in the set.
	 * 
	 * @return Size
	 */
	long heapSizeChange(final KeyValue.Key key, final boolean notpresent) {
		return notpresent ? ClassSize
				.align(ClassSize.CONCURRENT_SKIPLISTMAP_ENTRY + key.heapSize())
				: 0;
	}

	/**
	 * Get the entire heap usage for this MemStore not including keys in the
	 * snapshot.
	 */
	public long heapSize() {
		return heapsize.get();
	}

	public long dataSize() {
		return datasize.get();
	}
	
	public long getMemorySizeInByte(){
		return heapsize.get() + datasize.get();
	}

	
//	/**
//	 * Get the heap usage of KVs in this MemStore.
//	 */
//	public long keySize() {
//		return heapSize() - DEEP_OVERHEAD;
//	}



}
