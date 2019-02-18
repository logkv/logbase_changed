package sg.edu.nus.logbase.crindex;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class IntervalIndexIntervalTree {
	
	private static final long INF = 1L << 59;
	private TreeNode root;
	private final int node_capacity;
	
	public IntervalIndexIntervalTree(int cap){
		root = new TreeNode();
		node_capacity = cap;
	}
	
	public void add(double minV, double maxV, long id){
		root.insert(minV, maxV, id);
	}
	
	public long[] get(double v){
		
		List<Long> s = new ArrayList<Long>();
		root.get(v, s);
		
		long[] ret = new long[s.size()];
		int pos = 0;
		
		for (Long e : s){
			ret[pos++] = e;
		}
		return ret;
	}
	
	private class TreeNode{
		private double center;		// the center point of the node
		private SortedSet<PointEntry> interval_list;	// list of intervals
//		private int num_intervals;	// number of intervals in the list
		private TreeNode left_child, right_child;	// the child nodes of current node
		
		public TreeNode(){
			interval_list = new TreeSet<PointEntry>();
//			num_intervals = 0;
			left_child = right_child = null;
		}
		
		// insert a interval in the format of [minV, maxV] & id
		public void insert(double minV, double maxV, long id){

			PointEntry l = new PointEntry(minV, id, true);
			PointEntry r = new PointEntry(maxV, id, false);
			l.setPair(r);
			r.setPair(l);
			
			insert(l, r);
		}
		
		// insert a interval in the format of two endpoints
		private void insert(PointEntry l, PointEntry r){
			
			// Check if is leaf node
			if (left_child == null){
				
				insertInterval(l, r);
				if (interval_list.size() >= node_capacity*2){
					split();
				}
			}
			else{
				if (r.getKey() < center) left_child.insert(l, r);
				else if (center < l.getKey()) right_child.insert(l, r);
				else insertInterval(l, r);
			}
		}
		
		private void split(){
			
			List<PointEntry> list = new ArrayList<PointEntry>();
			int size = interval_list.size();
			for (PointEntry e : interval_list){
				list.add(e);
			}
			
//			LogIndexDebug.printDetail("before split:");
//			for (PointEntry e: list){
//				if (e.isLeftPoint() == false) continue;
//				LogIndexDebug.printDetail("b "+e.getID());
//			}
			
			center = list.get(size/2).getKey();
			left_child = new TreeNode();
			right_child = new TreeNode();
			
			interval_list.clear();
//			num_intervals = 0;
			
			for (PointEntry l : list){
				if (l.isLeftPoint() == false) continue;
				PointEntry r = l.getPair();
				
				if (r.getKey() < center) left_child.insertInterval(l, r);
				else if (center < l.getKey()) right_child.insertInterval(l, r);
				else insertInterval(l, r);
			}
			
//			LogIndexDebug.printDetail("after split:");
//			LogIndexDebug.printDetail("center = "+center);
//			for (PointEntry e: interval_list){
//				if (e.isLeftPoint() == false) continue;
//				LogIndexDebug.printDetail("a "+e.getID());
//			}
		}
		
		private void insertInterval(PointEntry l, PointEntry r){
			interval_list.add(l);
			interval_list.add(r);
//			++num_intervals;
		}
		
		
		// stabbing queries return the set of CR-record ids.
		public void get(double v, List<Long> ret){
			
			// access the interval_list in a leaf node
			if (left_child == null){
				for (PointEntry l : interval_list){
					if (l.isLeftPoint() == false) continue;
					if (l.getKey() > v) break;
					
					PointEntry r = l.getPair();
					if (r.getKey() > v) ret.add(l.getID());
				}
				return;
			}
			
			// access internal node
			if (v  <= center){
				SortedSet<PointEntry> s = interval_list.headSet(new PointEntry(v, INF, true));
				for (PointEntry e : s){
					ret.add(e.getID());
//					LogIndexDebug.printDetail("++"+e.getID());
				}
			}
			else{
				SortedSet<PointEntry> s = interval_list.tailSet(new PointEntry(v, 0, true));
				for (PointEntry e : s){
					ret.add(e.getID());
//					LogIndexDebug.printDetail("++"+e.getID());
				}
			}
			
			if (v < center) left_child.get(v, ret);
			else if (center < v) right_child.get(v, ret);
			
		}
	}
	
	private class PointEntry implements Comparable<PointEntry>{
		private double key;				// value of the end point
		private long record_id;			// id of the represented CR-record
		private PointEntry pair_point;	// other half of the interval
		private boolean isLeftPoint;	// true : left    right : right
		
		public PointEntry(double k, long id, boolean isLeft){
			key = k;
			record_id = id;
			isLeftPoint = isLeft;
		}
		
		@Override
		public int compareTo(PointEntry o) {
			if (key != o.key) return key < o.key ? -1 : 1;
			if (record_id != o.record_id) return record_id < o.record_id ? -1 : 1;
			return 0;
		}
		
		public void setPair(PointEntry e) {pair_point = e;}
		
		public double getKey() {return key;}
		public long getID() {return record_id;}
		public PointEntry getPair() {return pair_point;}
		public boolean isLeftPoint() {return isLeftPoint;}
	}
	
//	public static void main (String[] argv){
//	
//		IntervalIndexIntervalTree test = new IntervalIndexIntervalTree(10);
////		test.add(0.5, 0.7, 10);
////		test.add(0.3, 0.6, 12);
////		test.add(1.2, 1.3, 3);
//		
//		for (int i = 0; i < 10000; ++i){
//			test.add(10.0, 20.0, i);
//		}
//	
//		long[] ret =  test.get(11.55);
//		for (long e : ret){
//			System.out.println(e);
//		}
//	}
}
