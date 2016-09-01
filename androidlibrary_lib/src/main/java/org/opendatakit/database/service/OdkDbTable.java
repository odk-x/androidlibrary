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
package org.opendatakit.database.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.opendatakit.database.service.queries.OdkDbQuery;
import org.opendatakit.database.service.queries.OdkDbResumableQuery;
import org.opendatakit.database.service.queries.OdkDbSimpleQuery;
import org.opendatakit.database.service.queries.QueryBounds;
import org.opendatakit.database.utilities.OdkMarshallUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the generic parent table used inside the service. All other table objects should
 * inherit from this one
 */
public class OdkDbTable implements Parcelable {


  static final String TAG = OdkDbTable.class.getSimpleName();

  /**
   * The table data
   */
  protected final ArrayList<OdkDbRow> mRows;

  /**
   * The parent/container table
   */
  protected ParentTable mParent;

  /**
   * True if the user has the permissions to create a row.
   * Defaults to false.
   */
  protected boolean mEffectiveAccessCreateRow;

  /**
   * The query performed
   */
  protected OdkDbResumableQuery mQuery;

  /**
   * The fields that make up the primary key
   */
  protected final String[] mPrimaryKey;

  /**
   * Map indices to column names
   */
  protected final String[] mElementKeyForIndex;

  /**
   * Map column names to indices
   */
  protected final Map<String, Integer> mElementKeyToIndex;

  /**
   * Construct the table
   *
   * @param query the query that produced the results
   * @param elementKeyForIndex map indices to column names in row data
   * @param elementKeyToIndex map column names to indices in row data
   * @param rowCount the capacity of the table
   */
  public OdkDbTable(OdkDbResumableQuery query, String[] elementKeyForIndex,
      Map<String, Integer> elementKeyToIndex, String[] primaryKey, Integer rowCount) {
    this.mParent = null;

    this.mEffectiveAccessCreateRow = false;

    this.mQuery = query;

    if (elementKeyForIndex == null) {
      throw new IllegalStateException("elementKeyForIndex cannot be null");
    }
    this.mElementKeyForIndex = elementKeyForIndex;

    if (elementKeyToIndex == null) {
      this.mElementKeyToIndex = generateElementKeyToIndex();
    } else {
      this.mElementKeyToIndex = elementKeyToIndex;
    }

    this.mPrimaryKey = primaryKey;

    int numRows = 0;
    if (rowCount != null) {
      numRows = rowCount.intValue();
    }
    this.mRows = new ArrayList<>(numRows);
  }

  /**
   * Construct the table
   *
   * @param elementKeyForIndex map indices to column names in row data
   * @param elementKeyToIndex map column names to indices in row data
   * @param rowCount the capacity of the table
   */
  public OdkDbTable(String[] primaryKey, String[] elementKeyForIndex, Map<String, Integer> elementKeyToIndex,
     Integer rowCount) {
    this(null, elementKeyForIndex, elementKeyToIndex, primaryKey, rowCount);
  }

  public OdkDbTable(OdkDbTable table, List<Integer> indexes) {
    mRows = new ArrayList<>(indexes.size());
    for (int i = 0; i < indexes.size(); ++i) {
      OdkDbRow r = table.getRowAtIndex(indexes.get(i));
      mRows.add(r);
    }

    this.mEffectiveAccessCreateRow = table.mEffectiveAccessCreateRow;
    this.mQuery = table.mQuery;
    this.mPrimaryKey = table.mPrimaryKey;
    this.mElementKeyForIndex = table.mElementKeyForIndex;
    this.mElementKeyToIndex = table.generateElementKeyToIndex();
    this.mParent = null; // Set this with register
  }


  public OdkDbTable(Parcel in) {
    int dataCount;

    try {
      byte eacr = in.readByte();
      mEffectiveAccessCreateRow = (eacr == 0) ? false : true;
      mPrimaryKey = OdkMarshallUtil.unmarshallStringArray(in);
      mElementKeyForIndex = OdkMarshallUtil.unmarshallStringArray(in);
      mElementKeyToIndex = generateElementKeyToIndex();
    } catch (Throwable t) {
      Log.e(TAG, t.getMessage());
      Log.e(TAG, Log.getStackTraceString(t));
      throw t;
    }

    dataCount = in.readInt();
    mRows = new ArrayList<>(dataCount);
    for(; dataCount > 0; dataCount--) {
      OdkDbRow r = new OdkDbRow(in, this);
      mRows.add(r);
    }

    // The parent and the query are not parceled
    this.mParent = null;
    this.mQuery = null;
  }

  public void setEffectiveAccessCreateRow(boolean canCreateRow) {
    this.mEffectiveAccessCreateRow = canCreateRow;
  }

  public boolean getEffectiveAccessCreateRow() {
    return mEffectiveAccessCreateRow;
  }

  public void setQuery(OdkDbResumableQuery query) {
    this.mQuery = query;
  }

  public OdkDbResumableQuery getQuery() {
    return mQuery;
  }

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

  public int getEndIndex() {
    return getStartIndex() + getNumberOfRows() - 1;
  }

   /**
    * Return a resumable query that will perform the same query but adjust the query bounds. The
    * bounds will be moved forward through the row indeces as determined by the ORDER BY row
    * ordering.
    *
    * @param limit the maximum number of rows to retrieve
    * @return
    */
   public OdkDbResumableQuery resumeQueryForward(int limit) {
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
   *
   * The results are earlier in the ordering, but they are still conform to the ORDER BY
   * arguments (they are not reversed).
   *
   * @param limit the maximum number of rows to retrieve
   * @return
   */
  public OdkDbResumableQuery resumeQueryBackward(int limit) {
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

  public void addRow(OdkDbRow row) {
    mRows.add(row);
  }

  public OdkDbRow getRowAtIndex(int index) {
    return this.mRows.get(index);
  }

  public List<OdkDbRow> getRows() {
    return Collections.unmodifiableList(mRows);
  }

  public String getElementKey(int colNum) {
    return mElementKeyForIndex[colNum];
  }

  public Integer getColumnIndexOfElementKey(String elementKey) {
    return mElementKeyToIndex.get(elementKey);
  }

  public String getSqlCommand() {
    return (mQuery != null ? mQuery.getSqlCommand() : null);
  }

  public BindArgs getSqlBindArgs() {
    return (mQuery != null ? mQuery.getSqlBindArgs() : null);
  }

  public String[] getPrimaryKey() {
    if (mPrimaryKey == null) {
      return null;
    }
    return mPrimaryKey.clone();
  }

  public String[] getElementKeyForIndex() {
    return mElementKeyForIndex.clone();
  }

  /**
   * This is EXPENSIVE!!!  Used only for JS return value
   * Do not use for anything else!!!!
   *
   * @return copy of the map. Used for JS return value
   */
  public Map<String, Integer> getElementKeyToIndex() {
    return new HashMap<>(this.mElementKeyToIndex);
  }

  private Map<String, Integer> generateElementKeyToIndex() {
    Map<String, Integer> elementKeyToIndex = new HashMap<>();

    for (int i = 0; i < mElementKeyForIndex.length; i++) {
      elementKeyToIndex.put(mElementKeyForIndex[i], i);
    }
    return elementKeyToIndex;
  }

  public int getWidth() {
    return mElementKeyForIndex.length;
  }

  public int getNumberOfRows() {
    return this.mRows.size();
  }

  public void registerParentTable(ParentTable table) {
    this.mParent = table;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    // Do not marshal the parent table reference. The parent table will reregister itself when
    // it unmarshals.

    // Do not marshall the query, it will be reregistered on the other side

    // Do not marshall mElementKeyToIndex; just rebuilt it from the mElementKeyForIndex

    try {
      out.writeByte(mEffectiveAccessCreateRow ? ((byte) 1) : ((byte) 0));
      OdkMarshallUtil.marshallStringArray(out, mPrimaryKey);
      OdkMarshallUtil.marshallStringArray(out, mElementKeyForIndex);
    } catch (Throwable t) {
      Log.e(TAG, t.getMessage());
      Log.e(TAG, Log.getStackTraceString(t));
      throw t;
    }

    out.writeInt(mRows.size());
    for (OdkDbRow r : mRows) {
      r.writeToParcel(out, flags);
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Parcelable.Creator<OdkDbTable> CREATOR = new Parcelable.Creator<OdkDbTable>() {
    public OdkDbTable createFromParcel(Parcel in) {
      return new OdkDbTable(in);
    }

    public OdkDbTable[] newArray(int size) {
      return new OdkDbTable[size];
    }
  };

}
