package sg.edu.nus.test;


//import java.io.File;
//import java.io.IOException;
//import java.util.Scanner;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.hbase.HBaseTestingUtility;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.client.ResultScanner;
//import org.apache.hadoop.hbase.client.Scan;
//import org.apache.hadoop.hbase.util.Bytes;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import sg.edu.nus.LogBaseAPI.LogAdmin;
//import sg.edu.nus.LogBaseAPI.LogTable;
//
//import sg.edu.nus.logbase.crindex.*;

/**
 * Class to test HBaseAdmin.
 * Spins up the minicluster once at test start and then takes it down afterward.
 * Add any testing of HBaseAdmin functionality here.
 */
public class TestLogBaseCRIndex {
//  final Log LOG = LogFactory.getLog(getClass());
//  private final static HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
//  private LogAdmin admin;
//
//  @BeforeClass
//  public static void setUpBeforeClass() throws Exception {
//    TEST_UTIL.getConfiguration().setInt("hbase.regionserver.msginterval", 100);
//    TEST_UTIL.getConfiguration().setInt("hbase.client.pause", 250);
//    TEST_UTIL.getConfiguration().setInt("hbase.client.retries.number", 6);
//    TEST_UTIL.startMiniCluster(3);
//  }
//
//  @AfterClass
//  public static void tearDownAfterClass() throws Exception {
//    TEST_UTIL.shutdownMiniCluster();
//  }
//
//  @Before
//  public void setUp() throws Exception {
//    this.admin = new LogAdmin(TEST_UTIL.getConfiguration());
//  }
//
//  @Test
//  public void testRoutineOperations() throws IOException {
//
//	  System.out.println("*************************************************");
//
//
//
//	  //Step 1. Create a table with a specified table name and specified column names.
//	  admin.createTable("CMOP2013", new String[]{"salinity","temperature","conductivity","date"}, new Class[]{Double.class,Double.class,Double.class,String.class});
//      //String[] tableNames = admin.listTables();
//	  final LogTable table = admin.getExistingTable("CMOP2013");//tableNames[0]);
//	  //table.setBlockFlag(false);
//
//
//	  try {
//		Thread.sleep(2000);
//	} catch (InterruptedException e) {
//		e.printStackTrace();
//	}
//
//
//	Scanner reader = new Scanner(new File("CMOP.txt"));
//	int datasetRepeatTime = 10;
//	int tupleCount = 0;
//	int tupleNumber = 1000;
//
//	double salinity, temperature, conductivity;
//	String date, time;
//
//	byte[] r, s, t, c, d;
//	s = Bytes.toBytes("salinity");
//	t = Bytes.toBytes("temperature");
//	c = Bytes.toBytes("conductivity");
//	d = Bytes.toBytes("date");
//
//	byte[][] cols = new byte[][]{s, t, c, d};
//
//	byte[][] value = new byte[4][];
//
//	while (true){
//
//		if (reader.hasNext() == false){
//			LogIndexDebug.printInfo(tupleCount+" tuples has been inserted and reaches EOF");
//			if (datasetRepeatTime > 0){
//				--datasetRepeatTime;
//				reader.close();
//				reader = null;
//				reader = new Scanner(new File("CMOP.txt"));
//			}
//			else{
//				break;
//			}
//		}
//
//		date = reader.next();
//		time = reader.next();
//		salinity = reader.nextDouble();
//		temperature = reader.nextDouble();
//		conductivity = reader.nextDouble();
//
//		++tupleCount;
//
//		r = Bytes.toBytes("k"+tupleCount);
//		value[0] = Bytes.toBytes(salinity);
//		value[1] = Bytes.toBytes(temperature);
//		value[2] = Bytes.toBytes(conductivity);
//		value[3] = Bytes.toBytes(date+"|"+time);
//
//		//2013-12-13 wangsheng
//		table.put(r, cols, value);
//
//		if (tupleCount%tupleNumber == 0) break;
//	}
//
//	  System.out.println("*************************************************");
//	  System.out.println("*************************************************");
//	  System.out.println("*************************************************");
//	  System.out.println("*************************************************");
//	  System.out.println("*************************************************");
//
//	  /*
//
//	  //Step 3. Test Get
//	  for (int k = 0; k < tupleNumber; ++k){
//		  byte[] row = Bytes.toBytes(k);
//		  Result ret = table.get(row);
//	  	for(int i=0; i<ret.size(); i++){
//	  		if (ret.raw()[i].matchingQualifier(s)) System.out.println(" get salinity = " + Bytes.toDouble(ret.raw()[i].getValue()));
//	  		if (ret.raw()[i].matchingQualifier(t)) System.out.println(" get temperature = " + Bytes.toDouble(ret.raw()[i].getValue()));
//	  		if (ret.raw()[i].matchingQualifier(c)) System.out.println(" get conductivity = " + Bytes.toDouble(ret.raw()[i].getValue()));
//	  		if (ret.raw()[i].matchingQualifier(d)) System.out.println(" get date = " + Bytes.toString(ret.raw()[i].getValue()));
//	  	}
//  	  }
//  	  */
//
//	  //Step 4. Test Scanner
//
//	  ResultScanner scanner = table.getValueScanner(Bytes.toBytes("salinity"),20.0,25.0,null);
//	  while (true)
//	  {
//		  Result ret = scanner.next();
//		  if (ret == null) break;
//		  System.out.println(" get key = " + Bytes.toString(ret.raw()[0].getRow()));
//		  for(int i=0; i<ret.size(); i++)
//		  {
//		  		if (ret.raw()[i].matchingQualifier(s)) System.out.println(" get salinity = " + Bytes.toDouble(ret.raw()[i].getValue()));
//		  		if (ret.raw()[i].matchingQualifier(t)) System.out.println(" get temperature = " + Bytes.toDouble(ret.raw()[i].getValue()));
//		  		if (ret.raw()[i].matchingQualifier(c)) System.out.println(" get conductivity = " + Bytes.toDouble(ret.raw()[i].getValue()));
//		  		if (ret.raw()[i].matchingQualifier(d)) System.out.println(" get date = " + Bytes.toString(ret.raw()[i].getValue()));
//		  }
//	  }
//  }
}


  