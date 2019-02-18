package sg.edu.nus.LogBaseAPI;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class LogAdmin {
	
	private HBaseAdmin admin;
	private Configuration conf;
	
	
	public LogAdmin(Configuration conf) throws MasterNotRunningException, ZooKeeperConnectionException{
		this.conf = conf;
		this.admin = new HBaseAdmin(conf);
	}
	
	
	/**
	 * create a table with a specified table name, column names, and data type for each column.
	 */
	public void createTable(String tableName, String[] columnNames, Class[] dataTypes) 
			throws IOException{
		if (columnNames.length!=dataTypes.length)
		{
			System.out.println("The number of columns is not equal to that of data types.");
		}
		else if (!TableExists(tableName)){			
			if (!TableExists("Table_Property")){
				HTableDescriptor desc = new HTableDescriptor("Table_Property");
				desc.addFamily(new HColumnDescriptor(Bytes.toBytes("cf1")));
				admin.createTable(desc);
				  try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			HTableDescriptor desc = new HTableDescriptor(tableName);
			HTable T_Property = new HTable(conf, "Table_Property");
			Put p = null;
			String columns = "";
			for (int i=0;i<columnNames.length;++i){
				columns = columns+", "+columnNames[i];
				String tmp = tableName+"_"+columnNames[i];
				p = new Put(Bytes.toBytes(tmp));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("DataType"), Bytes.toBytes(dataTypes[i].toString()));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"), Bytes.toBytes("null"));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("ColFamily"), Bytes.toBytes("cf1"));//default cf is cf1
				T_Property.put(p);//update Table_Property for the table, T_col(cf1): (1)DataType (2)IdxInfo (3)ColFamily
			}
			p = new Put(Bytes.toBytes(tableName));
			p.add(Bytes.toBytes("cf1"), Bytes.toBytes("Columns"), Bytes.toBytes(columns.substring(2)));
			T_Property.put(p);//update the Table_Property by adding column info
			desc.addFamily(new HColumnDescriptor(Bytes.toBytes("cf1")));//default cf is cf1
			admin.createTable(desc);
		}else{
			System.out.println("The table named "+tableName+" is in LogBase already.");
		}
		
	}
	
	
	/**
     * create a table with a specified table name, column names, and data type for each column, and at the same time specify that the records of this table are organized in blocks with a given blockSize.
	 */
	public void createTable(String tableName, String[] columnNames, Class[] dataTypes,  int blockSize) 
			throws IOException{
		if (columnNames.length!=dataTypes.length)
		{
			System.out.println("The number of columns is not equal to that of data types.");
		}
		else if (!TableExists(tableName)){			
			if (!TableExists("Table_Property")){
				HTableDescriptor desc = new HTableDescriptor("Table_Property");
				desc.addFamily(new HColumnDescriptor(Bytes.toBytes("cf1")));
				admin.createTable(desc);
				  try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			HTableDescriptor desc = new HTableDescriptor(tableName);
			HTable T_Property = new HTable(conf, "Table_Property");
			Put p = null;
			String columns = "";
			columns = columns+", "+"BlockID";
			String tmp = tableName+"_"+"BlockID";
			p = new Put(Bytes.toBytes(tmp));
			p.add(Bytes.toBytes("cf1"), Bytes.toBytes("BlockSize"), Bytes.toBytes(String.valueOf(blockSize)));
			T_Property.put(p);
			for (int i=0;i<columnNames.length;++i){
				columns = columns+", "+columnNames[i];
				tmp = tableName+"_"+columnNames[i];
				p = new Put(Bytes.toBytes(tmp));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("DataType"), Bytes.toBytes(dataTypes[i].toString()));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"), Bytes.toBytes("null"));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("ColFamily"), Bytes.toBytes("cf1"));//default cf is cf1
				T_Property.put(p);//update Table_Property for the table, T_col(cf1): (1)DataType (2)IdxInfo (3)ColFamily
			}
			p = new Put(Bytes.toBytes(tableName));
			p.add(Bytes.toBytes("cf1"), Bytes.toBytes("Columns"), Bytes.toBytes(columns.substring(2)));
			T_Property.put(p);//update the Table_Property by adding column info
			desc.addFamily(new HColumnDescriptor(Bytes.toBytes("cf1")));//default cf is cf1
			admin.createTable(desc);
		}else{
			System.out.println("The table named "+tableName+" is in LogBase already.");
		}
		
	}
	

	/**
	 * create a table with a specified table name and a set of column names which are grouped by different sub-sets.
	 */
	public void createTable(String tableName, Vector<Vector<String>> columnGroups, Vector<Vector<Class>> dataTypes, String[] groupNames) 
			throws IOException{
	    if (columnGroups.size()!=groupNames.length) {
	    	System.out.println("The number of column groups is not equal to that of group names.");
	    }
		else if (columnGroups.size()!=dataTypes.size())
		{
			System.out.println("The number of columns is not equal to that of data types.");
		}
		else if (!TableExists(tableName)){
			if (columnGroups.get(0).size()!=dataTypes.get(0).size()){
				System.out.println("The number of columns is not equal to that of data types.");
			}else{
				if (!TableExists("Table_Property")){
					HTableDescriptor desc = new HTableDescriptor("Table_Property");
					desc.addFamily(new HColumnDescriptor(Bytes.toBytes("cf1")));
					admin.createTable(desc);
				}
				HTableDescriptor desc = new HTableDescriptor(tableName);
				HTable T_Property = new HTable(conf, "Table_Property");
				Put p=null;		
				String columns = "";
				for (int i=0;i<columnGroups.size();++i){					
					for(int j=0;j<columnGroups.get(i).size();++j){
						columns = columns+", "+columnGroups.get(i).get(j);
						String tmp = tableName+"_"+columnGroups.get(i).get(j);
						p = new Put(Bytes.toBytes(tmp));
						p.add(Bytes.toBytes("cf1"), Bytes.toBytes("DataType"), Bytes.toBytes(dataTypes.get(i).get(j).toString()));
						p.add(Bytes.toBytes("cf1"), Bytes.toBytes("IdxInfo"), Bytes.toBytes("null"));
						p.add(Bytes.toBytes("cf1"), Bytes.toBytes("ColFamily"), Bytes.toBytes(groupNames[i]));//default cf is cf1
						T_Property.put(p);//update Table_Property for the table, T_col(cf1): (1)DataType (2)IdxInfo (3)ColFamily
					}
					desc.addFamily(new HColumnDescriptor(Bytes.toBytes(groupNames[i])));
				}
				p = new Put(Bytes.toBytes(tableName));
				p.add(Bytes.toBytes("cf1"), Bytes.toBytes("Columns"), Bytes.toBytes(columns.substring(2)));
				T_Property.put(p);	//update the Table_Property by adding column info	
				admin.createTable(desc);
			}
		}else{
			System.out.println("The table named "+tableName+" is in LogBase already.");
		}
	}
	
	
	/**
	 * check whether there is a table with a specified table name.
	 */
    public boolean TableExists(String tableName) throws IOException {
    	return admin.tableExists(tableName);
    }
		
    
    /**
     * return a LogTable object handler for a table with a specified table name if it exists.
     */
	public LogTable getExistingTable(String tableName) throws IOException{		
		
		// get the block info
		String columnName = "BlockID";
		byte[] row = Bytes.toBytes(tableName+"_"+columnName);
		HTable T_Property = new HTable(conf, "Table_Property");
		Get g = new Get(row);
		g.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("BlockSize"));
		Result R = T_Property.get(g);
		boolean blocked = false;
		int blockSize = 1;
		if (R==null||R.raw().length==0){
			blocked=false;
		} else{
			blocked=true;
			blockSize= Integer.valueOf(new String(R.raw()[0].getValue())); 
		}		
		LogTable ret = new LogTable (new HTable(conf, tableName), blocked, blockSize);
		// pass the conf info
		ret.setConf(this.conf);
		
		return ret;
	}
		
	
	/**
	 * list all tables kept in the data base.
	 */
	public String[] listTables() throws IOException{
		HTableDescriptor[] tables = admin.listTables();
		//System.out.println("tables.length = " + String.valueOf(tables.length));
		
		if (tables == null || tables.length == 0) return null;
		
		String[] ret = new String[tables.length-1];
		int j=0;
		for (int i = 0; i < tables.length; ++i){
			String  tmp = tables[i].getNameAsString();
			//System.out.println("tables.getNameAsString = " + tmp);
			if (!tmp.equals("Table_Property")) {
				ret[j] = tmp;
				++j;
			}			
		}
		return ret;
	}
	
	
	/**
	 * delete a table with a specified name.
	 */
	public void deleteTable(String tableName) throws Exception{
		if (admin.tableExists(tableName)){
		  if (!tableName.equals("Table_Property")){
			if (admin.isTableEnabled(tableName)){
				admin.disableTable(tableName);
			}
		  }
		}
	}
		
	
	
}