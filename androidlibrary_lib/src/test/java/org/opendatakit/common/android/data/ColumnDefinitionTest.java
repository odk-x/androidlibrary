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
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.common.android.utilities.StaticStateManipulator;
import org.opendatakit.common.android.utilities.WebLogger;
import org.opendatakit.common.desktop.WebLoggerDesktopFactoryImpl;

import java.util.ArrayList;
import java.util.List;

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
}
