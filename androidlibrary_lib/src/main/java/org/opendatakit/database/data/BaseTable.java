/*
 * Copyright (C) 2016 University of Washington
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
package org.opendatakit.database.data;

import android.os.Parcel;
import android.os.Parcelable;
import org.opendatakit.database.queries.BindArgs;
import org.opendatakit.database.queries.QueryBounds;
import org.opendatakit.database.queries.ResumableQuery;
import org.opendatakit.database.utilities.MarshallUtil;
import org.opendatakit.logging.WebLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the generic parent table used inside the service. All other table objects should
 * inherit from this one
 */
public class BaseTable implements Parcelable {

  public static final Parcelable.Creator<BaseTable> CREATOR = new Parcelable.Creator<BaseTable>() {
    public BaseTable createFromParcel(Parcel in) {
      return new BaseTable(in);
    }

    public BaseTable[] newArray(int size) {
      return new BaseTable[size];
    }
  };
  private static final String TAG = BaseTable.class.getSimpleName();
  /**
   * These eight properties all have getters and setters, so I'm making them private - 06/23/17
   * The table data
   */
  private final ArrayList<Row> mRows;
  /**
   * The fields that make up the primary key
   */
  private final String[] mPrimaryKey;
  /**
   * Map indices to column names
   */
  private final String[] mElementKeyForIndex;
  /**
   * Map column names to indices
   */
  private final Map<String, Integer> mElementKeyToIndex;
  /**
   * The parent/container table
   */
  private WrapperTable mWrapper;
  /**
   * True if the user has the permissions to create a row.
   * Defaults to false.
   */
  private boolean mEffectiveAccessCreateRow;
  /**
   * The query performed
   */
  private ResumableQuery mQuery;

  /**
   * Construct the table
   *
   * @param query              the query that produced the results
   * @param elementKeyForIndex map indices to column names in row data
   * @param elementKeyToIndex  map column names to indices in row data
   * @param rowCount           the capacity of the table
   */
  public BaseTable(ResumableQuery query, String[] elementKeyForIndex,
      Map<String, Integer> elementKeyToIndex, String[] primaryKey, Integer rowCount) {
    mWrapper = null;

    mEffectiveAccessCreateRow = false;

    mQuery = query;

    if (elementKeyForIndex == null) {
      throw new IllegalArgumentException("elementKeyForIndex cannot be null");
    }
    mElementKeyForIndex = elementKeyForIndex;

    if (elementKeyToIndex == null) {
      mElementKeyToIndex = generateElementKeyToIndex();
    } else {
      mElementKeyToIndex = elementKeyToIndex;
    }

    mPrimaryKey = primaryKey;

    int numRows = 0;
    if (rowCount != null) {
      numRows = rowCount;
    }
    mRows = new ArrayList<>(numRows);
  }

  /**
   * Construct the table
   *
   * @param elementKeyForIndex map indices to column names in row data
   * @param elementKeyToIndex  map column names to indices in row data
   * @param rowCount           the capacity of the table
   */
  public BaseTable(String[] primaryKey, String[] elementKeyForIndex,
      Map<String, Integer> elementKeyToIndex, Integer rowCount) {
    this(null, elementKeyForIndex, elementKeyToIndex, primaryKey, rowCount);
  }

  public BaseTable(BaseTable table, List<Integer> indexes) {
    mRows = new ArrayList<>(indexes.size());
    for (int i = 0; i < indexes.size(); ++i) {
      Row r = table.getRowAtIndex(indexes.get(i));
      mRows.add(r);
    }

    mEffectiveAccessCreateRow = table.mEffectiveAccessCreateRow;
    mQuery = table.mQuery;
    mPrimaryKey = table.mPrimaryKey;
    mElementKeyForIndex = table.mElementKeyForIndex;
    mElementKeyToIndex = table.mElementKeyToIndex;
    mWrapper = null; // Set this with register
  }

  public BaseTable(Parcel in) {
    int dataCount;

    try {
      byte eacr = in.readByte();
      mEffectiveAccessCreateRow = eacr != 0;
      mPrimaryKey = MarshallUtil.unmarshallStringArray(in);
      mElementKeyForIndex = MarshallUtil.unmarshallStringArray(in);
      mElementKeyToIndex = generateElementKeyToIndex();
    } catch (Throwable t) {
      WebLogger.getContextLogger().e(TAG, t.getMessage());
      WebLogger.getContextLogger().printStackTrace(t);
      throw t;
    }

    dataCount = in.readInt();
    mRows = new ArrayList<>(dataCount);
    for (; dataCount > 0; dataCount--) {
      //noinspection ThisEscapedInObjectConstruction
      Row r = new Row(in, this);
      mRows.add(r);
    }

    // The parent and the query are not parceled
    mWrapper = null;
    mQuery = null;
  }

  /**
   * Used in ExecutorProcessor and UserTable
   *
   * @return whether we have access to create a row
   */
  @SuppressWarnings("WeakerAccess")
  public boolean getEffectiveAccessCreateRow() {
    return mEffectiveAccessCreateRow;
  }

  /**
   * Used in ODKDatabaseImplUtils
   *
   * @param canCreateRow whether we have access to create a row
   */
  @SuppressWarnings("unused")
  public void setEffectiveAccessCreateRow(boolean canCreateRow) {
    mEffectiveAccessCreateRow = canCreateRow;
  }

  public ResumableQuery getQuery() {
    return mQuery;
  }

  public void setQuery(ResumableQuery query) {
    mQuery = query;
  }

  /**
   * Only used in this method, but public anyways
   *
   * @return the start index for the table
   */
  public int getStartIndex() {
    if (mQuery == null || mQuery.getSqlQueryBounds() == null) {
      return 0;
    }

    QueryBounds bounds = mQuery.getSqlQueryBounds();

    if (bounds.mOffset <= 0) {
      return 0;
    }

    return bounds.mOffset;
  }

  /**
   * Only used in this method, but public anyways
   *
   * @return the end index for the table
   */
  public int getEndIndex() {
    return getStartIndex() + getNumberOfRows() - 1;
  }

  /**
   * Return a resumable query that will perform the same query but adjust the query bounds. The
   * bounds will be moved forward through the row indeces as determined by the ORDER BY row
   * ordering.
   * Used in UserTable, but that's it, no reason for it to be public
   *
   * @param limit the maximum number of rows to retrieve
   * @return A new query with different query bounds
   */
  public ResumableQuery resumeQueryForward(int limit) {
    if (mQuery == null) {
      return null;
    }

    int numCurrentRows = getNumberOfRows();
    if (numCurrentRows == 0 || numCurrentRows < mQuery.getSqlLimit()) {
      // We have reached the end of the query results
      return null;
    }

    mQuery.setSqlLimit(limit);
    mQuery.setSqlOffset(getEndIndex() + 1);

    return mQuery;
  }

  /**
   * Return a resumable query that will perform the same query but adjust the query bounds. The
   * bounds will be moved backward through the row indeces as determined by the ORDER BY row
   * ordering.
   * <p>
   * The results are earlier in the ordering, but they are still conform to the ORDER BY
   * arguments (they are not reversed).
   * Used in UserTable, but that's it, no reason for it to be public
   *
   * @param limit the maximum number of rows to retrieve
   * @return A new query with different query bounds
   */
  public ResumableQuery resumeQueryBackward(int limit) {
    if (mQuery == null) {
      return null;
    }

    int startIndex = getStartIndex();
    if (startIndex == 0) {
      // We are at the beginning of the results; we cannot go back farther
      return null;
    }

    // If we are asked to retrieve more rows than exist when counting back to the beginning,
    // shrink the bounds accordingly
    if (limit > startIndex) {
      limit = startIndex;
      startIndex = 0;
    }

    mQuery.setSqlLimit(limit);
    mQuery.setSqlOffset(startIndex - limit);

    return mQuery;
  }

  public void addRow(Row row) {
    mRows.add(row);
  }

  public Row getRowAtIndex(int index) {
    boolean inBounds = (index >= 0) && (index < mRows.size());
    return inBounds ? mRows.get(index) : null;
  }

  /**
   * Return the list of rows in the table.
   *
   * @return the rows in the table
   */
  public List<Row> getRows() {
    // This may have broken something. If it did, just change it back to "return mRows;"
    return Collections.unmodifiableList(mRows);
  }

  public String getElementKey(int colNum) {
    return mElementKeyForIndex[colNum];
  }

  /**
   * Gets the index of a column
   * Used in UserTable, Row, no need for it to be public
   * @param elementKey the column name
   * @return the index of that column name
   */
  public Integer getColumnIndexOfElementKey(String elementKey) {
    return mElementKeyToIndex.get(elementKey);
  }

  public String getSqlCommand() {
    return mQuery != null ? mQuery.getSqlCommand() : null;
  }

  /**
   * Gets the bindargs from the sql statement
   * Used in UserTable only, no need for it to be public
   * @return the bindargs
   */
  public BindArgs getSqlBindArgs() {
    return mQuery != null ? mQuery.getSqlBindArgs() : null;
  }

  /**
   * Get the list of columns that comprise the primary key for the table.
   * Caller should treat this as a read-only array. I.e., don't change it.
   * This method is completely unused
   *
   * @return a copy of the primary key
   */
  public String[] getPrimaryKey() {
    if (mPrimaryKey == null) {
      return null;
    }
    return mPrimaryKey.clone();
  }

  /**
   * Get the list of column names in the table.
   * Used in AggregateSynchronizer, others
   *
   * @return a copy of the array of column names
   */
  @SuppressWarnings("WeakerAccess")
  public String[] getElementKeyForIndex() {
    return mElementKeyForIndex.clone();
  }

  /**
   * This is EXPENSIVE!!!  Used only for JS return value
   * Do not use for anything else!!!!
   * Used in ExecutorProcessor
   *
   * @return copy of the map. Used for JS return value
   */
  @SuppressWarnings("WeakerAccess")
  public Map<String, Integer> getElementKeyToIndex() {
    return new HashMap<>(mElementKeyToIndex);
  }

  private Map<String, Integer> generateElementKeyToIndex() {
    Map<String, Integer> elementKeyToIndex = new HashMap<>();

    for (int i = 0; i < mElementKeyForIndex.length; i++) {
      elementKeyToIndex.put(mElementKeyForIndex[i], i);
    }
    return elementKeyToIndex;
  }

  /**
   * Returns the number of columns
   * Used in ExecutorProcessor
   * @return number of columns
   */
  @SuppressWarnings("WeakerAccess")
  public int getWidth() {
    return mElementKeyForIndex.length;
  }

  public int getNumberOfRows() {
    return mRows.size();
  }

  /**
   * Used in UserTable, nothing else, no need to be public
   * @param table the table to set the wrapper to
   */
  public void registerWrapperTable(WrapperTable table) {
    mWrapper = table;
  }

  /**
   * Actually used nowhere
   * @return the wrapper
   */
  public WrapperTable getWrapperTable() {
    return mWrapper;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    // Do not marshal the parent table reference. The parent table will reregister itself when
    // it unmarshals.

    // Do not marshall the query, it will be reregistered on the other side

    // Do not marshall mElementKeyToIndex; just rebuilt it from the mElementKeyForIndex

    try {
      out.writeByte(mEffectiveAccessCreateRow ? (byte) 1 : (byte) 0);
      MarshallUtil.marshallStringArray(out, mPrimaryKey);
      MarshallUtil.marshallStringArray(out, mElementKeyForIndex);
    } catch (Throwable t) {
      WebLogger.getContextLogger().e(TAG, t.getMessage());
      WebLogger.getContextLogger().printStackTrace(t);
      throw t;
    }

    out.writeInt(mRows.size());
    for (Row r : mRows) {
      r.writeToParcel(out, flags);
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

}
