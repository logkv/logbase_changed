/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sg.edu.nus.harmonious.utils; 

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.util.Tool;

public class HUtils { 
  final static int defaultPort = 8461;

  /**
   * List the files/directories in "dir"
   * @param fs
   * @param dir
   * @return
   * @throws IOException
   */
  public static Path[] listFilesPath(FileSystem fs, Path dir) throws IOException {
    FileStatus[] colStatus = fs.listStatus(dir);
    if(colStatus == null)throw new IOException(dir + " do not exist");
    Path path[] = new Path[colStatus.length];
    for (int i = 0; i < colStatus.length; i++) {
      path[i] = colStatus[i].getPath();
    }
    return path;
  }
  
  /**
   * lists the files name in dir
   * @param fs
   * @param dir
   * @return
   * @throws IOException
   */
  public static String[] listFilesName(FileSystem fs, Path dir) throws IOException{ 
    Path[] p = listFilesPath(fs, dir);
    String[] ret = new String[p.length];
    for(int i = 0;i < p.length;i ++){
      ret[i] = p[i].getName();
    }
    return ret;
  }

  public static Path[] generatePaths(Path dir, String[] colName){
    Path path[] = new Path[colName.length];
    for(int i = 0;i < path.length;i ++){
      path[i] = new Path(dir, colName[i]);
    }
    return path;
  }
  
  public static Path[] generatePaths(Path[] dirs, String suffix){
    Path path[] = new Path[dirs.length];
    for(int i = 0;i < path.length;i ++){
      path[i] = new Path(dirs[i], suffix);
    }
    return path;
  }
      
  static ArrayList<String> columns = new ArrayList<String>();
  public synchronized static void accumulatedInputColumns(String[] newColumns){
    for(String col : newColumns){
      columns.add(col);
    }
  }

  public static final String RPCOffsetServer = "offset.rpc.server";
  public static final String RPCOffsetPort = "offset.rpc.port"; 

  /**
   * Offset server should be initialed before the job is submitted
   * The conf should append the address 
   * @param conf
   * @throws IOException
   */
  private static void initConfiguration(Configuration conf) throws IOException{
    //initial the rpc configuration
    
    String hostAddr = getLocalHostName();
    conf.set(RPCOffsetServer, hostAddr);
    //initial the rpc
  }
   
  public static void startOffsetServer(Configuration conf) throws IOException{
    throw new IOException("should not use this function");
  } 
  
  public static int binarySearch(WritableComparable[] values, WritableComparable key){
    return binarySearch(values, key, 1);
  }
  /**
   * 
   * @param values the value of the beginning of each block. 
   * @param key the key to be searched 
   * @param orderFactor ascending order(1), descending order(-1); 
   * 
   * @return the block id that likely contains the key 
   */
  public static int binarySearch(WritableComparable[] values, WritableComparable key, int orderFactor){
    int begin = 0, end = values.length - 1;  
    if(orderFactor * values[0].compareTo(key) > 0)return -1;
    while(begin <= end){
      int mid = (end + begin) / 2;
      if(orderFactor * values[mid].compareTo(key) < 0) begin = mid + 1;
      else end = mid - 1;
    }
    return begin;
  }

  
  private static void testBinarySearch(long[] v, long query[], int[] ans, int orderFlag){

    LongWritable[] vals = new LongWritable[v.length];
    for(int i = 0; i < v.length;i ++)vals[i] = new LongWritable(v[i]);
    
    LongWritable k = new LongWritable();
    for(int i = 0;i < query.length;i ++){
      k.set(query[i]);
      int ret = binarySearch(vals, k, orderFlag);
      System.out.println((ans[i] == ret) + ": " + ans[i] + ":" + ret);
    }
  }

  public static java.net.InetAddress getLocalInetAddress(){
    try {
    java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();//aa java.net.InetAddress.getLocalHost().getHostAddress();
    return localMachine;
    }
    catch (java.net.UnknownHostException uhe) { // [beware typo in code sample -dmw]
      uhe.printStackTrace();
    }
    return null;
  }
   
  public static String getLocalHostName() {
    return getLocalInetAddress().getHostAddress();
  }
  public static void runAndRecordTime(Tool obj, String args[])throws Exception{
    long begin = System.currentTimeMillis();
    obj.run(args);
    long end = System.currentTimeMillis(); 

    System.out.print("Finish " + obj.getClass() + "[");
    for(String tmp : args)
      System.out.print(tmp + " "); 
    System.out.println("] in " + (end - begin) / 1000.0 + " seconds");
  }
   
//  public static Class<?> getCodec(String codecName){
//    if(codecName != null){
//      if(codecName.indexOf("Lzo") != -1) return com.hadoop.compression.lzo.LzoCodec.class;
//      else if(codecName.indexOf("Gzip") != -1) return org.apache.hadoop.io.compress.GzipCodec.class;
//      else if(codecName.indexOf("BZip2") != -1) return org.apache.hadoop.io.compress.BZip2Codec.class;
//    }
//    System.out.println("codecName is " + codecName);
//    return null;
//  }
//  
//  public static CompressionCodec getCodecInstance(String codecName){
//    if(codecName != null){
//      if(codecName.indexOf("Lzo") != -1) return new com.hadoop.compression.lzo.LzoCodec();
//      else if(codecName.indexOf("Gzip") != -1) return new org.apache.hadoop.io.compress.GzipCodec();
//      else if(codecName.indexOf("BZip2") != -1) return new org.apache.hadoop.io.compress.BZip2Codec();
//    }
//    System.out.println("codecName is " + codecName);
//    return null;
//  }

  public static List<String> listSlaves() throws IOException {
    String hadoopPath = System.getenv("HADOOP_HOME");
    final String slavePath = hadoopPath + "/conf/slaves";
    String slave;
    List<String> slaves = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(slavePath));
    while ((slave = reader.readLine()) != null) {
      slaves.add(slave);
    }
    reader.close();
    return slaves;
  }
  
  public static void sleep(long milliseconds){
    try{
      Thread.sleep(milliseconds);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /*
   * Format passed integer.
   * @param number
   * @return Returns zero-prefixed length-byte wide decimal version of passed
   * number (Does absolute in case number is negative).
   */
  public static byte [] format(final int number, int length) {
    byte [] b = new byte[length];
    int d = Math.abs(number);
    for (int i = b.length - 1; i >= 0; i--) {
      b[i] = (byte)((d % 10) + '0');
      d /= 10;
    }
    return b;
  }
  
  /*
   * Format passed integer.
   * @param number
   * @return Returns zero-prefixed length-byte wide decimal version of passed
   * number (Does absolute in case number is negative).
   */
  public static void format(final int number, byte[] b) {
    format(number, b, 0, b.length);
  }
  
  public static void format(final int number, byte[] b, int offset, int length){
    int d = Math.abs(number);
    for (int i = length - 1 + offset; i >= offset; i--){
      b[i] = (byte)((d % 10) + '0');
      d /= 10;
    }
  }
  
  /**
   * Both getVIntSize() & getVLongSize depends on 
   * the method of WritableUtils.writeVInt() & WritableUtils.writeVLong(); 
   * @param i
   * @return
   */
  public static int getVIntSize(int i){
    return getVLongSize(i);
  }
  public static int getVLongSize(long i){
    if (i >= -112 && i <= 127)
      return 1;
    int len = -112;
    if (i < 0) {
      i ^= -1L; // take one's complement'
      len = -120;
    }

    long tmp = i;
    while (tmp != 0) {
      tmp = tmp >> 8;
      len--;
    }
    len = (len < -120) ? -(len + 120) : -(len + 112);
    return 1 + len;
  }

  /**
   * Return the serialized bytes length according to Bytes.writeByteArray()
   * @param buf
   * @return
   */
  public static int getSerializedBytesLength(byte[] buf){
    int arrayLen = 0;
    arrayLen = (buf == null ? 0 : buf.length);
    return HUtils.getVIntSize(arrayLen) + arrayLen; 
  }
  
}
