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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is the generic parent table used inside the service. All other table objects should
 * inherit from this one
 */
public class OdkDbTable implements Parcelable {

  // TODO: Handle possible null values? Or make sure defaults are set up
  // TODO: Remove any APIs we aren't using
  // TODO: Make sure removal of mElementKeyToIndex was completed and the alternative is used
  // correctly

  static final String TAG = OdkDbTable.class.getSimpleName();

  /**
   * The table data
   */
  protected final ArrayList<OdkDbRow> mRows;

  /**
   * The SQL command used to retrieve the data
   */
  protected final String mSqlCommand;

  /**
   * The SELECT arguments for the SQL command
   */
  protected final String[] mSqlBindArgs;

  /**
   * The ORDER BY arguments
   */
  protected final String[] mOrderByElementKey;

  /**
   * The direction of each ORDER BY argument
   */
  protected final String[] mOrderByDirection;

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
   * @param sqlBindArgs the SELECT arguments for the SQL command
   * @param orderByDirection the ORDER BY arguments
   * @param orderByElementKey the direction of each ORDER BY argument
   * @param primaryKey the fields that make up the primary key
   * @param elementKeyForIndex map indices to column names in row data
   * @param rowCount the capacity of the table
   */
  public OdkDbTable(String sqlCommand, String[] sqlBindArgs, String[] orderByDirection,
      String[] orderByElementKey, String[] primaryKey, String[] elementKeyForIndex,
      Integer rowCount) {
    this.mSqlCommand = sqlCommand;
    this.mSqlBindArgs = sqlBindArgs;
    this.mOrderByDirection = orderByDirection;
    this.mOrderByElementKey = orderByElementKey;
    this.mPrimaryKey = primaryKey;

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

  public OdkDbTable(String sqlCommand, String[] sqlBindArgs, String[] elementkeyForIndex,
      Integer rowCount) {
    this(sqlCommand, sqlBindArgs, null, null, null, elementkeyForIndex, rowCount);
  }

  public OdkDbTable(Parcel in) {
    int dataCount = 0;

    mSqlCommand = in.readString();

    mSqlBindArgs = unmarshallStringArray(in);
    mOrderByDirection = unmarshallStringArray(in);
    mOrderByElementKey = unmarshallStringArray(in);
    mPrimaryKey = unmarshallStringArray(in);
    mElementKeyForIndex = unmarshallStringArray(in);

    dataCount = in.readInt();
    mRows = new ArrayList<>(dataCount);
    for(; dataCount > 0; dataCount--) {
      OdkDbRow r = new OdkDbRow(in, this);
      mRows.add(r);
    }
  }

  public void addRow(OdkDbRow row) {
    mRows.add(row);
  }

  public OdkDbRow getRowAtIndex(int index) {
    return this.mRows.get(index);
  }

  public String getElementKey(int colNum) {
    return mElementKeyForIndex[colNum];
  }

  public String getSqlCommand() {
    return mSqlCommand;
  }

  public String[] getSqlBindArgs() {
    if (mSqlBindArgs == null ) {
      return null;
    }
    return mSqlBindArgs.clone();
  }

  public String[] getOrderByElementKey() {
    if (mOrderByElementKey == null ) {
      return null;
    }
    return mOrderByElementKey.clone();
  }

  public String[] getOrderByDirection() {
    if (mOrderByDirection == null ) {
      return null;
    }
    return mOrderByDirection.clone();
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

  public int getWidth() {
    return mElementKeyForIndex.length;
  }

  public int getNumberOfRows() {
    return this.mRows.size();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(mSqlCommand);

    marshallStringArray(out, mSqlBindArgs);
    marshallStringArray(out, mOrderByDirection);
    marshallStringArray(out, mOrderByElementKey);
    marshallStringArray(out, mPrimaryKey);
    marshallStringArray(out, mElementKeyForIndex);

    out.writeInt(mRows.size());
    for (OdkDbRow r : mRows) {
      r.writeToParcel(out, flags);
    }
  }

  private void marshallStringArray(Parcel out, String[] toMarshall) {
    if (toMarshall == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(toMarshall.length);
      out.writeStringArray(toMarshall);
    }
  }

  private String[] unmarshallStringArray(Parcel in) {
    String[] result = null;

    int dataCount = in.readInt();
    if (dataCount < 0) {
      return null;
    } else {
      result = new String[dataCount];
      in.readStringArray(result);
    }

    return result;
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
