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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class LogEntryOffset implements WritableComparable<LogEntryOffset>{
  int filenum;
  int offset;
  int size;
  long logSeqNum;
  long writeTime;
  
  public LogEntryOffset(){
  }

  public LogEntryOffset(int filenum, int size, int offset){
    this.filenum = filenum;
    this.offset = offset;
    this.size = size;
  }
  
  public void RecordLogSeqNumAndWriteTime(long s, long t){
	  this.logSeqNum =s;
	  this.writeTime =t;
  }
  
  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(filenum);
    out.writeInt(offset);
    out.writeInt(size);
  }
  @Override
  public void readFields(DataInput in) throws IOException {
    this.filenum = in.readInt();
    this.offset = in.readInt();
    this.size = in.readInt();
  }

  @Override
  public int compareTo(LogEntryOffset o) {
    // if the filenum and the offset is the same, 
    // these two log offsets are pointing to the same entry, and the size are thus the same
    // therefore, we don't need to compare the size;
    if(this.filenum != o.filenum) return this.filenum - o.filenum;
    return this.offset - o.offset;
  }

  public int getFileNum(){
    return filenum;
  }
  public int getOffset(){
    return offset;
  }
  public int getSize(){
    return size;
  }
  public String toString(){
    return "filenum: " + this.filenum + ", offset: " + this.offset + ", size: " + size; 
  }
  
  public boolean equals(Object obj){
  	if(obj instanceof LogEntryOffset){
  		return this.compareTo((LogEntryOffset)obj) == 0;
  	}
  	return false;
  }
}
