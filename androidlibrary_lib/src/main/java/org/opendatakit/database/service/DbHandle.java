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

public class DbHandle implements Parcelable {
  private final String databaseHandle;

  public DbHandle(String databaseHandle) {
    if ( databaseHandle == null ) {
      throw new IllegalArgumentException("null databaseHandle");
    }
    this.databaseHandle = databaseHandle;
  }
  
  public DbHandle(Parcel in) {
    this.databaseHandle = in.readString();
    if ( databaseHandle == null ) {
      throw new IllegalArgumentException("null databaseHandle");
    }
  }

  /**
   * Used all over the place
   * @return the database handle
   */
  @SuppressWarnings("WeakerAccess")
  public String getDatabaseHandle() {
    return this.databaseHandle;
  }
  
  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.getDatabaseHandle());
  }

  public static final Parcelable.Creator<DbHandle> CREATOR = new Parcelable.Creator<DbHandle>() {
    public DbHandle createFromParcel(Parcel in) {
      return new DbHandle(in);
    }

    public DbHandle[] newArray(int size) {
      return new DbHandle[size];
    }
  };
}
