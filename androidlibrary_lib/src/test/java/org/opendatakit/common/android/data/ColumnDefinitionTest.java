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

package org.opendatakit.common.android.data;

import android.graphics.Color;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.common.android.provider.DataTableColumns;
import org.opendatakit.common.android.utilities.StaticStateManipulator;
import org.opendatakit.common.android.utilities.WebLogger;
import org.opendatakit.common.desktop.WebLoggerDesktopFactoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

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

      int metaCount = 0;
      TreeMap<String, Object> model = ColumnDefinition.getExtendedDataModel(colDefs);
      for ( Map.Entry<String,Object> entry : model.entrySet()) {
         String elementKey = entry.getKey();
         TreeMap<String,Object> value = (TreeMap<String, Object>) entry.getValue();
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
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SYNC_STATE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.get("isNotNullable"), true);
            } else if ( elementKey.equals(TableConstants.CONFLICT_TYPE)) {
               metaCount++;
               assertEquals(value.get("type"), "integer");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.FILTER_TYPE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.FILTER_VALUE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SAVEPOINT_TIMESTAMP)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SAVEPOINT_TYPE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.SAVEPOINT_CREATOR)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.FORM_ID)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else if ( elementKey.equals(TableConstants.LOCALE)) {
               metaCount++;
               assertEquals(value.get("type"), "string");
               assertEquals(value.containsKey("isNotNullable"), false);
            } else {
               throw new IllegalStateException("Unexpected non-user column");
            }
         } else {
            assertEquals(value.get("elementPath"), def.getElementName());
            assertEquals(value.get("elementSet"), "data");
            assertEquals(value.get("elementName"), def.getElementName());
            assertEquals(value.get("elementKey"), def.getElementKey());
            if (!def.getElementKey().equals(def.getElementName()) && !def.isUnitOfRetention()) {
               assertEquals(value.get("notUnitOfRetention"), true);
            }
            // TODO: there are a lot more paths to verify here...
            assertEquals(value.containsKey("isNotNullable"), false);
         }
      }
      assertEquals(metaCount, 11);
   }
}
