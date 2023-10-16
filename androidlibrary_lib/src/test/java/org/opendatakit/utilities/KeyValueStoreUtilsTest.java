/*
 * Copyright (C) 2015 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.utilities.KeyValueStoreUtils;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class KeyValueStoreUtilsTest {

   public static final String APP_NAME = "keyValueTests";

   public static final String MY_TABLE = "myTable";
   public static final String MY_TABLE2 = "myTable2";
   public static final String MY_PARTITION = "myPartition";
   public static final String MY_PARTITION2 = "myPartition2";
   public static final String MY_ASPECT = "myAspect";
   public static final String MY_ASPECT2 = "myAspect2";
   public static final String MY_KEY = "myKey";
   public static final String MY_KEY2 = "myKey2";
   public static final String VALUE = "aString";
   public static final String INT_VALUE = "-1130";
   public static final String DBL_VALUE = "9.39";

   @BeforeClass
  public static void oneTimeSetUp() {
     StaticStateManipulator.get().reset();
     WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

   @Test
   public void testKeyValueStoreString() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.string, VALUE);

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.string, ElementDataType.valueOf(e.type));
      assertEquals(VALUE, e.value);

      assertEquals(VALUE, KeyValueStoreUtils.getString(e));
   }

   @Test
   public void testKeyValueStoreInteger() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.integer, INT_VALUE);

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.integer, ElementDataType.valueOf(e.type));
      assertEquals(INT_VALUE, e.value);

      Integer value = -1130;
      assertEquals(value, KeyValueStoreUtils.getInteger(e));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreIntegerBad() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.string, INT_VALUE);

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.string, ElementDataType.valueOf(e.type));
      assertEquals(INT_VALUE, e.value);

      Integer value = -1130;
      assertEquals(value, KeyValueStoreUtils.getInteger(e));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreIntegerBad2() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.integer, VALUE);

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.integer, ElementDataType.valueOf(e.type));
      assertEquals(VALUE, e.value);

      Integer value = -1130;
      assertEquals(value, KeyValueStoreUtils.getInteger(e));
   }

   @Test
   public void testKeyValueStoreBoolean() throws IllegalArgumentException {
      // booleans are stored as 0 or 1 in the KVS array
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.bool, "1");

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.bool, ElementDataType.valueOf(e.type));
      assertEquals("1", e.value);
      assertEquals(Boolean.TRUE, KeyValueStoreUtils.getBoolean(e));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreBooleanBad() throws IllegalArgumentException {
      // booleans are stored as 0 or 1 in the KVS array
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.array, "1");

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.array, ElementDataType.valueOf(e.type));
      assertEquals("1", e.value);
      assertEquals(Boolean.TRUE, KeyValueStoreUtils.getBoolean(e));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreBooleanBad2() throws IllegalArgumentException {
      // booleans are stored as 0 or 1 in the KVS array
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.bool, VALUE);

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.bool, ElementDataType.valueOf(e.type));
      assertEquals(VALUE, e.value);
      assertEquals(Boolean.TRUE, KeyValueStoreUtils.getBoolean(e));
   }

   @Test
   public void testKeyValueStoreNumber() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.number, DBL_VALUE);

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.number, ElementDataType.valueOf(e.type));
      assertEquals(DBL_VALUE, e.value);

      Double value = 9.39;
      assertEquals(value, KeyValueStoreUtils.getNumber(e));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreNumberBad() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.array, DBL_VALUE);

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.array, ElementDataType.valueOf(e.type));
      assertEquals(DBL_VALUE, e.value);

      Double value = 9.39;
      assertEquals(value, KeyValueStoreUtils.getNumber(e));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreNumberBad2() throws IllegalArgumentException {
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.number, VALUE);

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.number, ElementDataType.valueOf(e.type));
      assertEquals(VALUE, e.value);

      Double value = 9.39;
      assertEquals(value, KeyValueStoreUtils.getNumber(e));
   }

   @Test
   public void testKeyValueStoreArray() throws IllegalArgumentException, JsonProcessingException {
      ArrayList<String> values = new ArrayList<String>();
      values.add("First");
      values.add("Second");
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.array, ODKFileUtils.mapper.writeValueAsString(values));

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.array, ElementDataType.valueOf(e.type));
      assertEquals(ODKFileUtils.mapper.writeValueAsString(values), e.value);

      ArrayList<String> returned = KeyValueStoreUtils.getArray(APP_NAME, e, String.class);

      assertEquals(2, returned.size());
      assertEquals(values.get(0), returned.get(0));
      assertEquals(values.get(1), returned.get(1));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreArrayBad() throws IllegalArgumentException, JsonProcessingException {
      ArrayList<String> values = new ArrayList<String>();
      values.add("First");
      values.add("Second");
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.string, ODKFileUtils.mapper.writeValueAsString(values));

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.string, ElementDataType.valueOf(e.type));
      assertEquals(ODKFileUtils.mapper.writeValueAsString(values), e.value);

      ArrayList<String> returned = KeyValueStoreUtils.getArray(APP_NAME, e, String.class);

      assertEquals(2, returned.size());
      assertEquals(values.get(0), returned.get(0));
      assertEquals(values.get(1), returned.get(1));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreArrayBad2() throws IllegalArgumentException {
      ArrayList<String> values = new ArrayList<String>();
      values.add("First");
      values.add("Second");
      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE, MY_PARTITION, MY_ASPECT,
          MY_KEY, ElementDataType.array, VALUE);

      assertEquals(MY_TABLE, e.tableId);
      assertEquals(MY_PARTITION, e.partition);
      assertEquals(MY_ASPECT, e.aspect);
      assertEquals(MY_KEY, e.key);
      assertEquals(ElementDataType.array, ElementDataType.valueOf(e.type));
      assertEquals(VALUE, e.value);

      ArrayList<String> returned = KeyValueStoreUtils.getArray(APP_NAME, e, String.class);

      assertEquals(2, returned.size());
      assertEquals(values.get(0), returned.get(0));
      assertEquals(values.get(1), returned.get(1));
   }

   @Test
   public void testKeyValueStoreObject() throws IllegalArgumentException, JsonProcessingException {
      ArrayList<String> values = new ArrayList<String>();
      values.add("a");
      values.add("b");
      Map<String,Object> myMap = new TreeMap<String,Object>();
      myMap.put("first", values);
      myMap.put("Second", 34);
      myMap.put("third", 33.32);

      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.object, ODKFileUtils.mapper.writeValueAsString(myMap));

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.object, ElementDataType.valueOf(e.type));
      assertEquals(ODKFileUtils.mapper.writeValueAsString(myMap), e.value);

      String returned = KeyValueStoreUtils.getObject(e);
      assertEquals(ODKFileUtils.mapper.writeValueAsString(myMap), e.value);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testKeyValueStoreObjectBad() throws IllegalArgumentException, JsonProcessingException {
      ArrayList<String> values = new ArrayList<String>();
      values.add("a");
      values.add("b");
      Map<String,Object> myMap = new TreeMap<String,Object>();
      myMap.put("first", values);
      myMap.put("Second", 34);
      myMap.put("third", 33.32);

      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.integer, ODKFileUtils.mapper.writeValueAsString(myMap));

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.integer, ElementDataType.valueOf(e.type));
      assertEquals(ODKFileUtils.mapper.writeValueAsString(myMap), e.value);

      String returned = KeyValueStoreUtils.getObject(e);
      assertEquals(ODKFileUtils.mapper.writeValueAsString(myMap), e.value);
   }

   @Test
   public void testKeyValueStoreObjectGood() throws IllegalArgumentException,
       JsonProcessingException {
      ArrayList<String> values = new ArrayList<String>();
      values.add("a");
      values.add("b");

      KeyValueStoreEntry e = KeyValueStoreUtils.buildEntry(MY_TABLE2, MY_PARTITION2, MY_ASPECT2,
          MY_KEY2, ElementDataType.array, ODKFileUtils.mapper.writeValueAsString(values));

      assertEquals(MY_TABLE2, e.tableId);
      assertEquals(MY_PARTITION2, e.partition);
      assertEquals(MY_ASPECT2, e.aspect);
      assertEquals(MY_KEY2, e.key);
      assertEquals(ElementDataType.array, ElementDataType.valueOf(e.type));
      assertEquals(ODKFileUtils.mapper.writeValueAsString(values), e.value);

      String returned = KeyValueStoreUtils.getObject(e);
      assertEquals(ODKFileUtils.mapper.writeValueAsString(values), e.value);
   }
}
