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

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.ElementType;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.SyncState;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.database.data.ColumnDefinition;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.Row;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.database.data.UserTable;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.provider.DataTableColumns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class UserTableTest {

  /*
                 * These are the columns that are present in any row in a user data table in
                 * the database. Each row should have these in addition to the user-defined
                 * columns. If you add a column here you have to be sure to also add it in
                 * the create table statement, which can't be programmatically created easily.
                 */
  private static final List<String> ADMIN_COLUMNS;

  /**
   * These are the columns that should be exported
   */
  private static final List<String> EXPORT_COLUMNS;

  static {
    ArrayList<String> adminColumns = new ArrayList<String>();
    adminColumns.add(DataTableColumns.ID);
    adminColumns.add(DataTableColumns.ROW_ETAG);
    adminColumns.add(DataTableColumns.SYNC_STATE); // not exportable
    adminColumns.add(DataTableColumns.CONFLICT_TYPE); // not exportable
    adminColumns.add(DataTableColumns.DEFAULT_ACCESS);
    adminColumns.add(DataTableColumns.ROW_OWNER);
    adminColumns.add(DataTableColumns.GROUP_READ_ONLY);
    adminColumns.add(DataTableColumns.GROUP_MODIFY);
    adminColumns.add(DataTableColumns.GROUP_PRIVILEGED);
    adminColumns.add(DataTableColumns.FORM_ID);
    adminColumns.add(DataTableColumns.LOCALE);
    adminColumns.add(DataTableColumns.SAVEPOINT_TYPE);
    adminColumns.add(DataTableColumns.SAVEPOINT_TIMESTAMP);
    adminColumns.add(DataTableColumns.SAVEPOINT_CREATOR);
    Collections.sort(adminColumns);
    ADMIN_COLUMNS = Collections.unmodifiableList(adminColumns);

    ArrayList<String> exportColumns = new ArrayList<String>();
    exportColumns.add(DataTableColumns.ID);
    exportColumns.add(DataTableColumns.ROW_ETAG);
    exportColumns.add(DataTableColumns.DEFAULT_ACCESS);
    exportColumns.add(DataTableColumns.ROW_OWNER);
    exportColumns.add(DataTableColumns.GROUP_READ_ONLY);
    exportColumns.add(DataTableColumns.GROUP_MODIFY);
    exportColumns.add(DataTableColumns.GROUP_PRIVILEGED);
    exportColumns.add(DataTableColumns.FORM_ID);
    exportColumns.add(DataTableColumns.LOCALE);
    exportColumns.add(DataTableColumns.SAVEPOINT_TYPE);
    exportColumns.add(DataTableColumns.SAVEPOINT_TIMESTAMP);
    exportColumns.add(DataTableColumns.SAVEPOINT_CREATOR);
    Collections.sort(exportColumns);
    EXPORT_COLUMNS = Collections.unmodifiableList(exportColumns);
  }

  private static final String APP_NAME = "userTableTest";
  private static final String TABLE_ID_1 = "myTableId_1";
  private static final String INSTANCE_ID_1 = "myInstanceId_1";
  private static final String INSTANCE_ID_2 = "myInstanceId_2";

  public static final String GEO_COL = "Geo_Col";

  public static final String GROUP_COL = "Group_Col";
  public static final String LIST_COL = "List_col";
  public static final String MY_COL = "My_col";
  public static final String THEIR_COL = "Their_col";
  public static final String YOUR_BOOLEAN_COL = "Your_boolean_col";
  public static final String YOUR_COL = "Your_col";
  public static final String YOUR_CONFIG_FILE_COL = "Your_config_file_col";
  public static final String YOUR_ROW_FILE_COL = "Your_row_file_col";

  @Before
  public void setUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testOrderedColumnsParcelation() throws IOException {

    List<String> geopointCells = new ArrayList<String>();
    geopointCells.add(GEO_COL + "_" + "accuracy");
    geopointCells.add(GEO_COL + "_" + "altitude");
    geopointCells.add(GEO_COL + "_" + "latitude");
    geopointCells.add(GEO_COL + "_" + "longitude");
    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column(GEO_COL, GEO_COL, ElementType.GEOPOINT,
        ODKFileUtils.mapper.writeValueAsString(geopointCells)));
    for ( String name : geopointCells ) {
      columns.add(new Column(name, name.substring(GEO_COL.length()+1),
          ElementDataType.number.name(), null));
    }
    columns.add(new Column(GROUP_COL, GROUP_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(MY_COL, MY_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(LIST_COL, LIST_COL, ElementDataType.array.name(),
        ODKFileUtils.mapper.writeValueAsString(Collections.singletonList(LIST_COL + "_items"))));
    columns.add(new Column(LIST_COL + "_items", "items", ElementDataType.integer.name(), null));
    columns.add(new Column(THEIR_COL, THEIR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_BOOLEAN_COL, YOUR_BOOLEAN_COL, ElementDataType.bool.name(), null));
    columns.add(new Column(YOUR_COL, YOUR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_CONFIG_FILE_COL, YOUR_CONFIG_FILE_COL, ElementDataType.configpath.name(),
        null));
    columns.add(new Column(YOUR_ROW_FILE_COL, YOUR_ROW_FILE_COL, ElementDataType.rowpath.name(), null));

    OrderedColumns ta = new OrderedColumns(APP_NAME, TABLE_ID_1, columns);

    int i;
    Parcel p = Parcel.obtain();
    ta.writeToParcel(p, 0);
    byte[] bytes = p.marshall();
    p.recycle();

    p = Parcel.obtain();
    p.unmarshall(bytes, 0, bytes.length);
    p.setDataPosition(0);

    OrderedColumns tb = OrderedColumns.CREATOR.createFromParcel(p);

    assertEquals(ta.getAppName(), tb.getAppName());
    assertEquals(ta.getTableId(), tb.getTableId());
    assertEquals(ta.graphViewIsPossible(), tb.graphViewIsPossible());
    assertEquals(ta.mapViewIsPossible(), tb.mapViewIsPossible());

    ArrayList<String> tsa = ta.getRetentionColumnNames();
    ArrayList<String> tsb = tb.getRetentionColumnNames();
    assertEquals(tsa.size(), tsb.size());
    for ( i = 0 ; i < tsa.size(); ++i ) {
      assertEquals(tsa.get(i), tsb.get(i));
    }
    ArrayList<ColumnDefinition> tca = ta.getColumnDefinitions();
    ArrayList<ColumnDefinition> tcb = tb.getColumnDefinitions();
    assertEquals(tca.size(), tcb.size());
    for ( i = 0 ; i < tca.size(); ++i ) {
      ColumnDefinition ca = tca.get(i);
      ColumnDefinition cb = tcb.get(i);
      assertEquals(ca, cb);
      assertEquals(ca.getElementKey(), cb.getElementKey());
      assertEquals(ca.getElementName(), cb.getElementName());
      assertEquals(ca.getElementType(), cb.getElementType());
      assertEquals(ca.getListChildElementKeys(), cb.getListChildElementKeys());
      assertEquals(ca.getType(), cb.getType());
    }

    TreeMap<String, Object> tas = ta.getDataModel();
    TreeMap<String, Object> tbs = tb.getDataModel();

    String vtas = ODKFileUtils.mapper.writeValueAsString(tas);
    String vtbs = ODKFileUtils.mapper.writeValueAsString(tbs);
    assertEquals(vtas, vtbs);

    assertEquals(ta.find(MY_COL), tb.find(MY_COL));
    assertEquals(ta.find(GEO_COL), tb.find(GEO_COL));
    assertEquals(ta.find(LIST_COL), tb.find(LIST_COL));
    boolean found = false;
    try {
      ta.find("unrecognized");
      found = true;
    } catch ( IllegalArgumentException e ) {
      // ignore
    }
    assertFalse(found);
    try {
      tb.find(null);
      found = true;
    } catch ( NullPointerException e ) {
      // ignore
    }
    assertFalse(found);
  }


  @Test
  public void testOrderedColumnsParcelationNoGeoNoArray() throws IOException {

    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column(GROUP_COL, GROUP_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(MY_COL, MY_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(THEIR_COL, THEIR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_COL, YOUR_COL, ElementDataType.integer.name(), null));

    OrderedColumns ta = new OrderedColumns(APP_NAME, TABLE_ID_1, columns);

    int i;
    Parcel p = Parcel.obtain();
    ta.writeToParcel(p, 0);
    byte[] bytes = p.marshall();
    p.recycle();

    p = Parcel.obtain();
    p.unmarshall(bytes, 0, bytes.length);
    p.setDataPosition(0);

    OrderedColumns tb = OrderedColumns.CREATOR.createFromParcel(p);

    assertEquals(ta.getAppName(), tb.getAppName());
    assertEquals(ta.getTableId(), tb.getTableId());
    assertEquals(ta.graphViewIsPossible(), tb.graphViewIsPossible());
    assertEquals(ta.mapViewIsPossible(), tb.mapViewIsPossible());

    ArrayList<String> tsa = ta.getRetentionColumnNames();
    ArrayList<String> tsb = tb.getRetentionColumnNames();
    assertEquals(tsa.size(), tsb.size());
    for ( i = 0 ; i < tsa.size(); ++i ) {
      assertEquals(tsa.get(i), tsb.get(i));
    }
    ArrayList<ColumnDefinition> tca = ta.getColumnDefinitions();
    ArrayList<ColumnDefinition> tcb = tb.getColumnDefinitions();
    assertEquals(tca.size(), tcb.size());
    for ( i = 0 ; i < tca.size(); ++i ) {
      ColumnDefinition ca = tca.get(i);
      ColumnDefinition cb = tcb.get(i);
      assertEquals(ca, cb);
      assertEquals(ca.getElementKey(), cb.getElementKey());
      assertEquals(ca.getElementName(), cb.getElementName());
      assertEquals(ca.getElementType(), cb.getElementType());
      assertEquals(ca.getListChildElementKeys(), cb.getListChildElementKeys());
      assertEquals(ca.getType(), cb.getType());
    }

    TreeMap<String, Object> tas = ta.getDataModel();
    TreeMap<String, Object> tbs = tb.getDataModel();

    String vtas = ODKFileUtils.mapper.writeValueAsString(tas);
    String vtbs = ODKFileUtils.mapper.writeValueAsString(tbs);
    assertEquals(vtas, vtbs);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testUserTableParcelation() throws IOException {

    List<String> geopointCells = new ArrayList<String>();
    geopointCells.add(GEO_COL + "_" + "accuracy");
    geopointCells.add(GEO_COL + "_" + "altitude");
    geopointCells.add(GEO_COL + "_" + "latitude");
    geopointCells.add(GEO_COL + "_" + "longitude");
    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column(GEO_COL, GEO_COL, ElementType.GEOPOINT,
        ODKFileUtils.mapper.writeValueAsString(geopointCells)));
    for ( String name : geopointCells ) {
      columns.add(new Column(name, name.substring(GEO_COL.length()+1),
          ElementDataType.number.name(), null));
    }
    columns.add(new Column(GROUP_COL, GROUP_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(MY_COL, MY_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(LIST_COL, LIST_COL, ElementDataType.array.name(),
        ODKFileUtils.mapper.writeValueAsString(Collections.singletonList(LIST_COL + "_items"))));
    columns.add(new Column(LIST_COL + "_items", "items", ElementDataType.integer.name(), null));
    columns.add(new Column(THEIR_COL, THEIR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_BOOLEAN_COL, YOUR_BOOLEAN_COL, ElementDataType.bool.name(), null));
    columns.add(new Column(YOUR_COL, YOUR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_CONFIG_FILE_COL, YOUR_CONFIG_FILE_COL, ElementDataType.configpath.name(),
        null));
    columns.add(new Column(YOUR_ROW_FILE_COL, YOUR_ROW_FILE_COL, ElementDataType.rowpath.name(), null));
    OrderedColumns orderedColumns = new OrderedColumns(APP_NAME, TABLE_ID_1, columns);

    List<String> retentionColumnNames = orderedColumns.getRetentionColumnNames();
    HashMap<String, Integer> elementKeyToIndex = new HashMap<String, Integer>();
    String[] elementKeyForIndex = new String[retentionColumnNames.size()+ADMIN_COLUMNS.size()];
    String[] rowValues1 = new String[retentionColumnNames.size() + ADMIN_COLUMNS.size()];
    String[] rowValues2 = new String[retentionColumnNames.size() + ADMIN_COLUMNS.size()];
    int i = 0;
    rowValues1[i] = "15.5"; // accuracy
    rowValues2[i] = "25.0";
    elementKeyForIndex[i] = geopointCells.get(i);
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(geopointCells.get(i), i++);
    rowValues1[i] = "215.5"; // altitude
    rowValues2[i] = "-125.0";
    elementKeyForIndex[i] = geopointCells.get(i);
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(geopointCells.get(i), i++);
    rowValues1[i] = "47.6097"; // latitude
    rowValues2[i] = "-47.6097";
    elementKeyForIndex[i] = geopointCells.get(i);
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(geopointCells.get(i), i++);
    rowValues1[i] = "122.3331"; // longitude
    rowValues2[i] = "-122.3331";
    elementKeyForIndex[i] = geopointCells.get(i);
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(geopointCells.get(i), i++);
    rowValues1[i] = "15";
    rowValues2[i] = "25";
    elementKeyForIndex[i] = GROUP_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(GROUP_COL, i++);
    rowValues1[i] = ODKFileUtils.mapper.writeValueAsString(Collections.singletonList("915"));
    rowValues2[i] = ODKFileUtils.mapper.writeValueAsString(Collections.singletonList("-15"));
    elementKeyForIndex[i] = LIST_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(LIST_COL, i++);
    rowValues1[i] = "5";
    rowValues2[i] = "5";
    elementKeyForIndex[i] = MY_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(MY_COL, i++);
    rowValues1[i] = "35";
    rowValues2[i] = "45";
    elementKeyForIndex[i] = THEIR_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(THEIR_COL, i++);
    rowValues1[i] = Boolean.toString(true);
    rowValues2[i] = null;
    elementKeyForIndex[i] = YOUR_BOOLEAN_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(YOUR_BOOLEAN_COL, i++);
    rowValues1[i] = "9";
    rowValues2[i] = "9";
    elementKeyForIndex[i] = YOUR_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(YOUR_COL, i++);
    rowValues1[i] = "assets/configfile.js";
    rowValues2[i] = "assets/configfile.html";
    elementKeyForIndex[i] = YOUR_CONFIG_FILE_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(YOUR_CONFIG_FILE_COL, i++);
    rowValues1[i] = "filename.jpg";
    rowValues2[i] = "filename.wav";
    elementKeyForIndex[i] = YOUR_ROW_FILE_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(YOUR_ROW_FILE_COL, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.CONFLICT_TYPE;
    elementKeyToIndex.put(DataTableColumns.CONFLICT_TYPE, i++); // not exportable
    rowValues1[i] = "FULL";
    rowValues2[i] = "FULL";
    elementKeyForIndex[i] = DataTableColumns.DEFAULT_ACCESS;
    elementKeyToIndex.put(DataTableColumns.DEFAULT_ACCESS, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.ROW_OWNER;
    elementKeyToIndex.put(DataTableColumns.ROW_OWNER, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.GROUP_READ_ONLY;
    elementKeyToIndex.put(DataTableColumns.GROUP_READ_ONLY, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_MODIFY;
    elementKeyToIndex.put(DataTableColumns.GROUP_MODIFY, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_PRIVILEGED;
    elementKeyToIndex.put(DataTableColumns.GROUP_PRIVILEGED, i++);
    rowValues1[i] = "myform_1";
    rowValues2[i] = "myform_2";
    elementKeyForIndex[i] = DataTableColumns.FORM_ID;
    elementKeyToIndex.put(DataTableColumns.FORM_ID, i++);
    rowValues1[i] = INSTANCE_ID_1;
    rowValues2[i] = INSTANCE_ID_2;
    elementKeyForIndex[i] = DataTableColumns.ID;
    elementKeyToIndex.put(DataTableColumns.ID, i++);
    rowValues1[i] = "default";
    rowValues2[i] = "fr";
    elementKeyForIndex[i] = DataTableColumns.LOCALE;
    elementKeyToIndex.put(DataTableColumns.LOCALE, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.ROW_ETAG;
    elementKeyToIndex.put(DataTableColumns.ROW_ETAG, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_CREATOR;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_CREATOR, i++);
    rowValues1[i] = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), Locale.ROOT);
    rowValues2[i] = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), Locale.ROOT);
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_TIMESTAMP;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_TIMESTAMP, i++);
    rowValues1[i] = SavepointTypeManipulator.complete();
    rowValues2[i] = SavepointTypeManipulator.incomplete();
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_TYPE;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_TYPE, i++);
    rowValues1[i] = SyncState.new_row.name();
    rowValues2[i] = SyncState.new_row.name();
    elementKeyForIndex[i] = DataTableColumns.SYNC_STATE;
    elementKeyToIndex.put(DataTableColumns.SYNC_STATE, i++); // not exportable

    String[] adminColumnOrder = ADMIN_COLUMNS.toArray(new String[ADMIN_COLUMNS.size()]);

    UserTable table = new UserTable(orderedColumns, MY_COL + "=?",
        new Object[] { "test", 5, 9.8, false },
        new String[] { GROUP_COL }, YOUR_COL + "=?", new String[] { THEIR_COL },
        new String[] { "ASC" }, adminColumnOrder, elementKeyToIndex, elementKeyForIndex, null);

    Row row;

    row = new Row(rowValues1.clone(), table.getBaseTable());
    table.addRow(row);
    row = new Row(rowValues2.clone(), table.getBaseTable());
    table.addRow(row);

    Parcel p = Parcel.obtain();
    table.writeToParcel(p, 0);
    byte[] bytes = p.marshall();
    p.recycle();

    p = Parcel.obtain();
    p.unmarshall(bytes, 0, bytes.length);
    p.setDataPosition(0);

    UserTable t = UserTable.CREATOR.createFromParcel(p);

    // appName
    assertEquals(table.getAppName(), t.getAppName());
    // tableId
    assertEquals(table.getTableId(), t.getTableId());

    OrderedColumns ta = table.getColumnDefinitions();
    OrderedColumns tb = table.getColumnDefinitions();
    assertEquals(ta.getAppName(), tb.getAppName());
    assertEquals(ta.getTableId(), tb.getTableId());
    assertEquals(ta.graphViewIsPossible(), tb.graphViewIsPossible());
    assertEquals(ta.mapViewIsPossible(), tb.mapViewIsPossible());

    ArrayList<String> tsa = ta.getRetentionColumnNames();
    ArrayList<String> tsb = tb.getRetentionColumnNames();
    assertEquals(tsa.size(), tsb.size());
    for ( i = 0 ; i < tsa.size(); ++i ) {
      assertEquals(tsa.get(i), tsb.get(i));
    }
    ArrayList<ColumnDefinition> tca = ta.getColumnDefinitions();
    ArrayList<ColumnDefinition> tcb = tb.getColumnDefinitions();
    assertEquals(tca.size(), tcb.size());
    for ( i = 0 ; i < tca.size(); ++i ) {
      ColumnDefinition ca = tca.get(i);
      ColumnDefinition cb = tcb.get(i);
      assertEquals(ca, cb);
      assertEquals(ca.getElementKey(), cb.getElementKey());
      assertEquals(ca.getElementName(), cb.getElementName());
      assertEquals(ca.getElementType(), cb.getElementType());
      assertEquals(ca.getListChildElementKeys(), cb.getListChildElementKeys());
      assertEquals(ca.getType(), cb.getType());
    }

    // verify mappings
    assertEquals(table.getWidth(), t.getWidth());
    for ( i = 0 ; i < table.getWidth() ; ++i ) {
      String elementKey = table.getElementKey(i);
      assertEquals(elementKey, t.getElementKey(i));
      assertEquals((Integer) i, table.getColumnIndexOfElementKey(elementKey));
      assertEquals((Integer) i, t.getColumnIndexOfElementKey(elementKey));
    }

    // verify rows
    assertEquals(2, table.getNumberOfRows());
    assertEquals(2, t.getNumberOfRows());
    assertEquals(0, table.getRowNumFromId(INSTANCE_ID_1));
    assertEquals(0, t.getRowNumFromId(INSTANCE_ID_1));
    assertEquals(1, table.getRowNumFromId(INSTANCE_ID_2));
    assertEquals(1, t.getRowNumFromId(INSTANCE_ID_2));
    TypedRow rat1 = table.getRowAtIndex(0);
    TypedRow rat2 = table.getRowAtIndex(1);
    TypedRow rbt1 = t.getRowAtIndex(0);
    TypedRow rbt2 = t.getRowAtIndex(1);

    assertEquals(rat1.getDataByKey(DataTableColumns.ID), INSTANCE_ID_1);
    assertEquals(rbt1.getDataByKey(DataTableColumns.ID), INSTANCE_ID_1);
    assertEquals(rat2.getDataByKey(DataTableColumns.ID), INSTANCE_ID_2);
    assertEquals(rbt2.getDataByKey(DataTableColumns.ID), INSTANCE_ID_2);
    assertEquals(rat1.getDataByKey(DataTableColumns.ID), table.getRowId(0));
    assertEquals(rbt1.getDataByKey(DataTableColumns.ID), table.getRowId(0));
    assertEquals(rat2.getDataByKey(DataTableColumns.ID), table.getRowId(1));
    assertEquals(rbt2.getDataByKey(DataTableColumns.ID), table.getRowId(1));
    for ( i = 0 ; i < table.getWidth() ; ++i ) {
      String elementKey = table.getElementKey(i);
      assertEquals(rowValues1[i], rat1.getStringValueByKey(elementKey));
      assertEquals(rowValues1[i], rbt1.getStringValueByKey(elementKey));
      assertEquals(rowValues2[i], rat2.getStringValueByKey(elementKey));
      assertEquals(rowValues2[i], rbt2.getStringValueByKey(elementKey));
      try {
        assertEquals(table.getDisplayTextOfData(0, table.getColumnDefinitions().find(elementKey)
            .getType(), elementKey), t.getDisplayTextOfData(0, t.getColumnDefinitions().find
            (elementKey).getType(), elementKey));
        assertEquals(table.getDisplayTextOfData(1, table.getColumnDefinitions().find(elementKey)
            .getType(), elementKey), t.getDisplayTextOfData(1, t.getColumnDefinitions().find
            (elementKey).getType(), elementKey));
      } catch ( Exception e) {
        // ignore...
      }
    }

    int cellIndex = table.getColumnIndexOfElementKey(GROUP_COL);
    Long va = rat1.getDataType(cellIndex, Long.class);
    Long vb = rbt1.getDataType(cellIndex, Long.class);
    assertEquals(va, Long.valueOf(15));
    assertEquals(va, vb);
    Long vai = rat1.getDataType(cellIndex, Long.class);
    Long vbi = rbt1.getDataType(cellIndex, Long.class);
    assertEquals(vai, Long.valueOf(15));
    assertEquals(vai, vbi);
    cellIndex = table.getColumnIndexOfElementKey(geopointCells.get(2));
    Double vad = rat1.getDataType(cellIndex, Double.class);
    Double vbd = rbt1.getDataType(cellIndex, Double.class);
    assertEquals(vad, Double.valueOf(47.6097));
    assertEquals(vad, vbd);
    assertEquals("47.6097", table.getDisplayTextOfData(0, orderedColumns.find(geopointCells.get(2))
        .getType(), geopointCells.get(2)));
    cellIndex = table.getColumnIndexOfElementKey(YOUR_BOOLEAN_COL);
    Boolean vabb = rat1.getDataType(cellIndex, Boolean.class);
    Boolean vbbb = rbt1.getDataType(cellIndex, Boolean.class);
    assertEquals(vabb, Boolean.TRUE);
    assertEquals(vabb, vbbb);
    vabb = rat2.getDataType(cellIndex, Boolean.class);
    vbbb = rbt2.getDataType(cellIndex, Boolean.class);
    assertNull(vabb);
    assertEquals(vabb, vbbb);
    cellIndex = table.getColumnIndexOfElementKey(YOUR_ROW_FILE_COL);
    String vaf = rat1.getDataType(cellIndex, String.class);
    String vbf = rbt1.getDataType(cellIndex, String.class);
    assertEquals(vaf, "filename.jpg");
    assertEquals(vaf, vbf);
    cellIndex = table.getColumnIndexOfElementKey(LIST_COL);
    ArrayList<Object> vaar = rat1.getDataType(cellIndex, ArrayList.class);
    ArrayList<Object> vbar = rbt1.getDataType(cellIndex, ArrayList.class);
    assertEquals(vaar.size(), vbar.size());
    for ( i = 0 ; i < vaar.size() ; ++i) {
      assertEquals(vaar.get(i), vbar.get(i));
    }
    cellIndex = table.getColumnIndexOfElementKey(DataTableColumns.CONFLICT_TYPE);
    Integer cta = rat1.getDataType(cellIndex, Integer.class);
    assertNull(cta);
  }

  @Test
  public void testUserTableParcelationNoGeoNoArray() throws IOException {

    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column(GROUP_COL, GROUP_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(MY_COL, MY_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(THEIR_COL, THEIR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_COL, YOUR_COL, ElementDataType.integer.name(), null));
    OrderedColumns orderedColumns = new OrderedColumns(APP_NAME, TABLE_ID_1, columns);

    List<String> retentionColumnNames = orderedColumns.getRetentionColumnNames();
    HashMap<String, Integer> elementKeyToIndex = new HashMap<String, Integer>();
    String[] elementKeyForIndex = new String[retentionColumnNames.size()+ADMIN_COLUMNS.size()];
    String[] rowValues1 = new String[retentionColumnNames.size() + ADMIN_COLUMNS.size()];
    String[] rowValues2 = new String[retentionColumnNames.size() + ADMIN_COLUMNS.size()];
    int i = 0;
    rowValues1[i] = "15";
    rowValues2[i] = "25";
    elementKeyForIndex[i] = GROUP_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(GROUP_COL, i++);
    rowValues1[i] = "5";
    rowValues2[i] = "5";
    elementKeyForIndex[i] = MY_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(MY_COL, i++);
    rowValues1[i] = "35";
    rowValues2[i] = "45";
    elementKeyForIndex[i] = THEIR_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(THEIR_COL, i++);
    rowValues1[i] = "9";
    rowValues2[i] = "9";
    elementKeyForIndex[i] = YOUR_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(YOUR_COL, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.CONFLICT_TYPE;
    elementKeyToIndex.put(DataTableColumns.CONFLICT_TYPE, i++); // not exportable
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.DEFAULT_ACCESS;
    elementKeyToIndex.put(DataTableColumns.DEFAULT_ACCESS, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.ROW_OWNER;
    elementKeyToIndex.put(DataTableColumns.ROW_OWNER, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_READ_ONLY;
    elementKeyToIndex.put(DataTableColumns.GROUP_READ_ONLY, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_MODIFY;
    elementKeyToIndex.put(DataTableColumns.GROUP_MODIFY, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_PRIVILEGED;
    elementKeyToIndex.put(DataTableColumns.GROUP_PRIVILEGED, i++);
    rowValues1[i] = "myform_1";
    rowValues2[i] = "myform_2";
    elementKeyForIndex[i] = DataTableColumns.FORM_ID;
    elementKeyToIndex.put(DataTableColumns.FORM_ID, i++);
    rowValues1[i] = INSTANCE_ID_1;
    rowValues2[i] = INSTANCE_ID_2;
    elementKeyForIndex[i] = DataTableColumns.ID;
    elementKeyToIndex.put(DataTableColumns.ID, i++);
    rowValues1[i] = "default";
    rowValues2[i] = "fr";
    elementKeyForIndex[i] = DataTableColumns.LOCALE;
    elementKeyToIndex.put(DataTableColumns.LOCALE, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.ROW_ETAG;
    elementKeyToIndex.put(DataTableColumns.ROW_ETAG, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_CREATOR;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_CREATOR, i++);
    rowValues1[i] = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), Locale.ROOT);
    rowValues2[i] = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), Locale.ROOT);
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_TIMESTAMP;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_TIMESTAMP, i++);
    rowValues1[i] = SavepointTypeManipulator.complete();
    rowValues2[i] = SavepointTypeManipulator.incomplete();
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_TYPE;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_TYPE, i++);
    rowValues1[i] = SyncState.new_row.name();
    rowValues2[i] = SyncState.new_row.name();
    elementKeyForIndex[i] = DataTableColumns.SYNC_STATE;
    elementKeyToIndex.put(DataTableColumns.SYNC_STATE, i++); // not exportable

    String[] adminColumnOrder = ADMIN_COLUMNS.toArray(new String[ADMIN_COLUMNS.size()]);

    UserTable table = new UserTable(orderedColumns, null, null, null, null, null,
        null,
        adminColumnOrder,
        elementKeyToIndex, elementKeyForIndex, null);

    Row row;

    row = new Row(rowValues1.clone(), table.getBaseTable());
    table.addRow(row);
    row = new Row(rowValues2.clone(), table.getBaseTable());
    table.addRow(row);

    Parcel p = Parcel.obtain();
    table.writeToParcel(p, 0);
    byte[] bytes = p.marshall();
    p.recycle();

    p = Parcel.obtain();
    p.unmarshall(bytes, 0, bytes.length);
    p.setDataPosition(0);

    UserTable t = UserTable.CREATOR.createFromParcel(p);

    // appName
    assertEquals(table.getAppName(), t.getAppName());
    // tableId
    assertEquals(table.getTableId(), t.getTableId());

    OrderedColumns ta = table.getColumnDefinitions();
    OrderedColumns tb = table.getColumnDefinitions();
    assertEquals(ta.getAppName(), tb.getAppName());
    assertEquals(ta.getTableId(), tb.getTableId());
    assertEquals(ta.graphViewIsPossible(), tb.graphViewIsPossible());
    assertEquals(ta.mapViewIsPossible(), tb.mapViewIsPossible());

    ArrayList<String> tsa = ta.getRetentionColumnNames();
    ArrayList<String> tsb = tb.getRetentionColumnNames();
    assertEquals(tsa.size(), tsb.size());
    for ( i = 0 ; i < tsa.size(); ++i ) {
      assertEquals(tsa.get(i), tsb.get(i));
    }
    ArrayList<ColumnDefinition> tca = ta.getColumnDefinitions();
    ArrayList<ColumnDefinition> tcb = tb.getColumnDefinitions();
    assertEquals(tca.size(), tcb.size());
    for ( i = 0 ; i < tca.size(); ++i ) {
      ColumnDefinition ca = tca.get(i);
      ColumnDefinition cb = tcb.get(i);
      assertEquals(ca, cb);
      assertEquals(ca.getElementKey(), cb.getElementKey());
      assertEquals(ca.getElementName(), cb.getElementName());
      assertEquals(ca.getElementType(), cb.getElementType());
      assertEquals(ca.getListChildElementKeys(), cb.getListChildElementKeys());
      assertEquals(ca.getType(), cb.getType());
    }

    // verify mappings
    assertEquals(table.getWidth(), t.getWidth());
    for ( i = 0 ; i < table.getWidth() ; ++i ) {
      String elementKey = table.getElementKey(i);
      assertEquals(elementKey, t.getElementKey(i));
      assertEquals((Integer) i, table.getColumnIndexOfElementKey(elementKey));
      assertEquals((Integer) i, t.getColumnIndexOfElementKey(elementKey));
    }

    // verify rows
    assertEquals(2, table.getNumberOfRows());
    assertEquals(2, t.getNumberOfRows());
    assertEquals(0, table.getRowNumFromId(INSTANCE_ID_1));
    assertEquals(0, t.getRowNumFromId(INSTANCE_ID_1));
    assertEquals(1, table.getRowNumFromId(INSTANCE_ID_2));
    assertEquals(1, t.getRowNumFromId(INSTANCE_ID_2));
    TypedRow rat1 = table.getRowAtIndex(0);
    TypedRow rat2 = table.getRowAtIndex(1);
    TypedRow rbt1 = t.getRowAtIndex(0);
    TypedRow rbt2 = t.getRowAtIndex(1);

    assertEquals(rat1.getDataByKey(DataTableColumns.ID), INSTANCE_ID_1);
    assertEquals(rbt1.getDataByKey(DataTableColumns.ID), INSTANCE_ID_1);
    assertEquals(rat2.getDataByKey(DataTableColumns.ID), INSTANCE_ID_2);
    assertEquals(rbt2.getDataByKey(DataTableColumns.ID), INSTANCE_ID_2);
    assertEquals(rat1.getDataByKey(DataTableColumns.ID), table.getRowId(0));
    assertEquals(rbt1.getDataByKey(DataTableColumns.ID), table.getRowId(0));
    assertEquals(rat2.getDataByKey(DataTableColumns.ID), table.getRowId(1));
    assertEquals(rbt2.getDataByKey(DataTableColumns.ID), table.getRowId(1));
    for ( i = 0 ; i < table.getWidth() ; ++i ) {
      String elementKey = table.getElementKey(i);
      assertEquals(rowValues1[i], rat1.getStringValueByKey(elementKey));
      assertEquals(rowValues1[i], rbt1.getStringValueByKey(elementKey));
      assertEquals(rowValues2[i], rat2.getStringValueByKey(elementKey));
      assertEquals(rowValues2[i], rbt2.getStringValueByKey(elementKey));
    }

    assertFalse(table.hasCheckpointRows());
    assertFalse(table.hasConflictRows());
    assertFalse(t.hasCheckpointRows());
    assertFalse(t.hasConflictRows());
  }

  @Test
  public void testUserTableSubsetNoGeoNoArray() throws IOException {

    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column(GROUP_COL, GROUP_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(MY_COL, MY_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(THEIR_COL, THEIR_COL, ElementDataType.integer.name(), null));
    columns.add(new Column(YOUR_COL, YOUR_COL, ElementDataType.integer.name(), null));
    OrderedColumns orderedColumns = new OrderedColumns(APP_NAME, TABLE_ID_1, columns);

    List<String> retentionColumnNames = orderedColumns.getRetentionColumnNames();
    HashMap<String, Integer> elementKeyToIndex = new HashMap<String, Integer>();
    String[] elementKeyForIndex = new String[retentionColumnNames.size()+ADMIN_COLUMNS.size()];
    String[] rowValues1 = new String[retentionColumnNames.size() + ADMIN_COLUMNS.size()];
    String[] rowValues2 = new String[retentionColumnNames.size() + ADMIN_COLUMNS.size()];
    int i = 0;
    rowValues1[i] = "15";
    rowValues2[i] = "25";
    elementKeyForIndex[i] = GROUP_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(GROUP_COL, i++);
    rowValues1[i] = "5";
    rowValues2[i] = "5";
    elementKeyForIndex[i] = MY_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(MY_COL, i++);
    rowValues1[i] = "35";
    rowValues2[i] = "45";
    elementKeyForIndex[i] = THEIR_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(THEIR_COL, i++);
    rowValues1[i] = "9";
    rowValues2[i] = "9";
    elementKeyForIndex[i] = YOUR_COL;
    assertEquals(retentionColumnNames.get(i), elementKeyForIndex[i]);
    elementKeyToIndex.put(YOUR_COL, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.CONFLICT_TYPE;
    elementKeyToIndex.put(DataTableColumns.CONFLICT_TYPE, i++); // not exportable
    rowValues1[i] = "FULL";
    rowValues2[i] = "FULL";
    elementKeyForIndex[i] = DataTableColumns.DEFAULT_ACCESS;
    elementKeyToIndex.put(DataTableColumns.DEFAULT_ACCESS, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.ROW_OWNER;
    elementKeyToIndex.put(DataTableColumns.ROW_OWNER, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_READ_ONLY;
    elementKeyToIndex.put(DataTableColumns.GROUP_READ_ONLY, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_MODIFY;
    elementKeyToIndex.put(DataTableColumns.GROUP_MODIFY, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.GROUP_PRIVILEGED;
    elementKeyToIndex.put(DataTableColumns.GROUP_PRIVILEGED, i++);
    rowValues1[i] = "myform_1";
    rowValues2[i] = "myform_2";
    elementKeyForIndex[i] = DataTableColumns.FORM_ID;
    elementKeyToIndex.put(DataTableColumns.FORM_ID, i++);
    rowValues1[i] = INSTANCE_ID_1;
    rowValues2[i] = INSTANCE_ID_2;
    elementKeyForIndex[i] = DataTableColumns.ID;
    elementKeyToIndex.put(DataTableColumns.ID, i++);
    rowValues1[i] = "default";
    rowValues2[i] = "fr";
    elementKeyForIndex[i] = DataTableColumns.LOCALE;
    elementKeyToIndex.put(DataTableColumns.LOCALE, i++);
    rowValues1[i] = null;
    rowValues2[i] = null;
    elementKeyForIndex[i] = DataTableColumns.ROW_ETAG;
    elementKeyToIndex.put(DataTableColumns.ROW_ETAG, i++);
    rowValues1[i] = "sudar";
    rowValues2[i] = "wrb";
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_CREATOR;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_CREATOR, i++);
    rowValues1[i] = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), Locale.ROOT);
    rowValues2[i] = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), Locale.ROOT);
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_TIMESTAMP;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_TIMESTAMP, i++);
    rowValues1[i] = SavepointTypeManipulator.complete();
    rowValues2[i] = SavepointTypeManipulator.incomplete();
    elementKeyForIndex[i] = DataTableColumns.SAVEPOINT_TYPE;
    elementKeyToIndex.put(DataTableColumns.SAVEPOINT_TYPE, i++);
    rowValues1[i] = SyncState.new_row.name();
    rowValues2[i] = SyncState.new_row.name();
    elementKeyForIndex[i] = DataTableColumns.SYNC_STATE;
    elementKeyToIndex.put(DataTableColumns.SYNC_STATE, i++); // not exportable

    String[] adminColumnOrder = ADMIN_COLUMNS.toArray(new String[ADMIN_COLUMNS.size()]);

    UserTable table = new UserTable(orderedColumns, null, null, null, null, null,
        null,
        adminColumnOrder,
        elementKeyToIndex, elementKeyForIndex, null);

    Row row;

    row = new Row(rowValues1.clone(), table.getBaseTable());
    table.addRow(row);
    row = new Row(rowValues2.clone(), table.getBaseTable());
    table.addRow(row);

    List<Integer> idxs = new ArrayList<Integer>();
    idxs.add(0);
    idxs.add(1);
    UserTable t = new UserTable(table, idxs);

    // appName
    assertEquals(table.getAppName(), t.getAppName());
    // tableId
    assertEquals(table.getTableId(), t.getTableId());

    OrderedColumns ta = table.getColumnDefinitions();
    OrderedColumns tb = table.getColumnDefinitions();
    assertEquals(ta.getAppName(), tb.getAppName());
    assertEquals(ta.getTableId(), tb.getTableId());
    assertEquals(ta.graphViewIsPossible(), tb.graphViewIsPossible());
    assertEquals(ta.mapViewIsPossible(), tb.mapViewIsPossible());

    ArrayList<String> tsa = ta.getRetentionColumnNames();
    ArrayList<String> tsb = tb.getRetentionColumnNames();
    assertEquals(tsa.size(), tsb.size());
    for ( i = 0 ; i < tsa.size(); ++i ) {
      assertEquals(tsa.get(i), tsb.get(i));
    }
    ArrayList<ColumnDefinition> tca = ta.getColumnDefinitions();
    ArrayList<ColumnDefinition> tcb = tb.getColumnDefinitions();
    assertEquals(tca.size(), tcb.size());
    for ( i = 0 ; i < tca.size(); ++i ) {
      ColumnDefinition ca = tca.get(i);
      ColumnDefinition cb = tcb.get(i);
      assertEquals(ca, cb);
      assertEquals(ca.getElementKey(), cb.getElementKey());
      assertEquals(ca.getElementName(), cb.getElementName());
      assertEquals(ca.getElementType(), cb.getElementType());
      assertEquals(ca.getListChildElementKeys(), cb.getListChildElementKeys());
      assertEquals(ca.getType(), cb.getType());
    }

    // verify mappings
    assertEquals(table.getWidth(), t.getWidth());
    for ( i = 0 ; i < table.getWidth() ; ++i ) {
      String elementKey = table.getElementKey(i);
      assertEquals(elementKey, t.getElementKey(i));
      assertEquals((Integer) i, table.getColumnIndexOfElementKey(elementKey));
      assertEquals((Integer) i, t.getColumnIndexOfElementKey(elementKey));
    }

    // verify rows
    assertEquals(2, table.getNumberOfRows());
    assertEquals(2, t.getNumberOfRows());
    assertEquals(0, table.getRowNumFromId(INSTANCE_ID_1));
    assertEquals(0, t.getRowNumFromId(INSTANCE_ID_1));
    assertEquals(1, table.getRowNumFromId(INSTANCE_ID_2));
    assertEquals(1, t.getRowNumFromId(INSTANCE_ID_2));
    TypedRow rat1 = table.getRowAtIndex(0);
    TypedRow rat2 = table.getRowAtIndex(1);
    TypedRow rbt1 = t.getRowAtIndex(0);
    TypedRow rbt2 = t.getRowAtIndex(1);

    assertEquals(rat1.getDataByKey(DataTableColumns.ID), INSTANCE_ID_1);
    assertEquals(rbt1.getDataByKey(DataTableColumns.ID), INSTANCE_ID_1);
    assertEquals(rat2.getDataByKey(DataTableColumns.ID), INSTANCE_ID_2);
    assertEquals(rbt2.getDataByKey(DataTableColumns.ID), INSTANCE_ID_2);
    assertEquals(rat1.getDataByKey(DataTableColumns.ID), table.getRowId(0));
    assertEquals(rbt1.getDataByKey(DataTableColumns.ID), table.getRowId(0));
    assertEquals(rat2.getDataByKey(DataTableColumns.ID), table.getRowId(1));
    assertEquals(rbt2.getDataByKey(DataTableColumns.ID), table.getRowId(1));
    for ( i = 0 ; i < table.getWidth() ; ++i ) {
      String elementKey = table.getElementKey(i);
      assertEquals(rowValues1[i], rat1.getStringValueByKey(elementKey));
      assertEquals(rowValues1[i], rbt1.getStringValueByKey(elementKey));
      assertEquals(rowValues2[i], rat2.getStringValueByKey(elementKey));
      assertEquals(rowValues2[i], rbt2.getStringValueByKey(elementKey));
    }

    assertFalse(table.hasCheckpointRows());
    assertFalse(table.hasConflictRows());
    assertFalse(t.hasCheckpointRows());
    assertFalse(t.hasConflictRows());
  }
}
