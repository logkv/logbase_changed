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

package sg.edu.nus.harmonious;

//import sg.edu.nus.harmonious.regionserver.wal.HSequenceFileLogReader;
//import sg.edu.nus.harmonious.regionserver.wal.HSequenceFileLogWriter;
//import sg.edu.nus.harmonious.regionserver.wal.HarmoniousLogKey;
//import sg.edu.nus.harmonious.regionserver.wal.LogFile;

public class HarmoniousArg {
  
  // "hbase.regionserver.hlog.keyclass"
//  public static String LogKeyClass = "harmonious.regionserver.hlog.keyclass"; 
//  public static Class<? extends HarmoniousLogKey> DefaultLogKeyClass = HarmoniousLogKey.class;
  
  // "hbase.regionserver.hlog.blocksize";
  public static String LogBlockSize = "harmonious.regionserver.hlog.blocksize";
  public static int DefaultLogBlockSize = 32 * 1024 * 1024;       // 32 MB
  
  // "hbase.regionserver.hlog.replication"
  public static String LogReplication = "harmonious.regionserver.hlog.replication"; 
  public static int DefaultLogReplication = 3;
   
  // "hbase.regionserver.optionallogflushinterval"
  public static String OptimalLogFlushInterval = "harmonious.regionserver.optionallogflushinterval";   
  public static int DefaultOptimalLogFlushInterval = 1 * 1000;    //in milliseconds
  
  // "hbase.regionserver.hlog.writer.impl"
//  public static String LogWriterClass = "harmonious.regionserver.hlog.writer.impl";
//  public static Class<? extends LogFile.Writer> DefaultLogWriterClass = HSequenceFileLogWriter.class;
  
  // "hbase.regionserver.hlog.reader.impl"
//  public static String LogReaderClass = "hbase.regionserver.hlog.reader.impl";
//  public static Class<? extends LogFile.Reader> DefaultLogReaderClass = HSequenceFileLogReader.class;
  
  // "hbase.hregion.max.filesize"
  public static String MaxFileSize = "harmonious.hregion.max.filesize";
  public static int DefaultMaxFileSize = 256 * 1024 * 1024;
  
  // "hbase.hstore.blockingStoreFiles"
  public static String BlockingStoreFiles = "harmonious.hstore.blockingStoreFiles";
  public static int DefaultBlockingStoreFileCount = 7;          //TODO: currently this follows hbase setting. maybe there are a better magic number.
  
  // "hbase.hstore.compaction.max";
  public static String MaxCompaction = "harmonious.hstore.compaction.max";
  public static int DefaultMaxCompactionCount = 10;
  
  // "hbase.hstore.compaction.min.size",
  public static String MinCompactSize = "harmonious.hstore.compaction.min.size";
  public static int DefaultMinCompactSize = 1024*1024*64;
  
  // "hbase.hregion.memstore.flush.size"
  public static String MemIndexFlushSize = "harmonious.hregion.memstore.flush.size";
  public static int DefaultMemIndexFlushSize = 1024*1024*64;
  
  // "hbase.hstore.compaction.ratio"
  public static String CompactionRatio = "hbase.hstore.compaction.ratio";   //TODO: not sure what's it used for in our system.
  public static float DefaultCompactionRatio = 1.2F;
  
  // "hbase.hstore.compactionThreshold"
  public static String CompactionThreshold = "hbase.hstore.compactionThreshold";
  public static int DefaultCompactionThreshold = 3;
}
