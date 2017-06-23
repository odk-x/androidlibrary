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

import java.util.ArrayList;

/**
 * A simple struct to hold the contents of a metadata call and its revision
 *
 */
public class TableMetaDataEntries implements Parcelable {

  private String tableId;

  private String revId;

  private ArrayList<KeyValueStoreEntry> entries;

  public TableMetaDataEntries(String tableId, String revId) {
    this.tableId = tableId;
    this.revId = revId;
    this.entries = new ArrayList<>();
  }

  public TableMetaDataEntries(TableMetaDataEntries other) {
    this.tableId = other.tableId;
    this.revId = other.revId;
    this.entries = new ArrayList<>(other.entries);
  }

  private TableMetaDataEntries(Parcel in) {
    readFromParcel(in);
  }

  public String getTableId() {
    return tableId;
  }

  public String getRevId() {
    return revId;
  }

  public ArrayList<KeyValueStoreEntry> getEntries() {
    return entries;
  }

  public void addEntry(KeyValueStoreEntry e) {
    entries.add(e);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeByte((byte)(tableId != null ? 1 : 0));
    out.writeString(tableId);

    out.writeByte((byte)(revId != null ? 1 : 0));
    out.writeString(revId);

    out.writeByte((byte)(entries != null ? 1 : 0));
    out.writeSerializable(entries);
  }

  @SuppressWarnings("unchecked")
  private void readFromParcel(Parcel in) {
    boolean notNull = in.readByte() == 1;
    tableId = notNull ? in.readString() : null;

    notNull = in.readByte() == 1;
    revId = notNull ? in.readString() : null;

    notNull = in.readByte() == 1;
    entries = notNull ? (ArrayList<KeyValueStoreEntry>) in.readSerializable() :
        new ArrayList<KeyValueStoreEntry>();
  }

  public static final Parcelable.Creator<TableMetaDataEntries> CREATOR
      = new Parcelable.Creator<TableMetaDataEntries>() {
    public TableMetaDataEntries createFromParcel(Parcel in) {
      return new TableMetaDataEntries(in);
    }

    public TableMetaDataEntries[] newArray(int size) {
      return new TableMetaDataEntries[size];
    }
  };
}
