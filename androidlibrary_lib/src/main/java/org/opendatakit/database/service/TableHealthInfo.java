/*
 * Copyright (C) 2015 University of Washington
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

import java.io.Serializable;

@SuppressWarnings("serial")
public class TableHealthInfo implements Parcelable, Serializable {

  private final String tableId;
  private final TableHealthStatus status;
  private final boolean hasChanges;
  
  public String getTableId() {
    return tableId;
  }
  
  public TableHealthStatus getHealthStatus() {
    return status;
  }

  public boolean hasChanges() {
    return hasChanges;
  }
  
  public TableHealthInfo(String tableId, TableHealthStatus status, boolean hasChanges) {
    this.tableId = tableId;
    this.status = status;
    this.hasChanges = hasChanges;
  }
  
  public int describeContents() {
      return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(tableId);
    status.writeToParcel(out, flags);
    out.writeByte((byte) (hasChanges ? 1 : 0));
  }

  public static final Parcelable.Creator<TableHealthInfo> CREATOR
          = new Parcelable.Creator<TableHealthInfo>() {
      public TableHealthInfo createFromParcel(Parcel in) {
          return new TableHealthInfo(in);
      }

      public TableHealthInfo[] newArray(int size) {
          return new TableHealthInfo[size];
      }
  };
  
  private TableHealthInfo(Parcel in) {
    tableId = in.readString();
    status = TableHealthStatus.CREATOR.createFromParcel(in);
    hasChanges = in.readByte() == 1;
  }

}
