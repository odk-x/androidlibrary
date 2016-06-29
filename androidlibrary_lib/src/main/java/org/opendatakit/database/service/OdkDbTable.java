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

  static final String TAG = OdkDbTable.class.getSimpleName();

  /**
   * The table data
   */
  private final ArrayList<OdkDbRow> mRows;

  /**
   * The SQL command used to retrieve the data
   */
  private final String mSqlCmd;

  /**
   * The SELECT arguments for the SQL command
   */
  private final String[] mBindArgs;

  /**
   * The ORDER BY arguments
   */
  private final String[] mOrderByElementKey;

  /**
   * The direction of each ORDER BY argument
   */
  private final String[] mOrderByDirection;

  /**
   * The fields that make up the primary key
   */
  private final String[] mPrimaryKey;

  /**
   * Map column names to indices in rowData
   */
  private final Map<String, Integer> mElementKeyToIndex;

  /**
   * Construct the table
   *
   * @param sqlCmd the SQL command use to retrieve the data
   * @param bindArgs the SELECT arguments for the SQL command
   * @param orderByDirection the ORDER BY arguments
   * @param orderByElementKey the direction of each ORDER BY argument
   * @param primaryKey the fields that make up the primary key
   * @param elementKeyToIndex map column names to indices in row data
   * @param rowCount the capacity of the table
   */
  public OdkDbTable(String sqlCmd, String[] bindArgs, String[] orderByDirection,
      String[] orderByElementKey, String[] primaryKey, Map<String, Integer> elementKeyToIndex,
      Integer rowCount) {
    this.mSqlCmd = sqlCmd;
    this.mBindArgs = bindArgs;
    this.mOrderByDirection = orderByDirection;
    this.mOrderByElementKey = orderByElementKey;
    this.mPrimaryKey = primaryKey;
    this.mElementKeyToIndex = elementKeyToIndex;

    int numRows = 0;
    if (rowCount != null) {
      numRows = rowCount.intValue();
    }
    this.mRows = new ArrayList<OdkDbRow>(numRows);
  }

  public OdkDbTable(Parcel in) {
    int dataCount = 0;

    mSqlCmd = in.readString();

    mBindArgs = unmarshallStringArray(in);
    mOrderByDirection = unmarshallStringArray(in);
    mOrderByElementKey = unmarshallStringArray(in);
    mPrimaryKey = unmarshallStringArray(in);

    dataCount = in.readInt();
    mRows = new ArrayList<>(dataCount);
    for(; dataCount > 0; dataCount--) {
      OdkDbRow r = new OdkDbRow(in, this);
      mRows.add(r);
    }

    dataCount = in.readInt();
    this.mElementKeyToIndex = new HashMap<String, Integer>();
    for (int i = 0; i < dataCount; i++) {
      String key = in.readString();
      Integer value = in.readInt();
      mElementKeyToIndex.put(key, value);
    }
  }

  public void addRow(OdkDbRow row) {
    mRows.add(row);
  }

  public OdkDbRow getRowAtIndex(int index) {
    return this.mRows.get(index);
  }

  public Integer getColumnIndexOfElementKey(String elementKey) {
    return this.mElementKeyToIndex.get(elementKey);
  }

  public String getSqlCommand() {
    return mSqlCmd;
  }

  public String[] getSqlBindArgs() {
    if (mBindArgs == null ) {
      return null;
    }
    return mBindArgs.clone();
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

  public int getWidth() {
    return mElementKeyToIndex.size();
  }

  public int getNumberOfRows() {
    return this.mRows.size();
  }

  public OdkDbRow getRow(String[] primaryKeyVals) {
    for (OdkDbRow row: mRows) {
      if (row.matchPrimaryKey(primaryKeyVals)) {
        return row;
      }
    }

    return null;
  }

  public Set<String> getCols() {
    return mElementKeyToIndex.keySet();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    String[] emptyString = {};

    out.writeString(mSqlCmd);

    marshallStringArray(out, mBindArgs);
    marshallStringArray(out, mOrderByDirection);
    marshallStringArray(out, mOrderByElementKey);
    marshallStringArray(out, mPrimaryKey);

    out.writeInt(mRows.size());
    for (OdkDbRow r : mRows) {
      r.writeToParcel(out, flags);
    }

    if (mElementKeyToIndex == null) {
      out.writeInt(0);
    } else {
      out.writeInt(mElementKeyToIndex.size());

      for (Map.Entry<String, Integer> entry : mElementKeyToIndex.entrySet()) {
        out.writeString(entry.getKey());
        out.writeInt(entry.getValue());
      }
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
