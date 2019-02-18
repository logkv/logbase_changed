package sg.edu.nus.test;


//import java.io.IOException;
//import java.util.List;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.hbase.HBaseTestingUtility;
//import org.apache.hadoop.hbase.client.Result;
//import org.apache.hadoop.hbase.client.ResultScanner;
//import org.apache.hadoop.hbase.util.Bytes;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import sg.edu.nus.LogBaseAPI.LogAdmin;
//import sg.edu.nus.LogBaseAPI.LogTable;


/**
 * Class to test HBaseAdmin.
 * Spins up the minicluster once at test start and then takes it down afterward.
 * Add any testing of HBaseAdmin functionality here.
 */
public class TestLogBase {
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
//	  //Step 1. Create a table with a specified table name and specified column names.
//	  admin.createTable("testLogBase", new String[]{"c1", "c2", "c3"}, new Class[]{String.class,String.class,String.class}, 3);
//      //String[] tableNames = admin.listTables();
//	  final LogTable table = admin.getExistingTable("testLogBase");//tableNames[0]);
//	  System.out.print("table.blocked = ");
//	  System.out.println(table.blocked);
//	  System.out.print("table.blockSize = ");
//	  System.out.println(table.blockSize);
//
//
//	  try {
//		Thread.sleep(500);
//	} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//
//
//
//	  //Step 2 Insert a record in the table
//	  byte[] row = Bytes.toBytes("r1"); //row name
//	  byte[][] cols = new byte[][]{Bytes.toBytes("c1"), Bytes.toBytes("c2"), Bytes.toBytes("c3")}; //columns’ names
//	  byte[][] value = new byte[][]{Bytes.toBytes("v11"), Bytes.toBytes("v12"), Bytes.toBytes("v13")}; //values
//	  table.put(row, cols, value);
//	  for (int i=2;i<=12;++i){
//		  String strTmp = String.valueOf(i);
//		  row = Bytes.toBytes("r"+strTmp); //row name
//		  value = new byte[][]{Bytes.toBytes("v"+strTmp+"1"), Bytes.toBytes("v"+strTmp+"2"), Bytes.toBytes("v"+strTmp+"3")}; //values
//		  table.put(row, cols, value);
//	  }
//	  Result ret =null;
//
//
//	  System.out.println("*************************************************");
//	  System.out.println("*************************************************");
//
//
//	  //Step 3.1 Get a record with row name as the key
//	  ret = table.get(row);
//	  for(int i=0; i<ret.size(); i++){
//	    System.out.println(" get r1 = " + (new String (ret.raw()[i].getValue())));
//	  }
//	  row = Bytes.toBytes("r100");
//	  ret = table.get(row);
//	  if (ret==null)
//		  System.out.println(" get r100 = null");
//	  else
//		  System.out.println(ret);
//
//	  System.out.println("ret.size()=");
//	  System.out.println(ret.size());
//	  for(int i=0; i<ret.size(); i++){
//	    System.out.println(" get r100 = " + (new String (ret.raw()[i].getValue())));
//	  }
//
//
//
//	  //Step 3.2 Get a record under specified row name and column names
////	  ret = table.get(row, new byte[][]{Bytes.toBytes("c1")});
////	  for(int i=0; i<ret.size(); i++){
////	    System.out.println(" get = " + new String (ret.raw()[i].getValue()));
////	  }
//
//
//	  //Step 4.1 Scan all columns of the table
////	  Iterable<Result> rets = table.tableScan();
////	  for (Result res:rets){
////		  for (int i=0;i<res.size();++i){
////			  System.out.println("tableScan= " + (new String (res.raw()[i].getValue())));
////		  }
////	  }
//
//	  //Step 4.2. Scan specified columns of the table.
////	  Iterable<Result> rets = table.tableScan(new byte[][]{Bytes.toBytes("c3")});
////	  for (Result res:rets){
////		  for (int i=0;i<res.size();++i){
////			  System.out.println(" tableScan on c3 = " + (new String (res.raw()[i].getValue())));
////		  }
////      }
//
//	  //Step 4.3. Scan specified columns of the table from a starting row.
////	  Iterable<Result> rets = table.tableScan(Bytes.toBytes("r3"));
////	  for (Result res:rets){
////		  for (int i=0;i<res.size();++i){
////			  System.out.println(" tableScan from r3 = " + (new String (res.raw()[i].getValue())));
////		  }
////      }
//
//	  //Step 4.4. Scan specified columns of the table from a starting row and stop at an ending row.
////	  Iterable<Result> rets = table.tableScan(Bytes.toBytes("r3"),Bytes.toBytes("r8"));
////	  for (Result res:rets){
////		  for (int i=0;i<res.size();++i){
////			  System.out.println(" tableScan for rowkeys within [r3,r8) = " + (new String (res.raw()[i].getValue())));
////		  }
////      }
//
//
//	  //step 5.1 get a block
////	  Iterable<Result> res = table.get("2");
////	  //System.out.println("==="+res);
////	  for (Result s:res){
////		  //System.out.println(s);
////		  for (int i=0;i<s.size();++i){
////			  System.out.println(" get block = " + (new String (s.raw()[i].getValue())));
////		  }
////	  }
//
//	  //step 5.2 get blocks
////	  res = table.get(new String[]{"1", "5"});
////	  for (Result s:res){
////		  //System.out.println(s);
////		  for (int i=0;i<s.size();++i){
////			  System.out.println(" get block = " + (new String (s.raw()[i].getValue())));
////		  }
////	  }
//
//
//	  //step 6.1 list up all columns
////	  String[] columns =  table.listColumns();
////	  for (int i=1; i<=columns.length;++i){
////		  System.out.println("column "+String.valueOf(i)+" = "+columns[i-1]);
////	  }
//
//
//	  //step 6.2 list up the data type of each column
////	  Class cls =null;
////	  try {
////		 cls = table.getDataType(Bytes.toBytes("c1"));
////	  } catch (ClassNotFoundException e) {
////		 // TODO Auto-generated catch block
////		 e.printStackTrace();
////	  }
////	  System.out.println(cls);
//
//
//	  //step 7.1 list up the index info of each column
////	  String[] IdxInfo =  table.listIndices();
////	  for (int i=1; i<=IdxInfo.length;++i){
////		  System.out.println("Index on column "+String.valueOf(i)+" = "+IdxInfo[i-1]);
////     }
//
//	  //step 7.2 create an index on a specified column
//
//
//
//  }
  

  
}


  