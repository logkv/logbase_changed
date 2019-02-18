package sg.edu.nus.logbase.crindex;

import java.util.SortedSet;
import java.util.TreeSet;

public class IntervalIndexBtree {
	
	private static final long INF = 1L << 59;
	private SortedSet<BtreeEntry> tree;
	
	public IntervalIndexBtree(){
		tree = new TreeSet<BtreeEntry>();
	}
	
	public void add(double key, long id){
		tree.add(new BtreeEntry(key, id));
	}
	
	public long[] get(double minV, double maxV){
		
		SortedSet<BtreeEntry> s = tree.subSet(new BtreeEntry(minV, 0), new BtreeEntry(maxV, INF));
		
		long[] ret = new long[s.size()];
		int pos = 0;
		
		for (BtreeEntry e : s){
			ret[pos++] = e.getID();
		}
		
		return ret;
	}
	
	private class BtreeEntry implements Comparable<BtreeEntry>{
		private double key;		// value of the end point
		private long record_id;	// id of the represented CR-record
		//private boolean isLeftPoint;	// true : left    right : right
		
		public BtreeEntry(double k, long id){
			key = k;
			record_id = id;
		}
		
		@Override
		public int compareTo(BtreeEntry o) {
			if (key != o.key) return key < o.key ? -1 : 1;
			if (record_id != o.record_id) return record_id < o.record_id ? -1 : 1;
			return 0;
		}
		
		//public double getKey() {return key;}
		public long getID() {return record_id;}
	}
	
//	public static void main (String[] argv){
//		
//		IntervalIndexBtree test = new IntervalIndexBtree();
//		test.add(0.5, 10);
//		test.add(0.3, 12);
//		test.add(1.2, 3);
//		
//		long[] ret = test.get(0.1, 1.0);
//		for (long e : ret){
//			System.out.println(e);
//		}
//	}
}
