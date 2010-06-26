package me.prettyprint.cassandra.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.model.ColumnQuery;
import me.prettyprint.cassandra.model.HColumn;
import me.prettyprint.cassandra.model.HectorException;
import me.prettyprint.cassandra.model.KeyspaceOperator;
import me.prettyprint.cassandra.model.KeyspaceOperatorFactory;
import me.prettyprint.cassandra.model.MultigetSliceQuery;
import me.prettyprint.cassandra.model.Mutator;
import me.prettyprint.cassandra.model.MutatorFactory;
import me.prettyprint.cassandra.model.QueryFactory;
import me.prettyprint.cassandra.model.Result;
import me.prettyprint.cassandra.model.Rows;
import me.prettyprint.cassandra.model.StringExtractor;
import me.prettyprint.cassandra.service.Cluster;
import me.prettyprint.cassandra.service.ClusterFactory;

public class ExampleDaoV2 {

  private final static String KEYSPACE = "Keyspace1";
  private final static String HOST_PORT = "localhost:9170";
  private final static String CF_NAME = "Standard1";
  /** Column name where values are stored */
  private final static String COLUMN_NAME = "v";
  private StringExtractor extractor = StringExtractor.get();

  private final KeyspaceOperator keyspaceOperator;
  
  public static void main(String[] args) throws HectorException {
    Cluster c = ClusterFactory.getOrCreate("MyCluster", HOST_PORT);
    ExampleDaoV2 ed = new ExampleDaoV2(KeyspaceOperatorFactory.create(KEYSPACE, c));
    ed.insert("key1", "value1");

    System.out.println(ed.get("key1"));
  }

  public ExampleDaoV2(KeyspaceOperator ko) {
    keyspaceOperator = ko;
  }
  
  /**
   * Insert a new value keyed by key
   *
   * @param key   Key for the value
   * @param value the String value to insert
   */
  public void insert(final String key, final String value) {
    Mutator m = MutatorFactory.createMutator(keyspaceOperator);
    m.insert(key, CF_NAME, m.createColumn(COLUMN_NAME, value, extractor, extractor));
  }

  /**
   * Get a string value.
   *
   * @return The string value; null if no value exists for the given key.
   */
  public String get(final String key) throws HectorException {
    ColumnQuery<String, String> q = 
        QueryFactory.createColumnQuery(keyspaceOperator, extractor, extractor);
    Result<HColumn<String, String>> r = q.setKey(key).
        setName(COLUMN_NAME).
        setColumnFamily(CF_NAME).
        execute();
    HColumn<String, String> c = r.get();
    return c.getValue();
  }

  /**
   * Delete a key from cassandra
   */
  public void delete(final String key) throws HectorException {
    Mutator m = MutatorFactory.createMutator(keyspaceOperator);
    m.delete(key, CF_NAME, COLUMN_NAME, extractor);
  }
  
  /**
   * Get multiple values
   * @param keys
   * @return
   */
  public Map<String, String> getMulti(Collection<String> keys) {
    MultigetSliceQuery<String,String,String> q = QueryFactory.createMultigetSliceQuery(keyspaceOperator);
    q.setColumnFamily(CF_NAME);
    q.setKeys(keys);
    q.setColumnNames(COLUMN_NAME);
    
    Result<Rows<String,String,String>> r = q.execute();
    Rows<String,String,String> rows = r.get();
    Map<String, String> ret = new HashMap<String, String>(keys.size());
    for (String k: keys) {
      HColumn<String,String> c = rows.get(k).getColumnSlice().getColumnByName(COLUMN_NAME);
      if (c != null && c.getValue() != null) {
        ret.put(k, c.getValue());
      }
    }
    return ret;
  }

  /**
   * Insert multiple values
   */
  public void insertMulti(Map<String, String> keyValues) {
    Mutator m = MutatorFactory.createMutator(keyspaceOperator);
    for (Map.Entry<String, String> keyValue: keyValues.entrySet()) {
      m.addInsertion(keyValue.getKey(), CF_NAME,  
          m.createColumn(COLUMN_NAME, keyValue.getValue(), extractor, extractor));
    }
    m.execute();
  }

  /**
   * Insert multiple values
   */
  public void deleteMulti(Collection<String> keys) {
    Mutator m = MutatorFactory.createMutator(keyspaceOperator);
    for (String key: keys) {
      m.addDeletion(key, CF_NAME,  COLUMN_NAME, extractor);
    }
    m.execute();
  }
}