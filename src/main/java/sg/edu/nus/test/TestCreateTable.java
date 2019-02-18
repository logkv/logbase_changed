package sg.edu.nus.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import sg.edu.nus.LogBaseAPI.LogAdmin;
import sg.edu.nus.LogBaseAPI.LogTable;

import java.io.IOException;

public class TestCreateTable {
	   public static void main (String[] argv) throws IOException, ClassNotFoundException{

           Configuration hBaseConfig =  HBaseConfiguration.create();
           hBaseConfig.setInt("timeout", 120000);
           hBaseConfig.set("hbase.master", "localhost:60000");
           hBaseConfig.set("hbase.zookeeper.quorum","localhost");
           hBaseConfig.set("hbase.zookeeper.property.clientPort", "2282");
           String tableName = "t1";
           if (argv.length > 1) {
               tableName = argv[0];
           }

		   LogAdmin admin = new LogAdmin(hBaseConfig);

           System.out.println(" begin create tabe " + tableName);
           //Create a table with a specified table name, column names, and data type of each column.
		   admin.createTable(tableName, new String[]{"c1","c2"}, new Class[]{String.class, String.class});
           System.out.println(" finish create tabe " + tableName);
	   }
	}
