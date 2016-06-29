/*
 * Copyright (C) 2016 University of Washington
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

package org.opendatakit.common.android.utilities;

import android.os.Parcel;
import android.test.AndroidTestCase;
import org.opendatakit.common.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.database.service.OdkDbRow;
import org.opendatakit.database.service.OdkDbTable;

import java.io.IOException;
import java.util.*;

public class OdkDbTableTest extends AndroidTestCase {

  /* Test data */
  private static final String COLUMN1 = "firstCol";
  private static final String COLUMN2 = "secondCol";
  private static final String COLUMN3 = "thirdCol";
  private static final String COLUMN4 = "fourthCol";
  private static final String COLUMN5 = "fifthCol";
  private static final int TABLE_WIDTH = 5;

  private static final String ROW1COL1 = "10";
  private static final String ROW1COL2 = null;
  private static final String ROW1COL3 = "text value";
  private static final String ROW1COL4 = null;
  private static final String ROW1COL5 = "15";

  private static final String ROW2COL1 = "20";
  private static final String ROW2COL2 = null;
  private static final String ROW2COL3 = "10";
  private static final String ROW2COL4 = "test value";
  private static final String ROW2COL5 = "10";

  private static final int NUM_ROWS = 2;

  private static final String[] PRIMARY_KEY = {COLUMN1, COLUMN3};
  private static final String[] ROW1_PK = {ROW1COL1, ROW1COL3};
  private static final String[] ROW2_PK = {ROW2COL1, ROW2COL3};

  private static final String SQLCMD = "";
  private static final String[] BINDARGS = {"test", "text"};
  private static final String[] ORDERBYELEM = null;
  private static final String[] ORDERBYDIR = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  public void testOdkDbTableParcelation() throws IOException {

    /*
     * Create test data
     */
    HashMap<String, Integer> elementKeyToIndex = new HashMap<>();
    String[] rowValues1 = new String[TABLE_WIDTH];
    String[] rowValues2 = new String[TABLE_WIDTH];

    int i = 0;
    rowValues1[i] = ROW1COL1;
    rowValues2[i] = ROW2COL1;
    elementKeyToIndex.put(COLUMN1, i++);

    rowValues1[i] = ROW1COL2;
    rowValues2[i] = ROW2COL2;
    elementKeyToIndex.put(COLUMN2, i++);

    rowValues1[i] = ROW1COL3;
    rowValues2[i] = ROW2COL3;
    elementKeyToIndex.put(COLUMN3, i++);

    rowValues1[i] = ROW1COL4;
    rowValues2[i] = ROW2COL4;
    elementKeyToIndex.put(COLUMN4, i++);

    rowValues1[i] = ROW1COL5;
    rowValues2[i] = ROW2COL5;
    elementKeyToIndex.put(COLUMN5, i++);

    String sqlCmd = SQLCMD;
    String[] bindArgs = BINDARGS;
    String[] orderByArgs = ORDERBYELEM;
    String[] orderByDirections = ORDERBYDIR;
    String[] primaryKey = PRIMARY_KEY;

    OdkDbTable table = new OdkDbTable(sqlCmd, bindArgs, orderByDirections, orderByArgs,
        primaryKey, elementKeyToIndex, NUM_ROWS);

    OdkDbRow row1 = new OdkDbRow(rowValues1, table);
    table.addRow(row1);

    OdkDbRow row2 = new OdkDbRow(rowValues2, table);
    table.addRow(row2);

    /*
     * Marshall the test data
     */
    Parcel p = Parcel.obtain();
    table.writeToParcel(p, 0);
    byte[] bytes = p.marshall();
    p.recycle();

    p = Parcel.obtain();
    p.unmarshall(bytes, 0, bytes.length);
    p.setDataPosition(0);

    OdkDbTable t = OdkDbTable.CREATOR.createFromParcel(p);

    /*
     * Test that the data survived
     */

    // sql command
    assertEquals(table.getSqlCommand(), t.getSqlCommand());

    // selectionArgs
    String[] sa = table.getSqlBindArgs();
    String[] sb = t.getSqlBindArgs();
    if ( sa != null && sb != null ) {
      assertEquals(sa.length, sb.length);
      for ( i = 0 ; i < sa.length ; ++i ) {
        assertEquals(sa[i], sb[i]);
      }
    } else {
      assertNull(sa);
      assertNull(sb);
    }

    // order by elementKey
    String[] ea = table.getOrderByElementKey();
    String[] eb = t.getOrderByElementKey();
    if ( ea != null && eb != null ) {
      assertEquals(ea.length, eb.length);
      for ( i = 0 ; i < ea.length ; ++i ) {
        assertEquals(ea[i], eb[i]);
      }
    } else {
      assertNull(ea);
      assertNull(eb);
    }

    // order by direction
    String[] da = table.getOrderByDirection();
    String[] db = t.getOrderByDirection();
    if ( da != null && db != null ) {
      assertEquals(da.length, db.length);
      for ( i = 0 ; i < da.length ; ++i ) {
        assertEquals(da[i], db[i]);
      }
    } else {
      assertNull(da);
      assertNull(db);
    }

    // primary key
    String[] pa = table.getPrimaryKey();
    String[] pb = t.getPrimaryKey();
    if ( pa != null && pb != null ) {
      assertEquals(pa.length, pb.length);
      for ( i = 0 ; i < pa.length ; ++i ) {
        assertEquals(pa[i], pb[i]);
      }
    } else {
      assertNull(pa);
      assertNull(pb);
    }

    // verify rows
    assertEquals(NUM_ROWS, table.getNumberOfRows());
    assertEquals(NUM_ROWS, t.getNumberOfRows());

    OdkDbRow rat1 = table.getRow(ROW1_PK);
    OdkDbRow rat2 = t.getRow(ROW1_PK);
    OdkDbRow rbt1 = table.getRow(ROW2_PK);
    OdkDbRow rbt2 = t.getRow(ROW2_PK);

    Set<String> cols = table.getCols();
    for (String col: cols) {
      assertEquals(rat2.getData(col), rat1.getData(col));
      assertEquals(rbt2.getData(col), rbt1.getData(col));
    }
  }

}
