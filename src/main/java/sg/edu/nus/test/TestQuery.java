package sg.edu.nus.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import sg.edu.nus.LogBaseAPI.LogAdmin;
import sg.edu.nus.LogBaseAPI.LogTable;

import java.io.IOException;

public class TestQuery {
	   public static void main (String[] argv) throws IOException, ClassNotFoundException{

           Configuration hBaseConfig =  HBaseConfiguration.create();
           hBaseConfig.setInt("timeout", 120000);
           hBaseConfig.set("hbase.master", "localhost:60000");
           hBaseConfig.set("hbase.zookeeper.quorum","localhost");
           hBaseConfig.set("hbase.zookeeper.property.clientPort", "2282");

		   LogAdmin admin = new LogAdmin(hBaseConfig);

//           System.out.println(" begin create tabe t1 ");
//           //Create a table with a specified table name, column names, and data type of each column.
//		   admin.createTable("t1", new String[]{"c1","c2"}, new Class[]{String.class, String.class});
//           System.out.println(" finish create tabe t1 ");
		   final LogTable table = admin.getExistingTable("t1");

		   //Insert a record in the table
           System.out.println(" begin insert value into tabe t1 ");
		   byte[] row = Bytes.toBytes("r1"); //row name   
		   byte[][] cols = new byte[][]{Bytes.toBytes("c1"), Bytes.toBytes("c2")}; //columns’ names
		   byte[][] value = new byte[][]{Bytes.toBytes("v1" + System.currentTimeMillis()), Bytes.toBytes("v2" + System.currentTimeMillis())}; //values
		   table.put(row, cols, value);
           System.out.println(" end insert value into tabe t1 ");
		   //Get a record with row name as the key
		   Result ret = table.get(row);  
		   for(int i=0; i<ret.size(); i++){
			   System.out.println(" get = " + (new String (ret.raw()[i].getValue()))); 
		   }
		   //Get a record with row name and column name as keys
		   ret = table.get(row, Bytes.toBytes("c1"));  
		   for(int i=0; i<ret.size(); i++){
			   System.out.println(" get = " + new String (ret.raw()[i].getValue())); 
		   }
	   }
	}
