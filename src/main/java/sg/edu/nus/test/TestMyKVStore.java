package sg.edu.nus.test;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.MyKVStore;
import org.apache.hadoop.hbase.regionserver.wal.LogScanner;

import sg.edu.nus.harmonious.utils.HUtils;

public class TestMyKVStore {

//	int numRecords = 500 * 1024;
//	int numRandomAccess = 10000;

	int numRecords = 4000000;
	int numRandomAccess = 4000;

	
//	int numRecords = 1024;
//	int numRandomAccess = 10;

	int numQualifier = 3;
	int numFamily = 1;

	byte[] row = null;
	byte[] value = null;
	byte[] qualifier = null;

	int rowLength = 11;
	int valLength = 1024;

	int qualifierLength = 2;

	Random random = new Random();

	MyKVStore kvstore = null;

	public static String testConfFilePath = "/data/hadoop/single/hadoop-0.20.205.0/conf/core-site.xml";

	public TestMyKVStore() throws IOException {
		this.row = new byte[this.rowLength];
		this.value = new byte[this.valLength];
		this.qualifier = new byte[this.qualifierLength];

		System.out.println("row length: " + rowLength + "\nvalue length: "
				+ valLength + "\nqualifer length: " + qualifierLength);

	}

	public void testwrite() throws IOException {

		kvstore = new MyKVStore(new Configuration(), "table", "cf", true,
				63.95f);

		for (int i = 0; i < numRecords; i++) {

			generateRow(i, row, 0, rowLength);
			generateColumnQualifier(i, qualifier, 0, qualifierLength);
			generateValue(i, value, 0, valLength);

			kvstore.put(row, qualifier, value);
		}

		kvstore.close();
	}

	public void testwriteandread() throws Exception {

		Configuration conf = new Configuration();

		/*
		 * need to add hadoop config (use full path) for working with hdfs
		 */
		conf.addResource(new Path(
				testConfFilePath));

		kvstore = new MyKVStore(conf, "table", "cf", true, 3.95f);
		long start = System.currentTimeMillis();

		long writeTime;
		
		for (int i = 0; i < numRecords; i++) {

			generateRow(i, row, 0, rowLength);
			generateColumnQualifier(i, qualifier, 0, qualifierLength);
			generateValue(i, value, 0, valLength);

			kvstore.put(row, qualifier, value);
			
			if (i%10000==0){
				writeTime = System.currentTimeMillis() - start;
				System.out.println("num write: " + i + " time: " + writeTime
						+ " avg: " + (writeTime * 1.0 / i));

			}
		}

		// kvstore.printLogFiles();

		kvstore.persistLatestData();

		writeTime = System.currentTimeMillis() - start;

		System.out.println("num write: " + numRecords + " time: " + writeTime
				+ " avg: " + (writeTime * 1.0 / numRecords));

		
		System.out.println("searching...");

		long readTime;
		
		start = System.currentTimeMillis();

		// kvstore.printLogFiles();

		// this.numRandomAccess = 1;
		for (int i = 0; i < this.numRandomAccess; i++) {

			final int randomIdx = (random.nextInt() % this.numRecords + this.numRecords)
					% this.numRecords;
			generateRow(randomIdx, row, 0, rowLength);
			generateColumnQualifier(randomIdx, qualifier, 0, qualifierLength);

			KeyValue kv = kvstore.get(row, qualifier);
			if (kv != null) {
				KeyValue.Key foundKey = kv.getKeyForLog();
				//System.out.println("search "+ randomIdx + " Found: "+ new String(kv.getRow()) + " | " +  new String(kv.getValue()));
				//System.out.println("search "+ randomIdx + " Found: "+ getKeyInteger(foundKey.getBuffer(), foundKey.getRowOffset(), foundKey.getRowLength()));
			} else {
				// System.out.println("search "+ randomIdx + " NULL result");
			}
			
			if (i%1000==0){
				readTime = System.currentTimeMillis() - start;
				System.out.println("num read: " + i + " time: " + readTime
						+ " avg: " + (readTime * 1.0 / i));

			}

		}

		readTime = System.currentTimeMillis() - start;

		
		System.out.println("num write: " + numRecords + " time: " + writeTime
				+ " avg: " + (writeTime * 1.0 / numRecords));
		System.out.println("num read: " + numRandomAccess + " time: "
				+ readTime + " avg: " + (readTime * 1.0 / numRandomAccess));

		// kvstore.printLogFiles();

		kvstore.close();
	}

	public void testread() throws IOException {

		kvstore = new MyKVStore(new Configuration(), "table", "cf", false,
				63.95f);

		// this.numRandomAccess = 1;
		for (int i = 0; i < this.numRandomAccess; i++) {

			final int randomIdx = (random.nextInt() % this.numRecords + this.numRecords)
					% this.numRecords;
			generateRow(randomIdx, row, 0, rowLength);
			generateColumnQualifier(randomIdx, qualifier, 0, qualifierLength);

			KeyValue kv = kvstore.get(row, qualifier);
			KeyValue.Key foundKey = kv.getKeyForLog();

			System.out.println("search "
					+ randomIdx
					+ " Found: "
					+ getKeyInteger(foundKey.getBuffer(),
							foundKey.getRowOffset(), foundKey.getRowLength()));
		}

		kvstore.printLogFiles();

		kvstore.close();

	}

	int getKeyInteger(byte[] key, int offset, int length) {
		int ret = 0;
		for (int i = offset; i < offset + length; i++) {
			ret *= 10;
			ret += (key[i] - '0');
		}
		return ret;
	}

	public void testscan() throws IOException {
		kvstore = new MyKVStore(new Configuration(), "table", "cf", false,
				63.95f);
		LogScanner scanner = kvstore.getLogScannerByFile();
		KeyValue ret;
		int cnt = 0;
		while ((ret = scanner.next()) != null) {
			int id = getKeyInteger(ret.getBuffer(), ret.getRowOffset(),
					ret.getRowLength());
			System.out.println("found key: " + id);
			cnt++;
		}
		scanner.close();
	}

	// public void generatedKey(int rowID) {
	// HUtils.format(rowID, row);
	// }
	//
	// public void generateValue(int rowID) {
	// random.setSeed(rowID);
	// random.nextBytes(value);
	// }

	public void generateRow(int rowID, byte[] row, int offset, int length) {
		HUtils.format(rowID, row, offset, length);
	}

	public void generateColumnFamily(int rowID, byte[] family, int offset,
			int length) {
		generateColumnFamily(rowID, family, offset, this.numFamily);
	}

	public void generateColumnFamily(int rowID, byte[] family, int offset,
			int length, int nFamily) {
		random.setSeed(rowID);
		HUtils.format(random.nextInt(nFamily), family, offset, length);
	}

	public void generateColumnQualifier(int rowID, byte[] columnQualifier,
			int offset, int length) {
		generateColumnQualifier(rowID, columnQualifier, offset, length,
				this.numQualifier);
	}

	public void generateColumnQualifier(int rowID, byte[] columnQualifier,
			int offset, int length, int nQualifier) {
		random.setSeed(rowID);
		HUtils.format(random.nextInt(nQualifier), columnQualifier, offset,
				length);
	}

	public void generateValue(int rowID, byte[] value, int offset, int length) {
		random.setSeed(rowID);
		for (int i = offset + length - 1; i >= offset; i--) {
			value[i] = (byte) (random.nextInt(95) + ' ');
		}
	}

	public void generateKey(int rowID, KeyValue.Key key) {
		generateKey(rowID, key, this.numFamily, this.numQualifier);
	}

	public void generateKey(int rowID, KeyValue.Key key, int nFamily,
			int nColumn) {
		generateRow(rowID, key.getBuffer(), key.getRowOffset(),
				key.getRowLength());
		generateColumnFamily(rowID, key.getBuffer(), key.getFamilyOffset(),
				key.getFamilyLength(), nFamily);
		generateColumnQualifier(rowID, key.getBuffer(),
				key.getQualifierOffset(), key.getQualifierLength(), nColumn);
	}

	public void generateKey(int rowID, KeyValue kv, int nFamily, int nColumn) {
		generateRow(rowID, kv.getBuffer(), kv.getRowOffset(), kv.getRowLength());
		generateColumnFamily(rowID, kv.getBuffer(), kv.getFamilyOffset(),
				kv.getFamilyLength(), nFamily);
		generateColumnQualifier(rowID, kv.getBuffer(), kv.getQualifierOffset(),
				kv.getQualifierLength(), nColumn);
	}

	public void generateKeyValue(int rowID, KeyValue kv) {
		generateKeyValue(rowID, kv, this.numFamily, this.numQualifier);
	}

	public void generateKeyValue(int rowID, KeyValue kv, int nFamily,
			int nColumn) {
		generateKey(rowID, kv, nFamily, nColumn);
		generateValue(rowID, kv.getBuffer(), kv.getValueOffset(),
				kv.getValueLength());
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		TestMyKVStore test = new TestMyKVStore();

		if (args.length >= 1) {
			testConfFilePath = args[0];
		}

		String operation = "wr";
		if (args.length >= 2) {
			operation = args[1];
		}

		if (args.length >= 3) {
			test.numRecords = Integer.parseInt(args[2]);
		}

		if (args.length >= 4) {
			test.numRandomAccess = Integer.parseInt(args[3]);
		}

		System.out.println("Test mykvstore123, with op:" + operation + "; records: " + test.numRecords +
				"; numRandomAccess: " + test.numRandomAccess + "; path: " + testConfFilePath);

		if ("wr".equals(operation)) {
			test.testwriteandread();
		} else if ("w".equals(operation)) {
			test.testwrite();
		} else if ("r".equals(operation)) {
			test.testread();
		} else if ("s".equals(operation)) {
			test.testscan();
		}


		// test.testwrite();
		// test.testread();
		// test.testscan();

	}

}
