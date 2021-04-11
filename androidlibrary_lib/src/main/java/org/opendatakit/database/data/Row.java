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
import org.opendatakit.database.DatabaseConstants;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * This represents a single row of data in a table.
 */
public final class Row implements Parcelable {

  public static final Creator<Row> CREATOR = new Creator<Row>() {
    public Row createFromParcel(Parcel in) {
      return new Row(in);
    }

    public Row[] newArray(int size) {
      return new Row[size];
    }
  };
  /**
   * Used for logging
   */
  @SuppressWarnings("unused")
  static final String TAG = Row.class.getSimpleName();
  /**
   * The data held in the rows columns
   */
  private final String[] mRowData;
  /**
   * The table that the row belongs to
   */
  private final BaseTable mOwnerTable;

  /**
   * Construct the row.
   *
   * @param rowData    the data in the row.
   * @param ownerTable the table that the row belongs to
   */
  public Row(String[] rowData, BaseTable ownerTable) {
    if (ownerTable == null || rowData == null) {
      throw new IllegalArgumentException("Null arguments are not permitted");
    }
    this.mRowData = rowData;
    this.mOwnerTable = ownerTable;
  }

  /**
   * Unmarshall the row
   *
   * @param in         the marshalled data
   * @param ownerTable the table that the row belongs to
   */
  public Row(Parcel in, BaseTable ownerTable) {
    if (ownerTable == null) {
      throw new IllegalArgumentException("Null arguments are not permitted");
    }
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
  @SuppressWarnings("UnusedParameters")
  public Row(Parcel in) {
    throw new IllegalStateException("Row invalid constructor");
  }


  /**
   * Return the String representing the contents of the cellIndex'th column
   * <p>
   * Null values are returned as nulls.
   *
   * @param cellIndex cellIndex of data or metadata column (0..nCol-1)
   * @return String representation of contents of column. Null values are
   * returned as null. Note that boolean values are reported as "1" or "0"
   */
  public String getRawStringByIndex(int cellIndex) {
    return this.mRowData[cellIndex];
  }

  /**
   * Return the String representing the contents of the cell in the "key" column.
   * <p>
   * Null values are returned as nulls.
   *
   * @param key The name of the column holding the desired data
   * @return String representation of contents of column. Null values are
   * returned as null. Note that boolean values are reported as "1" or "0"
   */
  public String getRawStringByKey(String key) {
    String result;
    Integer cell = getCellIndexByKey(key);
    if (cell == null) {
      return null;
    }
    result = getRawStringByIndex(cell);
    return result;
  }


  /**
   * Return the index of the "key" column.
   * <p>
   * If there is no matching key then null is returned.
   *
   * @param key The name of the column
   * @return Integer index of the column or null
   */
  private Integer getCellIndexByKey(String key) {
    return mOwnerTable.getColumnIndexOfElementKey(key);
  }

  /**
   * Return a pointer to the table this row belongs to
   * Used in AggregateSynchronizer and BaseTable
   *
   * @return the owner table
   */
  @SuppressWarnings("unused")
  public String[] getElementKeyForIndexMap() {
    return mOwnerTable.getElementKeyForIndex();
  }

  /**
   * Return a pointer to the table this row belongs to
   * Completely unused
   *
   * @return the owner table
   */
  public BaseTable getOwnerTable() {
    return mOwnerTable;
  }

  /**
   * Return the data stored in the cursor at the given cellIndex column position
   * as null OR whatever data type it is.
   * <p>
   * This does not actually convert data types from one type to the other.
   * Instead, it safely preserves null values and returns boxed data values.
   * If you specify ArrayList or HashMap, it JSON deserializes the value into
   * one of those.
   *
   * @param cellIndex cellIndex of data or metadata column (0..nCol-1)
   * @param clazz the class to deserialize to
   * @return the deserialized data type
   */
  @SuppressWarnings("unchecked")
  public final <T> T getDataType(int cellIndex, Class<T> clazz) throws IllegalStateException {
    // If you add additional return types here be sure to modify the javadoc.
    try {
      String value = getRawStringByIndex(cellIndex);
      if (value == null) {
        return null;
      }
      if (clazz == String.class) {
        return (T) value;
      } else if (clazz == Double.class) {
        Double d = Double.parseDouble(value);
        return (T) d;
      } else if (clazz == Long.class) {
        Long l = Long.parseLong(value);
        return (T) l;
      } else if (clazz == Boolean.class) {
        // booleans are stored as integer 1 or 0 in user tables.
        Boolean b = !DatabaseConstants.INT_FALSE_STRING.equals(value);
        return (T) b;
      } else if (clazz == ArrayList.class) {
        // json deserialization of an array
        return (T) ODKFileUtils.mapper.readValue(value, ArrayList.class);
      } else if (clazz == HashMap.class) {
        // json deserialization of an object
        return (T) ODKFileUtils.mapper.readValue(value, HashMap.class);
      } else if (clazz == TreeMap.class) {
        // json deserialization of an object
        return (T) ODKFileUtils.mapper.readValue(value, TreeMap.class);
      } else {
        throw new IllegalStateException("Unexpected data type in SQLite table");
      }
    } catch (ClassCastException | IOException e) {
      // JsonParseException and JsonMappingException extends IOException and will be caught here
      WebLogger.getLogger(null).printStackTrace(e);
      throw new IllegalStateException("Unexpected data type conversion failure " + e + " on SQLite table");
    }
  }

  public final <T> T getDataType(String elementKey, Class<T> clazz) {
    Integer cell = getCellIndexByKey(elementKey);
    if (cell == null) {
      return null;
    }
    return getDataType(cell, clazz);
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

}