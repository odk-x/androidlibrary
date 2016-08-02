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
import org.opendatakit.common.android.utilities.DataUtil;
import org.opendatakit.database.utilities.OdkMarshalUtil;

import java.util.*;

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
  protected ParentTable parent;

  /**
   * The SQL command used to retrieve the data
   */
  protected final String mSqlCommand;

  /**
   * The SELECT arguments for the SQL command
   */
  protected final String[] mSqlSelectionArgs;

  /**
   * The ORDER BY arguments
   */
  protected final String[] mOrderByElementKeys;

  /**
   * The direction of each ORDER BY argument
   */
  protected final String[] mOrderByDirections;

  /**
   * The fields that make up the primary key
   */
  protected final String[] mPrimaryKey;

  /**
   * Map indices to column names
   */
  protected final String[] mElementKeyForIndex;

  /**
   * Construct the table
   *
   * @param sqlCommand the SQL command use to retrieve the data
   * @param sqlSelectionArgs the SELECT arguments for the SQL command
   * @param orderByElementKeys the direction of each ORDER BY argument
   * @param orderByDirections the ORDER BY arguments
   * @param primaryKey the fields that make up the primary key
   * @param elementKeyForIndex map indices to column names in row data
   * @param rowCount the capacity of the table
   */
  public OdkDbTable(String sqlCommand, String[] sqlSelectionArgs, String[] orderByElementKeys,
      String[] orderByDirections, String[] primaryKey, String[] elementKeyForIndex,
      Integer rowCount) {

    this.mSqlCommand = sqlCommand;
    this.mSqlSelectionArgs = sqlSelectionArgs;
    this.mOrderByDirections = orderByDirections;
    this.mOrderByElementKeys = orderByElementKeys;
    this.mPrimaryKey = primaryKey;

    this.parent = null;

    if (elementKeyForIndex == null) {
      throw new IllegalStateException("elementKeyForIndex cannot be null");
    }
    this.mElementKeyForIndex = elementKeyForIndex;

    int numRows = 0;
    if (rowCount != null) {
      numRows = rowCount.intValue();
    }
    this.mRows = new ArrayList<OdkDbRow>(numRows);
  }

  public OdkDbTable(OdkDbTable table, List<Integer> indexes) {
    mRows = new ArrayList<OdkDbRow>(indexes.size());
    for (int i = 0; i < indexes.size(); ++i) {
      OdkDbRow r = table.getRowAtIndex(indexes.get(i));
      mRows.add(r);
    }
    this.mSqlCommand = table.mSqlCommand;
    this.mSqlSelectionArgs = table.mSqlSelectionArgs;
    this.mOrderByDirections = table.mOrderByDirections;
    this.mOrderByElementKeys = table.mOrderByDirections;
    this.mPrimaryKey = table.mPrimaryKey;
    this.mElementKeyForIndex = table.mElementKeyForIndex;
    this.parent = null; // Set this with register
  }


  public OdkDbTable(Parcel in) {
    int dataCount;

    mSqlCommand = in.readString();

    try {
      mSqlSelectionArgs = OdkMarshalUtil.unmarshallStringArray(in);
      mOrderByDirections = OdkMarshalUtil.unmarshallStringArray(in);
      mOrderByElementKeys = OdkMarshalUtil.unmarshallStringArray(in);
      mPrimaryKey = OdkMarshalUtil.unmarshallStringArray(in);
      mElementKeyForIndex = OdkMarshalUtil.unmarshallStringArray(in);
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

    this.parent = null; // Set this with register
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

  public String getSqlCommand() {
    return mSqlCommand;
  }

  public String[] getSqlSelectionArgs() {
    if (mSqlSelectionArgs == null ) {
      return null;
    }
    return mSqlSelectionArgs.clone();
  }

  public String[] getOrderByElementKeys() {
    if (mOrderByElementKeys == null ) {
      return null;
    }
    return mOrderByElementKeys.clone();
  }

  public String[] getOrderByDirections() {
    if (mOrderByDirections == null ) {
      return null;
    }
    return mOrderByDirections.clone();
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

  public Map<String, Integer> generateElementKeyToIndex() {
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
    this.parent = table;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    // Do not marshall the parent table reference. The parent table will reregister itself when
    // it unmarshalls.
    out.writeString(mSqlCommand);

    try {
      OdkMarshalUtil.marshallStringArray(out, mSqlSelectionArgs);
      OdkMarshalUtil.marshallStringArray(out, mOrderByDirections);
      OdkMarshalUtil.marshallStringArray(out, mOrderByElementKeys);
      OdkMarshalUtil.marshallStringArray(out, mPrimaryKey);
      OdkMarshalUtil.marshallStringArray(out, mElementKeyForIndex);
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
