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

import java.util.*;

/**
 * This represents a single row of data in a table.
 */
public final class OdkDbRow implements Parcelable {

  static final String TAG = OdkDbRow.class.getSimpleName();

  /**
   * The data held in the rows columns
   */
  private final String[] mRowData;

  /**
   * The table that the row belongs to
   */
  private final OdkDbTable mOwnerTable;

  /**
   * Construct the row.
   *
   * @param rowData the data in the row.
   * @param ownerTable the table that the row belongs to
   */
  public OdkDbRow(String[] rowData, OdkDbTable ownerTable) {
    this.mRowData = rowData;
    this.mOwnerTable = ownerTable;
  }

  /**
   * Unmarshal the row
   *
   * @param in the marshalled data
   * @param ownerTable the table that the row belongs to
   */
  public OdkDbRow(Parcel in, OdkDbTable ownerTable) {
    this.mOwnerTable = ownerTable;

    int dataCount = in.readInt();
    this.mRowData = new String[dataCount];
    in.readStringArray(mRowData);
  }

  /**
   * The owner table is not marshalled, so we cannot reconstruct the row without it
   *
   * @param in the marshalled data
   * @throws IllegalStateException always thrown
   */
  public OdkDbRow(Parcel in) {
    throw new IllegalStateException("OdkDbRow invalid constructor");
  }

  /**
   * Return the String representing the contents of the column represented by
   * the passed in elementKey.
   * <p>
   * Null values are returned as nulls.
   *
   * @param elementKey
   *          elementKey of the data
   * @return String representation of contents of column. Null values are
   *         returned as null. Note that boolean values are reported as "1" or "0"
   */
  public String getData(String elementKey) {
    String result;
    Integer cell = mOwnerTable.getColumnIndexOfElementKey(elementKey);
    if (cell == null) {
      Log.e(TAG, "elementKey [" + elementKey + "] was not found in table");
      return null;
    }
    result = this.mRowData[cell];
    if (result == null) {
      return null;
    }
    return result;
  }

  public String[] getPrimaryKey() {
    String[] primaryKey = mOwnerTable.getPrimaryKey();
    String[] result = new String[primaryKey.length];

    for (int i = 0; i < primaryKey.length; i++) {
      int index = mOwnerTable.getColumnIndexOfElementKey(primaryKey[i]);
      result[i] = mRowData[index];
    }

    return result;
  }

  public boolean matchPrimaryKey(String[] primaryKeyVals) {
    String[] thisKey = getPrimaryKey();

    if (thisKey.length != primaryKeyVals.length) {
      return false;
    }

    for (int i = 0; i < thisKey.length; i++) {
      if (!thisKey[i].equals(primaryKeyVals[i])) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    String[] emptyString = {};

    if (mRowData == null) {
      out.writeInt(0);
      out.writeStringArray(emptyString);
    } else {
      out.writeInt(mRowData.length);
      out.writeStringArray(mRowData);
    }
  }

  public static final Creator<OdkDbRow> CREATOR = new Creator<OdkDbRow>() {
    public OdkDbRow createFromParcel(Parcel in) {
      return new OdkDbRow(in);
    }

    public OdkDbRow[] newArray(int size) {
      return new OdkDbRow[size];
    }
  };
}