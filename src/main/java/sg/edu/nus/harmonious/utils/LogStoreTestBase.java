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

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.wal.LogStoreCache;
import org.apache.hadoop.util.Tool;

public abstract class LogStoreTestBase implements Tool {

  protected int numRecords = 100;
  protected int numQualifier = 1;
  protected int numFamily = 1;
  protected int numRandomAccess = 10;
  protected boolean local = false;
  protected Configuration conf = new Configuration();
  protected FileSystem fs = null;

  protected int rowLength = 10;
  protected int valLength = 1024;
  protected int familyLength = 2;
  protected int qualifierLength = 2;
  protected int fileThreshold = 1 << 18;
  protected byte[] row = null;
  protected byte[] value = null;
  protected byte[] family = null;
  protected byte[] qualifier = null;
  
  protected String tableName = "table";
  
//WANGSHENG
  protected String path = "/tmp";
  protected String hdfspath = "hdfs://awan-1-23-0:12345";
  
  protected Random random = new Random();
  
  //Wang Sheng
  protected boolean write = true;
  protected boolean rand = true;
  protected boolean scan = true;
  
  public void generatedKey(int rowID){
    HUtils.format(rowID, row);
  }
  
  public void generateValue(int rowID){
    random.setSeed(rowID);
    random.nextBytes(value);
  }
  
  public void parseArguments(String[] args) throws IOException{
	    for(int i = 0;i < args.length;i ++){
	      if(args[i].equalsIgnoreCase("-local")){
	        this.local = Boolean.parseBoolean(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-nRows")){
	        this.numRecords = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-keyLength")){
	        this.rowLength = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-valLength")){
	        this.valLength = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-familyLength")){
	        this.familyLength = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-qualifierLength")){
	        this.qualifierLength = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-nFamily")){
	        this.numFamily = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-nQualifier")){
	        this.numQualifier = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-nRandom")){
	        this.numRandomAccess = Integer.parseInt(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-path")){	//	WANGSHENG: Set New Path
	    	this.path = args[++ i];
	      }else if(args[i].equalsIgnoreCase("-hdfspath")){	//	WANGSHENG: Set New Path
	      	this.hdfspath = args[++ i];
	      }else if(args[i].equalsIgnoreCase("-logcache")){	//	WANGSHENG: Set Log Cache
	          	LogStoreCache.setCacheEnable(Boolean.parseBoolean(args[++ i]));
	      }else if(args[i].equalsIgnoreCase("-cacheSize")){	//	WANGSHENG: Set Log Cache
	        	LogStoreCache.setCacheHeapSize(Long.parseLong(args[++ i]));
	      }else if(args[i].equalsIgnoreCase("-blockSize")){	//	WANGSHENG: Set Log Cache
	        	LogStoreCache.setBlockSize(Integer.parseInt(args[++ i]));
	      }else if(args[i].equalsIgnoreCase("-write")){
	    	this.write = Boolean.parseBoolean(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-random")){
	  	    this.rand = Boolean.parseBoolean(args[++ i]);
	      }else if(args[i].equalsIgnoreCase("-scan")){
	  	    this.scan = Boolean.parseBoolean(args[++ i]);
	      }else{
	        throw new IOException("unrecognized arguments: " + args[i]);
	      }
	    }
    
    this.row = new byte[this.rowLength];
    this.value = new byte[this.valLength];
    this.family = new byte[this.familyLength];
    this.qualifier = new byte[this.qualifierLength];
    
    /*
    if(this.local){
      fs = FileSystem.getLocal(conf);
    }else{
      fs = FileSystem.get(conf);
    }*/
    
    if(this.local){
        fs = FileSystem.getLocal(conf);
      }else{
        fs = FileSystem.get(URI.create(hdfspath),conf);
      }
  }
  
  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  @Override
  public Configuration getConf() {
    return conf;
  }
  

  /**
   * Here just create the keyvalue, without any initialization
   * @param row
   * @param columnFamily
   * @param columnQualifier
   * @param value
   * @return
   */
  public KeyValue createKeyValue(byte[] row, byte[] columnFamily, byte[] columnQualifier, byte [] value){
    return new KeyValue(row, columnFamily, columnQualifier, value);
  }
  
  public void generateRow(int rowID, byte[] row, int offset, int length){
    HUtils.format(rowID, row, offset, length);
  }
  
  public  void generateColumnFamily(int rowID, byte[] family, int offset, int length){
    generateColumnFamily(rowID, family, offset, this.numFamily);
  }
  public  void generateColumnFamily(int rowID, byte[] family, int offset, int length, int nFamily){
    random.setSeed(rowID);
    HUtils.format(random.nextInt(nFamily), family, offset, length);
  }
  
  public void generateColumnQualifier(int rowID, byte[] columnQualifier, int offset, int length){
    generateColumnQualifier(rowID, columnQualifier, offset, length, this.numQualifier);  
  }
  public void generateColumnQualifier(int rowID, byte[] columnQualifier, int offset, int length, int nQualifier){
    random.setSeed(rowID);
    HUtils.format(random.nextInt(nQualifier), columnQualifier, offset, length);
  }
  
  public void generateValue(int rowID, byte[] value, int offset, int length){
    random.setSeed(rowID);
    byte b = (byte) (random.nextInt(95) + ' ');
    for(int i = offset + length - 1;i >= offset;i --){
      value[i] = b;
    }
  }
  
  public void generateKey(int rowID, KeyValue.Key key){
    generateKey(rowID, key, this.numFamily, this.numQualifier);
  }
  
  public void generateKey(int rowID, KeyValue.Key key, int nFamily, int nColumn){
    generateRow(rowID, key.getBuffer(), key.getRowOffset(), key.getRowLength());
    generateColumnFamily(rowID, key.getBuffer(), key.getFamilyOffset(), key.getFamilyLength(), nFamily);
    generateColumnQualifier(rowID, key.getBuffer(), key.getQualifierOffset(), key.getQualifierLength(), nColumn);
  }
  
  public void generateKey(int rowID, KeyValue kv, int nFamily, int nColumn){
    generateRow(rowID, kv.getBuffer(), kv.getRowOffset(), kv.getRowLength());
    generateColumnFamily(rowID, kv.getBuffer(), kv.getFamilyOffset(), kv.getFamilyLength(), nFamily);
    generateColumnQualifier(rowID, kv.getBuffer(), kv.getQualifierOffset(), kv.getQualifierLength(), nColumn);
  }
  
  public void generateKeyValue(int rowID, KeyValue kv){
    generateKeyValue(rowID, kv, this.numFamily, this.numQualifier);
  }
  
  public void generateKeyValue(int rowID, KeyValue kv, int nFamily, int nColumn){
    generateKey(rowID, kv, nFamily, nColumn);
    generateValue(rowID, kv.getBuffer(), kv.getValueOffset(), kv.getValueLength());
  }

}
