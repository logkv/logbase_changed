package sg.edu.nus.logbase.crindex;

import org.apache.hadoop.hbase.regionserver.wal.LogEntryOffset;

public class CRrecord {
	
	private long cid;	// sequence ID of CR-record
	private double minV, maxV;	// boundary pair of CR-record
	private LogEntryOffset block_location;	// location of the represented block
	private long block_length;	// number of tuples inside the block
	
	private int valid_hole;
	private double hole_min[], hole_max[];	// the holes inside the record
	
	public CRrecord (long id, double minValue, double maxValue, LogEntryOffset entry, long length, int hole){
		cid = id;
		minV = minValue;
		maxV = maxValue;
		block_location = entry;
		block_length = length;
		
		hole_min = new double[hole];
		hole_max = new double[hole];
		valid_hole = 0;
	}
	
	public void insert(double v){
		++block_length;
		if (minV > v) minV = v;
		if (maxV < v) maxV = v;
	}
	
	public void insertHole(double minV, double maxV){
		if (valid_hole == hole_min.length){
			//LogIndexDebug.printDetail("no free hole slots for the insertion");
			return;
		}
		
		hole_min[valid_hole] = minV;
		hole_max[valid_hole] = maxV;
		
		valid_hole++;
	}
	
	public long getCid(){ return cid;}
	public double getMinValue() {return minV;}
	public double getMaxValue() {return maxV;}
	public LogEntryOffset getBlockLocation() {return block_location;}
	public long getBlockLength() {return block_length;}
	public int getValidHole() {return valid_hole;}
	public double getHoleMinV(int idx) {return hole_min[idx];}
	public double getHoleMaxV(int idx) {return hole_max[idx];}
}
