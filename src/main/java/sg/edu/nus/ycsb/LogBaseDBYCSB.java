package sg.edu.nus.ycsb;

import com.yahoo.ycsb.ByteArrayByteIterator;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import sg.edu.nus.LogBaseAPI.LogAdmin;
import sg.edu.nus.LogBaseAPI.LogTable;

import java.io.IOException;
import java.util.*;

import static com.yahoo.ycsb.workloads.CoreWorkload.TABLENAME_PROPERTY;

public class LogBaseDBYCSB extends com.yahoo.ycsb.DB {

    private static final String DEFAULT_TIMEOUT = "120000";
    private static final String DEFAULT_HBASE_MASTER = "x.x.x.x:60000";
    private static final String DEFAULT_ZK_QUORUM = "x.x.x.x";
    private static final String DEFAULT_ZK_PORT = "2282";

    private LogTable logTable;
    private String tableName;
    private boolean isDebug = false;
    private boolean checkResult = false;

    public void init() throws com.yahoo.ycsb.DBException {

        String timeout = getProperties().getProperty("timeout", DEFAULT_TIMEOUT);
        String hbaseMaster = getProperties().getProperty("hbase.master", DEFAULT_HBASE_MASTER);
        String zkQuorum = getProperties().getProperty("zk.quorum", DEFAULT_ZK_QUORUM);
        String zkPort = getProperties().getProperty("zk.port", DEFAULT_ZK_PORT);
        isDebug = Boolean.valueOf(getProperties().getProperty("debug", "false"));
        checkResult = Boolean.valueOf(getProperties().getProperty("checkResult", "false"));
        tableName = getProperties().getProperty(TABLENAME_PROPERTY, "t1");

        Configuration hBaseConfig = HBaseConfiguration.create();
        hBaseConfig.setInt("timeout", Integer.valueOf(timeout));
        hBaseConfig.set("hbase.master", hbaseMaster);
        hBaseConfig.set("hbase.zookeeper.quorum", zkQuorum);
        hBaseConfig.set("hbase.zookeeper.property.clientPort", zkPort);

        LogAdmin admin = null;
        try {
            admin = new LogAdmin(hBaseConfig);
            logTable = admin.getExistingTable(tableName);
//            if (!logTable.blocked) {
//                String errMsg = "cannot found table in logbase : " + tableName;
//                System.out.println(errMsg);
//                throw new Exception(errMsg);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    @Override
    public Status read(String table, String key, Set<String> set, Map<String, ByteIterator> resultMap) {

        if (!tableName.equals(table)) {
            throw new RuntimeException("a new table issued in read method, table: " + table);
        }

        if (isDebug) {
            System.out.println("read key: " + key);
        }

        long startTime = 0;
        byte[] row = Bytes.toBytes(key); //row name
        try {
            if (isDebug) {
                startTime = System.currentTimeMillis();
            }
            Result ret = logTable.get(row);
            if (isDebug) {
                System.out.println("get duration: " + (System.currentTimeMillis() - startTime) + "ms");
                startTime = System.currentTimeMillis();
            }
            if (checkResult) {
                if (ret.isEmpty()) {
                    if (isDebug) {
                        System.out.println("ret.isEmpty 1: " + (System.currentTimeMillis() - startTime) + "ms");
                    }
                    return Status.NOT_FOUND;
                } else {
                    if (isDebug) {
                        System.out.println("ret.not empty 2: " + (System.currentTimeMillis() - startTime) + "ms");
                    }
//                startTime = System.currentTimeMillis();
//                for(int i=0; i<ret.size(); i++){
//                    resultMap.put(ret.raw()[i].getKeyString(), new ByteArrayByteIterator(ret.raw()[i].getValue()));
//                }
//                System.out.println("resultMap duration: " + (System.currentTimeMillis() - startTime) + "ms");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return Status.ERROR;
        }

        return Status.OK;
    }

    @Override
    public Status update(String table, String key, Map<String, ByteIterator> values) {

        if (!tableName.equals(table)) {
            throw new RuntimeException("a new table issued in update method, table: " + table);
        }

        int size = values.size();
        byte[][] cols = new byte[size][]; //columns’ names
        byte[][] value = new byte[size][]; //values

        if (isDebug) {
            System.out.println("update key: " + key + "; cols size: " + size);
        }

        int i = 0;
        for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
            cols[i] = Bytes.toBytes(entry.getKey());
            value[i] = entry.getValue().toArray();
            if (isDebug) {
                System.out.println("i: " + i + "; cols size: " + cols[i].length + "; value len: " + value[i].length);
            }
            i++;
        }
        byte[] row = Bytes.toBytes(key); //row name

        try {
            logTable.put(row, cols, value);
        } catch (IOException e) {
            e.printStackTrace();
            return Status.ERROR;
        }

        return Status.OK;
    }

    @Override
    public Status insert(String table, String key, Map<String, ByteIterator> values) {
        if (!tableName.equals(table)) {
            throw new RuntimeException("a new table issued in insert method, table: " + table);
        }
        return update(table, key, values);
    }

    @Override
    public Status delete(String table, String key) {
        if (!tableName.equals(table)) {
            throw new RuntimeException("a new table issued in delete method, table: " + table);
        }
        try {
            logTable.deleteIndex(Bytes.toBytes(key));
        } catch (IOException e) {
            e.printStackTrace();
            return Status.ERROR;
        }
        return Status.OK;
    }

    @Override
    public Status scan(String table, String startkey, int recordcount, Set<String> set, Vector<HashMap<String, ByteIterator>> vector) {

        if (!tableName.equals(table)) {
            throw new RuntimeException("a new table issued in scan method, table: " + table);
        }

        byte[] row = Bytes.toBytes(startkey); //row name
        try {

            Iterable<Result> resultIterable = logTable.tableScan(row, recordcount);
            Iterator<Result> itor = resultIterable.iterator();
            while (itor.hasNext()) {
                Result ret = itor.next();
                if (ret.isEmpty()) {
                    return Status.NOT_FOUND;
                } else {
                    HashMap<String, ByteIterator> map = new HashMap<String, ByteIterator>();
                    for (int i = 0; i < ret.size(); i++) {
                        map.put(ret.raw()[i].getKeyString(), new ByteArrayByteIterator(ret.raw()[i].getValue()));
                    }
                    vector.add(map);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Status.ERROR;
        }

        return Status.OK;
    }
}
