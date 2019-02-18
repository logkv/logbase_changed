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

package org.apache.hadoop.hbase.regionserver.wal;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.MemIndex;
import org.apache.hadoop.io.LongWritable;

import sg.edu.nus.test.Debug;

public abstract class LogScanner{

  public static enum ScannerType{
    // Sequential scan the logs, log by log, the read entries are likely un-ordered 
    ByFile,
    
    // Random seek the log files via the key order
    ByKey
  }
  MemIndex index = null;
  protected SortedMap<LongWritable, SequenceFileLogReader> readers = null;
  
  protected SortedMap<LongWritable, Path> files = null;
  FileSystem fs = null;
  Configuration conf = null;
  
  LongWritable currentFileNum = new LongWritable();
  SequenceFileLogReader currentFileReader = null;
  
  //WangSheng
  LogStoreCache logCache = null;
  
  void initial(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf) throws IOException{
    if(readers != null){
      close();
    }else {
      readers = new TreeMap<LongWritable, SequenceFileLogReader>();
    }
    this.files = outputfiles;
    this.fs = fs;
    this.conf = conf;
  }

  public void close() throws IOException{
    Collection<SequenceFileLogReader> rs = readers.values();
    for(SequenceFileLogReader r : rs){
      r.close();
    }
    readers.clear();
  }

  protected SequenceFileLogReader getReader(LongWritable logFileNum) throws IOException{
  	Path path = this.files.get(logFileNum);
  	if(path == null){
  		throw new IOException("Invalid file num: " + logFileNum);
  	}
//  	System.out.println("In LogScanner, path is: " + path);
  	SequenceFileLogReader ret = this.readers.get(logFileNum);
//  	System.out.println("*************");
//  	System.out.println("show Reader:");
//  	for(Map.Entry<LongWritable, SequenceFileLogReader> entry : readers.entrySet()){
//  		System.out.println(entry.getKey() + " -> " + entry.getValue());
//  	}
//  	System.out.println("*************");
  	if(ret == null){
  		ret = new SequenceFileLogReader();
  		ret.init(this.fs, path, this.conf);
  		this.readers.put(new LongWritable(logFileNum.get()), ret);
  	}
  	return ret;
  }
  
  public void printLogFiles(){
	Debug.debug(" logfiles -- \n" + files.toString());  
  }
  
  abstract public void init(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf, MemIndex index)throws IOException;
  
  abstract public void init(SortedMap<LongWritable, Path> outputfiles, FileSystem fs, Configuration conf, MemIndex index, LogStoreCache cache)throws IOException;
  
  public abstract KeyValue next() throws IOException;
  
  public abstract void seek(KeyValue.Key key) throws IOException;
  
}
