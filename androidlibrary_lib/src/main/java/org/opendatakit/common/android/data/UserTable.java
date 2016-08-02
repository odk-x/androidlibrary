/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.android.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.ElementType;
import org.opendatakit.common.android.provider.DataTableColumns;
import org.opendatakit.common.android.utilities.DataUtil;
import org.opendatakit.common.android.utilities.ODKFileUtils;
import org.opendatakit.common.android.utilities.OdkDbQueryUtil;
import org.opendatakit.common.android.utilities.WebLogger;
import org.opendatakit.database.service.OdkDbRow;
import org.opendatakit.database.service.OdkDbTable;
import org.opendatakit.database.service.ParentTable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a table. This can be conceptualized as a list of rows.
 * Each row comprises the user-defined columns, or data, as well as the
 * ODKTables-specified metadata.
 * <p>
 * This should be considered an immutable class.
 *
 * @author unknown
 * @author sudar.sam@gmail.com
 *
 */
public class UserTable implements Parcelable, ParentTable{

  static final String TAG = UserTable.class.getSimpleName();

  private static final String[] primaryKey = {"rowId", "savepoint_timestamp"};

  private final OdkDbTable mBaseTable;

  private final OrderedColumns mColumnDefns;

  private final String mSqlWhereClause;
  private final String[] mSqlGroupByArgs;
  private final String mSqlHavingClause;

  private final String[] mAdminColumnOrder;

  private final Map<String, Integer> mElementKeyToIndex;

  public UserTable(UserTable table, List<Integer> indexes) {
    this.mBaseTable = new OdkDbTable(table.mBaseTable, indexes);
    this.mColumnDefns = table.mColumnDefns;
    this.mSqlWhereClause = table.mSqlWhereClause;
    this.mSqlGroupByArgs = table.mSqlGroupByArgs;
    this.mSqlHavingClause = table.mSqlHavingClause;
    this.mAdminColumnOrder = table.mAdminColumnOrder;
    this.mElementKeyToIndex = table.mElementKeyToIndex;
  }

  public UserTable(OdkDbTable baseTable, OrderedColumns columnDefns, String sqlWhereClause,
      String[] sqlGroupByArgs, String sqlHavingClause, String[] adminColumnOrder, Map<String,
      Integer> elementKeyToIndex) {
    this.mBaseTable = baseTable;
    baseTable.registerParentTable(this);

    this.mColumnDefns = columnDefns;
    this.mSqlWhereClause = sqlWhereClause;
    this.mSqlGroupByArgs = sqlGroupByArgs;
    this.mSqlHavingClause = sqlHavingClause;
    this.mAdminColumnOrder = adminColumnOrder;

    if (elementKeyToIndex == null) {
      this.mElementKeyToIndex = mBaseTable.generateElementKeyToIndex();
    } else {
      this.mElementKeyToIndex = elementKeyToIndex;
    }
  }

  public UserTable(OrderedColumns columnDefns, String sqlWhereClause,
      String[] sqlSelectionArgs, String[] sqlGroupByArgs, String sqlHavingClause,
      String[] sqlOrderByElementKeys, String[] sqlOrderByDirections, String[] adminColumnOrder,
      Map<String, Integer> elementKeyToIndex, String[] elementKeyForIndex, Integer rowCount) {

    this.mBaseTable = new OdkDbTable(OdkDbQueryUtil
        .buildSqlStatement(columnDefns.getTableId(), sqlWhereClause, sqlGroupByArgs, sqlHavingClause,
            sqlOrderByElementKeys, sqlOrderByDirections), sqlSelectionArgs, sqlOrderByElementKeys,
        sqlOrderByDirections, primaryKey, elementKeyForIndex, rowCount);

    this.mColumnDefns = columnDefns;
    this.mSqlWhereClause = sqlWhereClause;
    this.mSqlGroupByArgs = sqlGroupByArgs;
    this.mSqlHavingClause = sqlHavingClause;
    this.mAdminColumnOrder = adminColumnOrder;

    if (elementKeyToIndex == null) {
      this.mElementKeyToIndex = mBaseTable.generateElementKeyToIndex();
    } else {
      this.mElementKeyToIndex = elementKeyToIndex;
    }

  }

  public OdkDbTable getBaseTable() {
    return mBaseTable;
  }

  public void addRow(OdkDbRow row) {
    mBaseTable.addRow(row);
  }

  public String getAppName() {
    return mColumnDefns.getAppName();
  }

  public String getTableId() {
    return mColumnDefns.getTableId();
  }

  public OrderedColumns getColumnDefinitions() {
    return mColumnDefns;
  }

  public OdkDbRow getRowAtIndex(int index) {
    return mBaseTable.getRowAtIndex(index);
  }

  public Integer getColumnIndexOfElementKey(String elementKey) {
    return this.mElementKeyToIndex.get(elementKey);
  }

  /**
   * This is EXPENSIVE!!!  Used only for JS return value
   * Do not use for anything else!!!!
   *
   * @return copy of the map. Used for JS return value
   */
  public Map<String, Integer> getElementKeyMap() {
    HashMap<String, Integer> copyMap = new HashMap<String, Integer>(this.mElementKeyToIndex);
    return copyMap;
  }

  public String getElementKey(int colNum) {
    return mBaseTable.getElementKey(colNum);
  }

  public String getWhereClause() {
    return mSqlWhereClause;
  }

  public String[] getSelectionArgs() {
    return mBaseTable.getSqlSelectionArgs();
  }

  /**
   * True if the table has a group-by clause in its query
   *
   * @return
   */
  public boolean isGroupedBy() {
    return mSqlGroupByArgs != null && mSqlGroupByArgs.length != 0;
  }

  public String[] getGroupByArgs() {
    if (mSqlGroupByArgs == null) {
      return null;
    }
    return mSqlGroupByArgs.clone();
  }

  public String getHavingClause() {
    return mSqlHavingClause;
  }

  public String[] getOrderByElementKeys() {
    return mBaseTable.getOrderByElementKeys();
  }

  public String[] getOrderByDirections() {
    return mBaseTable.getOrderByDirections();
  }

  public int getWidth() {
    return mBaseTable.getWidth();
  }

  public int getNumberOfRows() {
    return mBaseTable.getNumberOfRows();
  }

  /**
   * Return the String representing the contents of the column represented by
   * the passed in elementKey. This can be either the element key of a
   * user-defined column or a ODKTables-specified metadata column.
   * <p>
   * Null values are returned as nulls.
   *
   * @param rowIndex
   *          index of the relevant row
   * @param elementKey
   *          elementKey of data or metadata column
   * @return String representation of contents of column. Null values are
   *         returned as null. Note that boolean values are reported as "1" or "0"
   */
  public String getRawDataOrMetadataByElementKey(int rowIndex, String elementKey) {
    String result;
    OdkDbRow row = mBaseTable.getRowAtIndex(rowIndex);
    Integer cell = getColumnIndexOfElementKey(elementKey);
    if (cell == null) {
      WebLogger.getLogger(mColumnDefns.getAppName()).e(UserTable.TAG,
          "elementKey [" + elementKey + "] was not found in table");
      return null;
    }
    result = row.getDataByIndex(cell);
    if (result == null) {
      return null;
    }
    return result;
  }

  public String getRowId(int rowIndex) {
    return getRawDataOrMetadataByElementKey(rowIndex, DataTableColumns.ID);
  }

  public String getDisplayTextOfData(int rowIndex, ElementType type, String elementKey) {
    // TODO: share processing with CollectUtil.writeRowDataToBeEdited(...)
    String raw = getRawDataOrMetadataByElementKey(rowIndex, elementKey);
    String rowId = getRawDataOrMetadataByElementKey(rowIndex, DataTableColumns.ID);

    if (raw == null) {
      return null;
    } else if (raw.length() == 0) {
      throw new IllegalArgumentException("unexpected zero-length string in database! "
          + elementKey);
    }

    if (type == null) {
      return raw;
    } else if (type.getDataType() == ElementDataType.number && raw.indexOf('.') != -1) {
      // trim trailing zeros on numbers (leaving the last one)
      int lnz = raw.length() - 1;
      while (lnz > 0 && raw.charAt(lnz) == '0') {
        lnz--;
      }
      if (lnz >= raw.length() - 2) {
        // ended in non-zero or x0
        return raw;
      } else {
        // ended in 0...0
        return raw.substring(0, lnz + 2);
      }
    } else if (type.getDataType() == ElementDataType.rowpath) {
      File theFile = ODKFileUtils.getRowpathFile(getAppName(),
          getTableId(), rowId, raw);
      return theFile.getName();
    } else if (type.getDataType() == ElementDataType.configpath) {
      return raw;
    } else {
      return raw;
    }
  }

  public boolean hasCheckpointRows() {
    Integer cell = getColumnIndexOfElementKey(DataTableColumns.SAVEPOINT_TYPE);
    if (cell == null) {
      WebLogger.getLogger(mColumnDefns.getAppName()).e(UserTable.TAG,
          "elementKey [" + DataTableColumns.SAVEPOINT_TYPE + "] was not found in table");
      return false;
    }

    List<OdkDbRow> rows = mBaseTable.getRows();
    for (int i = 0; i < rows.size(); i++) {
      OdkDbRow row = rows.get(i);
      String type = row.getDataByIndex(cell);
      if (type == null || type.length() == 0) {
        return true;
      }
    }
    return false;
  }

  public boolean hasConflictRows() {
    Integer cell = getColumnIndexOfElementKey(DataTableColumns.CONFLICT_TYPE);
    if (cell == null) {
      WebLogger.getLogger(mColumnDefns.getAppName()).e(UserTable.TAG,
          "elementKey [" + DataTableColumns.CONFLICT_TYPE + "] was not found in table");
      return false;
    }

    List<OdkDbRow> rows = mBaseTable.getRows();
    for (int i = 0; i < rows.size(); i++) {
      OdkDbRow row = rows.get(i);
      String conflictType = row.getDataByIndex(cell);
      if (conflictType != null && conflictType.length() != 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Scan the rowIds to get the row number. As the rowIds are not sorted, this
   * is a potentially expensive operation, scanning the entire array, as well as
   * the cost of checking String equality. Should be used only when necessary.
   * <p>
   * Return -1 if the row Id is not found.
   *
   * @param rowId
   * @return
   */
  public int getRowNumFromId(String rowId) {
    Integer cell = getColumnIndexOfElementKey(DataTableColumns.ID);
    if (cell == null) {
      WebLogger.getLogger(mColumnDefns.getAppName()).e(UserTable.TAG,
          "elementKey [" + DataTableColumns.ID + "] was not found in table");
      return -1;
    }

    List<OdkDbRow> rows = mBaseTable.getRows();
    for (int i = 0; i < rows.size(); i++) {
      OdkDbRow row = rows.get(i);
      String readRowId = row.getDataByIndex(cell);
      if (readRowId.equals(rowId)) {
        return i;
      }
    }
    return -1;
  }

  public ParentTable getParentTable() {
    return this;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(mSqlWhereClause);
    DataUtil.marshallStringArray(out, mSqlGroupByArgs);
    out.writeString(mSqlHavingClause);
    this.mColumnDefns.writeToParcel(out, flags);
    DataUtil.marshallStringArray(out, mAdminColumnOrder);
    this.mBaseTable.writeToParcel(out, flags);
  }

  public UserTable(Parcel in) {
    this.mSqlWhereClause = in.readString();
    this.mSqlGroupByArgs = DataUtil.unmarshallStringArray(in);
    this.mSqlHavingClause = in.readString();
    this.mColumnDefns = new OrderedColumns(in);
    this.mAdminColumnOrder = DataUtil.unmarshallStringArray(in);
    this.mBaseTable = new OdkDbTable(in);
    this.mBaseTable.registerParentTable(this);
    this.mElementKeyToIndex = mBaseTable.generateElementKeyToIndex();
  }

  public static final Parcelable.Creator<UserTable> CREATOR = new Parcelable.Creator<UserTable>() {
    public UserTable createFromParcel(Parcel in) {
      return new UserTable(in);
    }

    public UserTable[] newArray(int size) {
      return new UserTable[size];
    }
  };

}
