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

package org.opendatakit.utilities;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.data.Row;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OdkBaseTableTest {

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
  private static final Object[] BINDARGS = new Object[] {"test", 1, 1.1, 1.1f, false, true, 2452L };
  private static final String[] ORDERBYELEM = null;
  private static final String[] ORDERBYDIR = null;

  @Before
  public void setUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testOdkTableParcelation() throws IOException {

    /*
     * Create test data
     */
    String[] elementKeyForIndex = new String[TABLE_WIDTH];
    String[] rowValues1 = new String[TABLE_WIDTH];
    String[] rowValues2 = new String[TABLE_WIDTH];

    int index = 0;
    elementKeyForIndex[index] = COLUMN1;
    rowValues1[index] = ROW1COL1;
    rowValues2[index] = ROW2COL1;
    index++;

    elementKeyForIndex[index] = COLUMN2;
    rowValues1[index] = ROW1COL2;
    rowValues2[index] = ROW2COL2;
    index++;

    elementKeyForIndex[index] = COLUMN3;
    rowValues1[index] = ROW1COL3;
    rowValues2[index] = ROW2COL3;
    index++;

    elementKeyForIndex[index] = COLUMN4;
    rowValues1[index] = ROW1COL4;
    rowValues2[index] = ROW2COL4;
    index++;

    elementKeyForIndex[index] = COLUMN5;
    rowValues1[index] = ROW1COL5;
    rowValues2[index] = ROW2COL5;
    index++;

    String sqlCmd = SQLCMD;
    Object[] bindArgs = BINDARGS;
    String[] orderByArgs = ORDERBYELEM;
    String[] orderByDirections = ORDERBYDIR;
    String[] primaryKey = PRIMARY_KEY;

    BaseTable table = new BaseTable(primaryKey, elementKeyForIndex, null, NUM_ROWS);

    Row row1 = new Row(rowValues1, table);
    table.addRow(row1);

    Row row2 = new Row(rowValues2, table);
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

    BaseTable t = BaseTable.CREATOR.createFromParcel(p);

    /*
     * Test that the data survived
     */

    // sql command
    assertEquals(table.getSqlCommand(), t.getSqlCommand());

    // primary key
    String[] pa = table.getPrimaryKey();
    String[] pb = t.getPrimaryKey();
    if ( pa != null && pb != null ) {
      assertEquals(pa.length, pb.length);
      for ( index = 0 ; index < pa.length ; ++index ) {
        assertEquals(pa[index], pb[index]);
      }
    } else {
      assertNull(pa);
      assertNull(pb);
    }

    // verify rows
    assertEquals(NUM_ROWS, table.getNumberOfRows());
    assertEquals(NUM_ROWS, t.getNumberOfRows());

    Row rat1 = table.getRowAtIndex(0);
    Row rat2 = t.getRowAtIndex(0);
    Row rbt1 = table.getRowAtIndex(1);
    Row rbt2 = t.getRowAtIndex(1);

    for (int j = 0; j < TABLE_WIDTH; j++) {
      assertEquals(rat1.getRawStringByIndex(j), rat2.getRawStringByIndex(j));
      assertEquals(rbt2.getRawStringByIndex(j), rbt1.getRawStringByIndex(j));
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testOdkTableCreationValidationTable() throws IOException {

    String[] primaryKey = PRIMARY_KEY;

    BaseTable table = new BaseTable(primaryKey, null, null, NUM_ROWS);
  }

}
