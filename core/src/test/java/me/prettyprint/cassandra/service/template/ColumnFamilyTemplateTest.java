package me.prettyprint.cassandra.service.template;

import static org.junit.Assert.*;

import java.util.Arrays;

import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.hector.api.factory.HFactory;

import org.junit.Test;

public class ColumnFamilyTemplateTest extends BaseColumnFamilyTemplateTest {

  @Test
  public void testCreateSelect() {
    ColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(keyspace, "Standard1", se, se, HFactory.createMutator(keyspace, se));

    ColumnFamilyUpdater updater = template.createUpdater("key1");
    updater.setString("column1","value1");
    template.update(updater);

    template.addColumn("column1", se);
    ColumnFamilyResult wrapper = template.queryColumns("key1");
    assertEquals("value1",wrapper.getString("column1"));

  }

  @Test
  public void testCreateSelectTemplate() {
    ColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(keyspace, "Standard1", se, se, HFactory.createMutator(keyspace, se));
    ColumnFamilyUpdater updater = template.createUpdater("key1");
    updater.setString("column1","value1");
    updater.update();
    template.setCount(10);
    String value = template.queryColumns("key1", new ColumnFamilyRowMapper<String, String, String>() {
      @Override
      public String mapRow(ColumnFamilyResult<String, String> results) {
        // TODO Auto-generated method stub
        return results.getString("column1");
      }
    });
    assertEquals("value1",value);
  }

  @Test
  public void testQueryMultiget() {
    ColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(keyspace, "Standard1", se, se, HFactory.createMutator(keyspace, se));
    ColumnFamilyUpdater updater = template.createUpdater("mg_key1");
    updater.setString("column1","value1");
    updater.addKey("mg_key2");
    updater.setString("column1","value2");
    updater.addKey("mg_key3");
    updater.setString("column1","value3");
    template.update(updater);

    template.addColumn("column1", se);
    ColumnFamilyResult wrapper = template.queryColumns(Arrays.asList("mg_key1", "mg_key2", "mg_key3"));
    assertEquals("value1",wrapper.getString("column1"));
    wrapper.next();
    assertEquals("value2",wrapper.getString("column1"));
    wrapper.next();
    assertEquals("value3",wrapper.getString("column1"));
  }

  @Test
  public void testHasNoResults() {
    ColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(keyspace, "Standard1", se, se, HFactory.createMutator(keyspace, se));
    assertFalse(template.queryColumns("noresults").hasResults());

  }

}
