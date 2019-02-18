package sg.edu.nus.test;

import java.io.IOException;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;

import sg.edu.nus.LogBaseAPI.LogAdmin;
import sg.edu.nus.LogBaseAPI.LogTable;

public class Test {	
	   public static void main (String[] argv) throws IOException, ClassNotFoundException{

           Configuration hBaseConfig =  HBaseConfiguration.create();
           hBaseConfig.setInt("timeout", 120000);
           hBaseConfig.set("hbase.master", "localhost:60000");
           hBaseConfig.set("hbase.zookeeper.quorum","localhost");
           hBaseConfig.set("hbase.zookeeper.property.clientPort", "2282");
           String tableName = "t1";
           if (argv.length >= 1) {
               tableName = argv[0];
           }
           int times = 100;
           if (argv.length >= 2) {
               times = Integer.valueOf(argv[1]);
           }

		   LogAdmin admin = new LogAdmin(hBaseConfig);

		   final LogTable table = admin.getExistingTable(tableName);
		   //Insert a record in the table
           System.out.println(" begin insert value into tabe " + tableName);

           long startTime = System.currentTimeMillis();

           for (int i=0;i<times;i++) {
               byte[] row = Bytes.toBytes("r1" + i); //row name
               byte[][] cols = new byte[][]{Bytes.toBytes("c1" + i), Bytes.toBytes("c2" + i)}; //columns’ names
               byte[][] value = new byte[][]{Bytes.toBytes("v1" + i), Bytes.toBytes("v2" + i)}; //values
               table.put(row, cols, value);
           }

           long duration = System.currentTimeMillis() - startTime;
           System.out.println(" end insert value into tabe , total count : " + times +
                   ", duration: " + duration + "(ms)" + "; tps: " + (times / duration) * 1000);
	   }
	}
