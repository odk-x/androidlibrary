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

package org.opendatakit.database.data;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.utilities.ODKFileUtils;
import org.opendatakit.utilities.StaticStateManipulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ColumnDefinitionTest {

   @BeforeClass
   public static void oneTimeSetUp() throws Exception {
      StaticStateManipulator.get().reset();
      WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildAppName1() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions(null,
          "tableName", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildAppName2() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("",
          "tableName", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildTableId1() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          null, columns);
   }


   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildTableId2() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList1() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList2() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      columns.add(new Column("colb","colb","integer", "a"));
      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList3() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      columns.add(new Column("colb", "colb", "array", "[\"colb_items\"]"));
      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList4() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      columns.add(new Column("colb", "colb", "object", "[\"colb_elem\"]"));
      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList5() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      columns.add(new Column("colb", "colb", "array", "[\"colb_items\",\"colb_alt\"]"));
      columns.add(new Column("colb_alt", "alt", "string", "[]"));
      columns.add(new Column("colb_items", "items", "string", "[]"));
      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }


   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList6() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("colb", "colb", "array", "[\"colb_items\",\"cob_alt\"]"));
      columns.add(new Column("cob_alt", "alt", "string", "[]"));
      columns.add(new Column("colb_items", "items", "string", "[]"));
      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList7() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }


   @Test(expected = IllegalArgumentException.class)
   public void testBadBuildColumnList8() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      columns.add(new Column("col8a", "col8a", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"cola_items_accuracy\",\"cola_items_altitude\",\"cola_items_latitude\",\"cola_items_longitude\"]"));
      columns.add(new Column("cola_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("cola_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("cola_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("cola_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }

   @Test
   public void testBuildColumnList() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(500)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"col1a_items_accuracy\",\"col1a_items_altitude\",\"col1a_items_latitude\","
              + "\"col1a_items_longitude\"]"));
      columns.add(new Column("col1a_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col1a_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col1a_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col1a_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testBuildExtendedJson() {
      List<Column> columns = new ArrayList<Column>();

      // arbitrary type derived from integer
      columns.add(new Column("col0", "col0", "myothertype:integer", "[]"));
      // primitive types
      columns.add(new Column("col1", "col1", "boolean", "[]"));
      columns.add(new Column("col2", "col2", "integer", "[]"));
      columns.add(new Column("col3", "col3", "number", "[]"));
      columns.add(new Column("col4", "col4", "string", "[]"));
      // string with 500 varchars allocated to it
      columns.add(new Column("col5", "col5", "string(500)", "[]"));
      columns.add(new Column("col6", "col6", "configpath", "[]"));
      columns.add(new Column("col7", "col7", "rowpath", "[]"));
      // object type (geopoint)
      columns.add(new Column("col8", "col8", "geopoint",
          "[\"col8_accuracy\",\"col8_altitude\",\"col8_latitude\",\"col8_longitude\"]"));
      columns.add(new Column("col8_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col8_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col8_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col8_longitude", "longitude", "number", "[]"));
      // arbitrary type derived from string
      columns.add(new Column("col9", "col9", "mytype", "[]"));

      // arrays
      columns.add(new Column("col12", "col12", "array", "[\"col12_items\"]"));
      columns.add(new Column("col12_items", "items", "integer", "[]"));
      // array with 500 varchars allocated to it
      columns.add(new Column("col14", "col14", "array(400)", "[\"col14_items\"]"));
      columns.add(new Column("col14_items", "items", "string", "[]"));

      columns.add(new Column("col1a", "col1a", "geolist:array(500)", "[\"col1a_items\"]"));
      columns.add(new Column("col1a_items", "items", "geopoint",
          "[\"col1a_items_accuracy\",\"col1a_items_altitude\",\"col1a_items_latitude\","
              + "\"col1a_items_longitude\"]"));
      columns.add(new Column("col1a_items_accuracy", "accuracy", "number", "[]"));
      columns.add(new Column("col1a_items_altitude", "altitude", "number", "[]"));
      columns.add(new Column("col1a_items_latitude", "latitude", "number", "[]"));
      columns.add(new Column("col1a_items_longitude", "longitude", "number", "[]"));

      ArrayList<ColumnDefinition> colDefs = ColumnDefinition.buildColumnDefinitions("appName",
          "testTable", columns);

      String equivalentXLSXConverterDataTableModel =
          "{" +
              "\"col0\": {" +
              "\"type\": \"integer\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 2," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"myothertype:integer\"," +
              "\"elementKey\": \"col0\"," +
              "\"elementName\": \"col0\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col0\"" +
              "}," +
              "\"col1\": {" +
              "\"type\": \"boolean\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 3," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementKey\": \"col1\"," +
              "\"elementName\": \"col1\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1\"" +
              "}," +
              "\"col2\": {" +
              "\"type\": \"integer\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 4," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementKey\": \"col2\"," +
              "\"elementName\": \"col2\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col2\"" +
              "}," +
              "\"col3\": {" +
              "\"type\": \"number\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 5," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementKey\": \"col3\"," +
              "\"elementName\": \"col3\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col3\"" +
              "}," +
              "\"col4\": {" +
              "\"type\": \"string\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 6," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementKey\": \"col4\"," +
              "\"elementName\": \"col4\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col4\"" +
              "}," +
              "\"col5\": {" +
              "\"type\": \"string\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 7," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"string(500)\"," +
              "\"elementKey\": \"col5\"," +
              "\"elementName\": \"col5\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col5\"" +
              "}," +
              "\"col6\": {" +
              "\"type\": \"string\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 8," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"configpath\"," +
              "\"elementKey\": \"col6\"," +
              "\"elementName\": \"col6\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col6\"" +
              "}," +
              "\"col7\": {" +
              "\"type\": \"string\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 9," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"rowpath\"," +
              "\"elementKey\": \"col7\"," +
              "\"elementName\": \"col7\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col7\"" +
              "}," +
              "\"col8\": {" +
              "\"type\": \"object\"," +
              "\"elementType\": \"geopoint\"," +
              "\"properties\": {" +
              "\"latitude\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_latitude\"," +
              "\"elementName\": \"latitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.latitude\"" +
              "}," +
              "\"longitude\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_longitude\"," +
              "\"elementName\": \"longitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.longitude\"" +
              "}," +
              "\"altitude\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_altitude\"," +
              "\"elementName\": \"altitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.altitude\"" +
              "}," +
              "\"accuracy\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_accuracy\"," +
              "\"elementName\": \"accuracy\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.accuracy\"" +
              "}" +
              "}," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 10," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementKey\": \"col8\"," +
              "\"elementName\": \"col8\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8\"," +
              "\"listChildElementKeys\": [" +
              "\"col8_accuracy\"," +
              "\"col8_altitude\"," +
              "\"col8_latitude\"," +
              "\"col8_longitude\"" +
              "]," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col9\": {" +
              "\"type\": \"string\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 11," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"mytype\"," +
              "\"elementKey\": \"col9\"," +
              "\"elementName\": \"col9\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col9\"" +
              "}," +
              "\"col12\": {" +
              "\"type\": \"array\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 12," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"items\": {" +
              "\"type\": \"integer\"," +
              "\"elementKey\": \"col12_items\"," +
              "\"elementName\": \"items\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col12.items\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"elementKey\": \"col12\"," +
              "\"elementName\": \"col12\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col12\"," +
              "\"listChildElementKeys\": [" +
              "\"col12_items\"" +
              "]" +
              "}," +
              "\"col14\": {" +
              "\"type\": \"array\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 13," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"array(400)\"," +
              "\"items\": {" +
              "\"type\": \"string\"," +
              "\"elementKey\": \"col14_items\"," +
              "\"elementName\": \"items\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col14.items\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"elementKey\": \"col14\"," +
              "\"elementName\": \"col14\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col14\"," +
              "\"listChildElementKeys\": [" +
              "\"col14_items\"" +
              "]" +
              "}," +
              "\"col1a\": {" +
              "\"type\": \"array\"," +
              "\"_defn\": [" +
              "{" +
              "\"_row_num\": 14," +
              "\"section_name\": \"model\"" +
              "}" +
              "]," +
              "\"elementType\": \"geolist:array(500)\"," +
              "\"items\": {" +
              "\"type\": \"object\"," +
              "\"elementType\": \"geopoint\"," +
              "\"properties\": {" +
              "\"latitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"latitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.latitude\"," +
              "\"elementKey\": \"col1a_items_latitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"longitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"longitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.longitude\"," +
              "\"elementKey\": \"col1a_items_longitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"altitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"altitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.altitude\"," +
              "\"elementKey\": \"col1a_items_altitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"accuracy\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"accuracy\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.accuracy\"," +
              "\"elementKey\": \"col1a_items_accuracy\"," +
              "\"notUnitOfRetention\": true" +
              "}" +
              "}," +
              "\"elementKey\": \"col1a_items\"," +
              "\"elementName\": \"items\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items\"," +
              "\"listChildElementKeys\": [" +
              "\"col1a_items_accuracy\"," +
              "\"col1a_items_altitude\"," +
              "\"col1a_items_latitude\"," +
              "\"col1a_items_longitude\"" +
              "]," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"elementKey\": \"col1a\"," +
              "\"elementName\": \"col1a\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a\"," +
              "\"listChildElementKeys\": [" +
              "\"col1a_items\"" +
              "]" +
              "}," +
              "\"col8_latitude\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_latitude\"," +
              "\"elementName\": \"latitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.latitude\"" +
              "}," +
              "\"col8_longitude\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_longitude\"," +
              "\"elementName\": \"longitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.longitude\"" +
              "}," +
              "\"col8_altitude\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_altitude\"," +
              "\"elementName\": \"altitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.altitude\"" +
              "}," +
              "\"col8_accuracy\": {" +
              "\"type\": \"number\"," +
              "\"elementKey\": \"col8_accuracy\"," +
              "\"elementName\": \"accuracy\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col8.accuracy\"" +
              "}," +
              "\"col12_items\": {" +
              "\"type\": \"integer\"," +
              "\"elementKey\": \"col12_items\"," +
              "\"elementName\": \"items\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col12.items\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col14_items\": {" +
              "\"type\": \"string\"," +
              "\"elementKey\": \"col14_items\"," +
              "\"elementName\": \"items\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col14.items\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col1a_items\": {" +
              "\"type\": \"object\"," +
              "\"elementType\": \"geopoint\"," +
              "\"properties\": {" +
              "\"latitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"latitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.latitude\"," +
              "\"elementKey\": \"col1a_items_latitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"longitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"longitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.longitude\"," +
              "\"elementKey\": \"col1a_items_longitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"altitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"altitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.altitude\"," +
              "\"elementKey\": \"col1a_items_altitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"accuracy\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"accuracy\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.accuracy\"," +
              "\"elementKey\": \"col1a_items_accuracy\"," +
              "\"notUnitOfRetention\": true" +
              "}" +
              "}," +
              "\"elementKey\": \"col1a_items\"," +
              "\"elementName\": \"items\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items\"," +
              "\"listChildElementKeys\": [" +
              "\"col1a_items_accuracy\"," +
              "\"col1a_items_altitude\"," +
              "\"col1a_items_latitude\"," +
              "\"col1a_items_longitude\"" +
              "]," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col1a_items_latitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"latitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.latitude\"," +
              "\"elementKey\": \"col1a_items_latitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col1a_items_longitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"longitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.longitude\"," +
              "\"elementKey\": \"col1a_items_longitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col1a_items_altitude\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"altitude\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.altitude\"," +
              "\"elementKey\": \"col1a_items_altitude\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"col1a_items_accuracy\": {" +
              "\"type\": \"number\"," +
              "\"elementName\": \"accuracy\"," +
              "\"elementSet\": \"data\"," +
              "\"elementPath\": \"col1a.items.accuracy\"," +
              "\"elementKey\": \"col1a_items_accuracy\"," +
              "\"notUnitOfRetention\": true" +
              "}," +
              "\"_id\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": true," +
              "\"elementKey\": \"_id\"," +
              "\"elementName\": \"_id\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_id\"" +
              "}," +
              "\"_row_etag\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_row_etag\"," +
              "\"elementName\": \"_row_etag\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_row_etag\"" +
              "}," +
              "\"_sync_state\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": true," +
              "\"elementKey\": \"_sync_state\"," +
              "\"elementName\": \"_sync_state\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_sync_state\"" +
              "}," +
              "\"_conflict_type\": {" +
              "\"type\": \"integer\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_conflict_type\"," +
              "\"elementName\": \"_conflict_type\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_conflict_type\"" +
              "}," +
              "\"_default_access\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_default_access\"," +
              "\"elementName\": \"_default_access\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_default_access\"" +
              "}," +
              "\"_row_owner\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_row_owner\"," +
              "\"elementName\": \"_row_owner\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_row_owner\"" +
              "}," +
              "\"_group_read_only\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_group_read_only\"," +
              "\"elementName\": \"_group_read_only\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_group_read_only\"" +
              "}," +
              "\"_group_modify\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_group_modify\"," +
              "\"elementName\": \"_group_modify\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_group_modify\"" +
              "}," +
              "\"_group_privileged\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_group_privileged\"," +
              "\"elementName\": \"_group_privileged\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_group_privileged\"" +
              "}," +
              "\"_form_id\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_form_id\"," +
              "\"elementName\": \"_form_id\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_form_id\"" +
              "}," +
              "\"_locale\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_locale\"," +
              "\"elementName\": \"_locale\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_locale\"" +
              "}," +
              "\"_savepoint_type\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_savepoint_type\"," +
              "\"elementName\": \"_savepoint_type\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_savepoint_type\"" +
              "}," +
              "\"_savepoint_timestamp\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": true," +
              "\"elementKey\": \"_savepoint_timestamp\"," +
              "\"elementName\": \"_savepoint_timestamp\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_savepoint_timestamp\"" +
              "}," +
              "\"_savepoint_creator\": {" +
              "\"type\": \"string\"," +
              "\"isNotNullable\": false," +
              "\"elementKey\": \"_savepoint_creator\"," +
              "\"elementName\": \"_savepoint_creator\"," +
              "\"elementSet\": \"instanceMetadata\"," +
              "\"elementPath\": \"_savepoint_creator\"" +
              "}" +
              "}";
      int metaCount = 0;
      TreeMap<String, Object> model = ColumnDefinition.getExtendedDataModel(colDefs);
      TypeReference<TreeMap<String,Object>> refType = new TypeReference<TreeMap<String,Object>>(){};
      TreeMap<String, Object> xlsxModel;
      try {
         xlsxModel = ODKFileUtils.mapper.readValue
             (equivalentXLSXConverterDataTableModel, refType);
      } catch (IOException e) {
         assertFalse("failed to parse XLSXConverter version", true);
         return;
      }

      for (String elementKey : model.keySet()) {
         TreeMap<String,Object> value = (TreeMap<String, Object>) model.get(elementKey);
         assert(xlsxModel.containsKey(elementKey));
         Map<String,Object> xlsxValue = (Map<String,Object>) xlsxModel.get(elementKey);
         List<String> ignoredKeys = new ArrayList<String>();
         for ( String key : xlsxValue.keySet() ) {
            if ( key.startsWith("_") ) {
               ignoredKeys.add(key);
            }
            if ( key.equals("prompt_type_name") ) {
               ignoredKeys.add(key);
            }
         }
         for ( String key : ignoredKeys ) {
            xlsxValue.remove(key);
         }
         assertEquals("Investigating: " + elementKey, value.size(), xlsxValue.size());
         recursiveMatch(elementKey, value, xlsxValue);

         ColumnDefinition def = null;
         try {
            def = ColumnDefinition.find(colDefs, elementKey);
         } catch (IllegalArgumentException e) {
            // ignore
         }
         if ( def == null ) {
            assertEquals(value.get("elementSet"), "instanceMetadata");
            assertEquals(value.get("elementKey"), elementKey);
            assertEquals(value.get("elementName"), elementKey);
            assertEquals(value.get("elementPath"), elementKey);
            assertEquals(value.containsKey("notUnitOfRetention"), false);

            if ( elementKey.equals(TableConstants.ID) ) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), true);
            } else if ( elementKey.equals(TableConstants.ROW_ETAG)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SYNC_STATE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), true);
            } else if ( elementKey.equals(TableConstants.CONFLICT_TYPE)) {
               metaCount++;
               assertEquals(value.get("type"), "integer");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.DEFAULT_ACCESS)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.ROW_OWNER)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.GROUP_READ_ONLY)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.GROUP_MODIFY)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.GROUP_PRIVILEGED)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SAVEPOINT_TIMESTAMP)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), true);
            } else if ( elementKey.equals(TableConstants.SAVEPOINT_TYPE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SAVEPOINT_CREATOR)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.FORM_ID)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.LOCALE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), false);
            } else {
               throw new IllegalStateException("Unexpected non-user column");
            }
         } else {
            assertEquals(value.get("elementPath"), def.getElementName());
            assertEquals(value.get("elementSet"), "data");
            assertEquals(value.get("elementName"), def.getElementName());
            assertEquals(value.get("elementKey"), def.getElementKey());
            if (!def.isUnitOfRetention()) {
               assertEquals(value.get("notUnitOfRetention"), true);
            }
            // TODO: there are a lot more paths to verify here...
            assertEquals(value.containsKey("isNotNullable"), false);
         }
      }
      assertEquals(metaCount, 14);
   }

   @SuppressWarnings("unchecked")
   private void recursiveMatch(String parent, TreeMap<String,Object> value, Map<String,Object>
       xlsxValue) {
      for ( String key : value.keySet() ) {
         assertTrue("Investigating " + parent + "." + key, xlsxValue.containsKey(key));
         Object ov = value.get(key);
         Object oxlsxv = xlsxValue.get(key);
         if ( ov instanceof Map ) {
            TreeMap<String,Object> rv = (TreeMap<String,Object>) ov;
            Map<String,Object> xlsrv = (Map<String,Object>) oxlsxv;
            List<String> ignoredKeys = new ArrayList<String>();
            for ( String rvkey : xlsrv.keySet() ) {
               if ( rvkey.startsWith("_") ) {
                  ignoredKeys.add(rvkey);
               }
               if ( rvkey.equals("prompt_type_name") ) {
                  ignoredKeys.add(rvkey);
               }
            }
            for ( String rvkey : ignoredKeys ) {
               xlsrv.remove(rvkey);
            }
            assertEquals("Investigating " + parent + "." + key, rv.size(), xlsrv.size());
            recursiveMatch(parent + "." + key, rv, xlsrv);

         } else {
            assertEquals("Investigating " + parent + "." + key, ov, oxlsxv);
         }
      }
   }
}
