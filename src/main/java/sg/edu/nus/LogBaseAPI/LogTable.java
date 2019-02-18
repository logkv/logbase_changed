package sg.edu.nus.LogBaseAPI;

import java.io.IOException;
import java.io.Serializable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.commons.lang.SerializationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LogTable {
	private HTable table;
	public final boolean blocked;
	public final int blockSize;
	private int internalCount = 0;
	private long blockCount = 1;
	private Configuration conf;
	private Map<String, String> columnGroupNameMap;

	public LogTable(HTable t, boolean Blocked, int BlockSize) {
		table = t;
		blocked = Blocked;
		blockSize = BlockSize;
		columnGroupNameMap = new ConcurrentHashMap<String, String>();
	}

	/**
	 * Put a tuple (a value in one row, one column) into a table.
	 * @throws IOException e
	 */
	public void put(byte[] row, byte[] column, byte[] value) throws IOException {
		Put p = new Put(row);
		if (blocked){
			internalCount = internalCount+1;
			if (internalCount>=blockSize){
				internalCount=0;
				blockCount = System.currentTimeMillis();
			}
			p.add(getColumnGroupName(Bytes.toBytes("BlockID")), Bytes.toBytes("BlockID"), Bytes.toBytes("BLOCK_"+String.valueOf(blockCount)));		
			
		}			
		p.add(getColumnGroupName(column), column, value);
		table.put(p);
	}
	

	/**
	 * Put a row of record (with specified columns) into a table.
	 * @throws IOException e
	 */
	public void put(byte[] row, byte[][] columns, byte[][] values)
			throws IOException {
		Put p = new Put(row);
		if (blocked){
			internalCount = internalCount+1;
			if (internalCount>=blockSize){
				internalCount=0;
				blockCount = System.currentTimeMillis();
			}
			p.add(getColumnGroupName(Bytes.toBytes("BlockID")), Bytes.toBytes("BlockID"), Bytes.toBytes("BLOCK_"+String.valueOf(blockCount)));		
			
		}
		
		for (int i = 0; i < columns.length; ++i) {
			p.add(getColumnGroupName(columns[i]), columns[i], values[i]);
		}
		table.put(p);
	}
	
	
	/**
	 * Put a data block into a table
	 * @param blockID, i.e., block name, used to identify the block
	 * @param blockData, in which each ArrayList corresponds to a row
	 * @throws IOException
	 */
	public void put(byte[][] rows, byte[][] columns, Iterable<Iterable<Serializable>> blockData, String blockID) 
			throws IOException {	
		if (blockID==null||blockID.length()==0)
			blockID = String.valueOf(System.currentTimeMillis());
		
		byte[] BlockID= Bytes.toBytes("BLOCK_"+blockID);		
		int rowCount = 0;
		if (blockData!=null){
			for(Iterable<Serializable> rowData:blockData){
				byte[] Row = rows[rowCount];
				rowCount += 1;
				if (rows==null||rows.length==0)
					Row = Bytes.toBytes(blockID+"_"+String.valueOf(rowCount+1));
				Put p = new Put(Row);
				p.add(getColumnGroupName(Bytes.toBytes("BlockID")), Bytes.toBytes("BlockID"), Bytes.toBytes("BLOCK_"+blockID));
				int colCount = 0;
				for (Serializable tupleData:rowData){
					byte[] Col = columns[colCount];
					colCount += 1;
					if (columns==null||columns.length==0)						
						Col =Bytes.toBytes("c"+String.valueOf(colCount+1));
					p.add(getColumnGroupName(Col), Col, SerializationUtils.serialize(tupleData));
				}
				table.put(p);
			}
		}
		
	}
	

	/**
	 * Get a record from a table by its row as the key.
	 * @throws IOException e
	 */
	public Result get(byte[] row) throws IOException {
		Get g = new Get(row);
		return table.get(g);
	}
	
	
	/**
	 * Get a tuple from a table by its row and column as the key
	 * @throws IOException e
	 */
	public Result get(byte[] row, byte[] column) throws IOException {	
		Get g = new Get(row);
		g.addColumn(getColumnGroupName(column), column);
		return table.get(g);
	}

	
	/**
	 * Get a row (with specified columns) from a table.
	 * @throws IOException e
	 */
	public Result get(byte[] row, byte[][] columns) throws IOException {
		Get g = new Get(row);
		for (int i = 0; i < columns.length; ++i) {
			g.addColumn(getColumnGroupName(columns[i]), columns[i]);
		}
		return table.get(g);
	}

	
	/**
	 * Scan the table and return the results in a set of rows within a specified block.
	 * @throws IOException
	 */
	public Iterable<Result> blockScan(String blockID)  throws IOException {
		List<Result> ret = new ArrayList<Result>();
		ResultScanner rs = getScanner(blockID);
		int BlockCount = 0; 
		byte[] BlockCol = Bytes.toBytes("BlockID");
		while (true){
			Result res = rs.next();
			if (res==null){
				//System.out.println("break since res=null");
				break;
			}else{
				//System.out.println("here 1 =" + (new String(res.raw()[0].getValue())));
				for (int i=0;i<res.raw().length;++i){
					if (Bytes.compareTo(res.raw()[i].getQualifier(), BlockCol)==0){
						if (Bytes.compareTo(res.raw()[i].getValue(), Bytes.toBytes("BLOCK_"+blockID))==0){
							ret.add(res);
							break;
						}
					}
				}
			}
		}
		rs.close();
		return  ret;
	}
		
	
	/**
	 * Scan the table and return all records.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan() throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		ResultScanner rs = getScanner();
		while (true){
			res = rs.next();
			if (res==null){
			  break;
			  }else{
				  ret.add(res);
			  }
		}
		
		rs.close();		
		return ret;		
	}
		
	
	/**
	 * Scan the table and return all records on specified columns.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan(byte[][] columns) throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		ResultScanner rs = getScanner(columns);
		while (true){
			res = rs.next();
			if (res==null){
			  break;
			  }else{
				  ret.add(res);
			  }
		}
		
		rs.close();		
		return ret;		
	}
	
	
	/**
	 * Scan the table and return all records with row keys not less than a startRow.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan(byte[] startRow) throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		ResultScanner rs = getScanner(startRow);
		while (true){
			res = rs.next();
			if (res==null){
			  break;
			  }else{
				  ret.add(res);
			  }
		}
		
		rs.close();		
		return ret;		
	}

	/**
	 * Scan the table and return all records with row keys not less than a startRow.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan(byte[] startRow, int count) throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		ResultScanner rs = getScanner(startRow);
		int readCount = 0;
		while (true){
			res = rs.next();
			if (res==null){
				break;
			}else{
				ret.add(res);
			}
            readCount++;
			if (readCount == count) {
			    break;
            }
		}

		rs.close();
		return ret;
	}

	
	/**
	 * Scan the table and return all records on specified columns with row keys not less than a startRow.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan(byte[] startRow, byte[][] columns) throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		ResultScanner rs = getScanner(startRow, columns);
		while (true){
			res = rs.next();
			if (res==null){
			  break;
			  }else{
				  ret.add(res);
			  }
		}
		rs.close();		
		return ret;		
	}
	
	
	/**
	 * Scan the table and return all target records with row key not less than a startRow and less than an endRow.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan(byte[] startRow, byte[] endRow) throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		if (startRow.equals(endRow)){
			res = get(startRow); 
			ret.add(res);
			return ret;
		}
		ResultScanner rs = getScanner(startRow, endRow);
		while (true){
			res = rs.next();
			//System.out.println("getRow = "+new String(res.getRow()));
			//System.out.println("getValue = "+new String(res.raw()[0].getValue()));
			if (res==null){
			  break;
			  }else{
				 ret.add(res);
			  }
		}
		rs.close();
		return ret;		
	}

	
	/**
	 * Scan the table and return all target records on specified columns with row key not less than a startRow and less than an endRow.
	 * @throws IOException
	 */
	public Iterable<Result> tableScan(byte[] startRow, byte[] endRow, byte[][] columns) throws IOException{
		List<Result> ret = new ArrayList<Result>();
		Result res = null;
		if (startRow.equals(endRow)){
			res = get(startRow); 
			ret.add(res);
			return ret;
		}
		ResultScanner rs = getScanner(startRow, endRow, columns);
		while (true){
			res = rs.next();
			//System.out.println("getRow = "+new String(res.getRow()));
			//System.out.println("getValue = "+new String(res.raw()[0].getValue()));
			if (res==null){
			  break;
			  }else{
				 ret.add(res);
			  }
		}
		rs.close();
		return ret;		
	}

	
	/**
	 * list up the names of all columns in the table
	 */
	public String[] listColumns() throws IOException{
		String[] ret = null;
		String tableName = new String(table.getTableName());
		byte[] row = Bytes.toBytes(tableName);
		HTable T_Property = new HTable(conf, "Table_Property");
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("Columns"));
		Result R = T_Property.get(g);		
		String tmp = new String(R.raw()[0].getValue());
		ret = tmp.split(", ");		
		return ret;
	}
	
	
	/**
	 * return the data type of a specified column.
	 */
	public Class getDataType(byte[] column) throws IOException, ClassNotFoundException{
		//Class ret = null;
		String tableName = new String(table.getTableName());
		String columnName = new String(column);
		byte[] row = Bytes.toBytes(tableName+"_"+columnName);
		HTable T_Property = new HTable(conf, "Table_Property");
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("DataType"));
		Result R = T_Property.get(g);
		String tmp = new String(R.raw()[0].getValue());
		return Class.forName(tmp.split(" ")[1]);
	}
	
	
	/**
	 * list up the secondary index information on each column
	 * @return String array of which the i-th element is the ID of the index on the i-th column, or "null" if there is no index on the column
	 */
	public String[] listIndices() throws IOException{
		String[] ret = null; 
		String tableName = new String(table.getTableName());
		byte[] row = Bytes.toBytes(tableName);
		HTable T_Property = new HTable(conf, "Table_Property");
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("Columns"));
		Result R = T_Property.get(g);		
		String tmp = new String(R.raw()[0].getValue());
		String[] cols = tmp.split(", ");
		ret = new String[cols.length];
		for (int i=0;i<cols.length;++i){
			String columnName = cols[i];
			row = Bytes.toBytes(tableName+"_"+columnName);
			g = new Get(row);
			g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"));
			R = T_Property.get(g);
			ret[i]=new String(R.raw()[0].getValue());
		}
		return ret;
	}
	
	
	/**
	 * delete the secondary index on a specified column
	 */
	public void deleteIndex(byte[] column) throws IOException{
		String tableName = new String(table.getTableName());
		// row = Bytes.toBytes(tableName);
		HTable T_Property = new HTable(conf, "Table_Property");
		//for (int i=0;i<cols.length;++i){
		String columnName = new String(column);
		byte[] row = Bytes.toBytes(tableName+"_"+columnName);
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"));
		Result R = T_Property.get(g);
		if (R==null||R.raw().length==0){
			System.out.println("There has not been a column called "+columnName+" in table "+tableName+"!");
		}else{
			String ret=new String(R.raw()[0].getValue());
			if (ret.equals("null")){
				System.out.println("There has not been any index on column "+columnName+" of table "+tableName+"!");
			}else{
				//1. change the index info to null in Table_Property
				Put p = new Put(row);
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"), Bytes.toBytes("null"));
				T_Property.put(p);
				//2. remove the correspond index (in-mem index or local file) 
				//principle: reuse the passageway of getScanner with scan carrying the deleting info
				Scan scan = new Scan();
				scan.addColumn(getColumnGroupName(column), column);
				scan.toDelete2ndIndex(column);		
				ResultScanner rs =table.getScanner(scan);
				rs.close();
			}
		}
	}
	
	
	/**
	 * create a secondary index on a specified column
	 */
	public void creatIndex(byte[] column, int binSize) throws IOException{
		String tableName = new String(table.getTableName());
		HTable T_Property = new HTable(conf, "Table_Property");
		String columnName = new String(column);
		byte[] row = Bytes.toBytes(tableName+"_"+columnName);
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"));
		Result R = T_Property.get(g);
		if (R==null||R.raw().length==0){
			System.out.println("There has not been a column called "+columnName+" in table "+tableName+"!");
		}else{
			String ret=new String(R.raw()[0].getValue());
			//0. check if there is an index yet
			if (!ret.equals("null")){
				System.out.println("There has been an index on column "+columnName+" of table "+tableName+" already!");
			}else{
				g = new Get(row);
				g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("DataType"));
				R = T_Property.get(g);
				ret=new String(R.raw()[0].getValue());
				if (ret.equals(Double.class.toString())){
					//1. update index info in Table_Property
					Put p = new Put(row);
					p.add(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"), column);
					T_Property.put(p);
					//2. construct an index (in-mem index or local file) on the column
					//principle: reuse the passageway of getScanner with scan carrying the deleting info
					Scan scan = new Scan();
					scan.addColumn(getColumnGroupName(column), column);
					int ColumnNum = listColumns().length;
					if (this.blocked) ++ColumnNum;
					if (binSize<1) binSize =64;
					scan.toCreate2ndIndex(column,ColumnNum,binSize);		
					ResultScanner rs =table.getScanner(scan);
					rs.close();
				}else{
					System.out.println("The data type of column "+columnName+" of table "+tableName+" is "+ret.split(" ")[1]+", which cannot support index contruction!");
				}
				

			}			
		}		
	}
	
	
	/**
	 * Return records on retColumns if values on indexed column of the same row (i.e., idxColumn) fall in [minV, maxV]
	 */
	public Iterable<Result> valueScan(byte[] idxColumn, double minV, double maxV, byte[][] retColumns) throws IOException{
		List<Result> ret = new ArrayList<Result>();

		String tableName = new String(table.getTableName());
		HTable T_Property = new HTable(conf, "Table_Property");
		String columnName = new String(idxColumn);
		byte[] row = Bytes.toBytes(tableName+"_"+columnName);
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"));
		Result R = T_Property.get(g);
		//0.Check whether idxColumn is indexed yet.
		if (R==null||R.raw().length==0){
			System.out.println("There has not been a column called "+columnName+" in table "+tableName+"!");
		}else{
			String res=new String(R.raw()[0].getValue());
			//0. check if there is an index yet
			if (res.equals("null")){
				System.out.println("There has not been any index on column "+columnName+" of table "+tableName+"!");
			}else{
				 ResultScanner scanner = getValueScanner(idxColumn,minV,maxV,retColumns);
				  while (true)
				  {
					  Result RS = scanner.next();					  
					  if (ret == null) 
						  break;
					  else
						  ret.add(RS);
				  }
			}
		}
		return ret;
	}
	
	
	//2013-12-13 wangsheng
	public ResultScanner getValueScanner(byte[] idxColumn, double minV, double maxV, byte[][] columns) throws IOException {
	    Scan scan = new Scan();
	    if (columns == null){
	    	//scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
			HTableDescriptor t = table.getTableDescriptor();
			HColumnDescriptor[] cf = t.getColumnFamilies();	
			for (int i=0;i<cf.length;++i){
				scan.addFamily(cf[i].getName());
			}
	    }	    	
	    else{
	    	for (int i = 0; i < columns.length; ++i) {
				scan.addColumn(getColumnGroupName(columns[i]), columns[i]);
			}
	    }
	    scan.use2ndIndex(idxColumn, minV, maxV);
	    return table.getScanner(scan);// scanner track: i am here 0 (link to i am here 1.5)
	}
	

	/**
	 * return the name of the column group containing a specified column
	 */
	public byte[] getColumnGroupName(byte[] column) throws IOException {

		String columnStr = String.valueOf(column);
		if (columnGroupNameMap.containsKey(columnStr)) {
			return Bytes.toBytes(columnGroupNameMap.get(columnStr));
		} else {
			HTableDescriptor t = table.getTableDescriptor();
            HColumnDescriptor[] cf = t.getColumnFamilies();
            if (cf.length<=1){
				columnGroupNameMap.put(columnStr, "cf1");
                return Bytes.toBytes("cf1");
            }else{
                String tableName = new String(table.getTableName());
                String columnName = new String(column);
                byte[] row = Bytes.toBytes(tableName+"_"+columnName);
                HTable T_Property = new HTable(conf, "Table_Property");
                Get g = new Get(row);
                g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("ColFamily"));
                Result R = T_Property.get(g);
                byte[] result = R.raw()[0].getValue();
				columnGroupNameMap.put(columnStr, String.valueOf(result));
                return result;
            }
		}
	}
	
	
	/**
	 * Set the configuration for table accessing under current LogAdmin
	 * @param c Configuration information (just use the Configuration in the host LogAdmin)
	 */
	public void setConf(Configuration c){
		this.conf =c;
	}
	
	
	
//=====================huanghao: the following functions are private ones===============================================================

	private ResultScanner getScanner(byte[] startRow) throws IOException {
	    Scan scan = new Scan();
	    //scan.regionalScan = true;
	    scan.setStartRow(startRow);
		HTableDescriptor t = table.getTableDescriptor();
		HColumnDescriptor[] cf = t.getColumnFamilies();	
		for (int i=0;i<cf.length;++i){
			scan.addFamily(cf[i].getName());
		}	    
	    //scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
	    return table.getScanner(scan);
	}
		

	private ResultScanner getScanner(byte[] startRow, byte[][] columns) throws IOException {
	    Scan scan = new Scan();
	    //scan.regionalScan = true;
	    scan.setStartRow(startRow);
		for (int i = 0; i < columns.length; ++i) {
			scan.addColumn(getColumnGroupName(columns[i]), columns[i]);
		}	    
	    //scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
	    return table.getScanner(scan);
	}
	
	
	private ResultScanner getScanner(byte[] startRow, byte[] endRow) throws IOException {
	    Scan scan = new Scan();
	    //scan.regionalScan = true;
	    scan.setStartRow(startRow);
	    scan.setStopRow(endRow);
		HTableDescriptor t = table.getTableDescriptor();
		HColumnDescriptor[] cf = t.getColumnFamilies();	
		for (int i=0;i<cf.length;++i){
			scan.addFamily(cf[i].getName());
		}	    
	    //scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
	    return table.getScanner(scan);
	}
	
	
	private ResultScanner getScanner(byte[] startRow, byte[] endRow, byte[][] columns) throws IOException {
	    Scan scan = new Scan();
	    //scan.regionalScan = true;
	    scan.setStartRow(startRow);
	    scan.setStopRow(endRow);
		for (int i = 0; i < columns.length; ++i) {
			scan.addColumn(getColumnGroupName(columns[i]), columns[i]);
		}	    
	    //scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
	    return table.getScanner(scan);
	}
	
	
	private ResultScanner getScanner(String startBlockID) throws IOException {
	    Scan scan = new Scan();
	    scan.regionalScan = true;
	    scan.setStartBlockID(startBlockID);
		HTableDescriptor t = table.getTableDescriptor();
		HColumnDescriptor[] cf = t.getColumnFamilies();	
		for (int i=0;i<cf.length;++i){
			scan.addFamily(cf[i].getName());
		}	    
	    //scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
	    return table.getScanner(scan);
	}
	
	
	private ResultScanner getScanner() throws IOException {
	    Scan scan = new Scan();
		HTableDescriptor t = table.getTableDescriptor();
		HColumnDescriptor[] cf = t.getColumnFamilies();	
		for (int i=0;i<cf.length;++i){
			scan.addFamily(cf[i].getName());
		}
	    //scan.addFamily(getColumnGroupName(Bytes.toBytes("c1")));
	    return table.getScanner(scan);
	}
	
	
	private ResultScanner getScanner(byte[][] columns) throws IOException {
		Scan scan = new Scan();
		for (int i = 0; i < columns.length; ++i) {
			scan.addColumn(getColumnGroupName(columns[i]), columns[i]);
		}
		return table.getScanner(scan);
	}
	
	


}