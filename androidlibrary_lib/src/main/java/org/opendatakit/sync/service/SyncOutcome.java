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

package org.opendatakit.sync.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SyncOutcome code used at app and table levels when returning SyncResult
 *
 * WORKING should never be returned (the initial value)
 */
public enum SyncOutcome implements Parcelable {
  WORKING, SUCCESS, FAILURE, AUTH_EXCEPTION, EXCEPTION, TABLE_DOES_NOT_EXIST_ON_SERVER, TABLE_CONTAINS_CHECKPOINTS, TABLE_CONTAINS_CONFLICTS, TABLE_PENDING_ATTACHMENTS, TABLE_REQUIRES_APP_LEVEL_SYNC;

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.name());
  }

  public static final Parcelable.Creator<SyncOutcome> CREATOR = new Parcelable.Creator<SyncOutcome>() {
    public SyncOutcome createFromParcel(Parcel in) {
      return SyncOutcome.valueOf(in.readString());
    }

    public SyncOutcome[] newArray(int size) {
      return new SyncOutcome[size];
    }
  };
}
