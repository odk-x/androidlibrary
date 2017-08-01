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
 * SyncOutcome code used at app and table levels when returning SyncOverallResult
 *
 * Used all over the place
 *
 * WORKING should never be returned (the initial value)
 */
@SuppressWarnings("WeakerAccess")
public enum SyncOutcome implements Parcelable {
  WORKING,
  SUCCESS,
  FAILURE,

  ACCESS_DENIED_EXCEPTION,
  ACCESS_DENIED_REAUTH_EXCEPTION,
  BAD_CLIENT_CONFIG_EXCEPTION,
  NETWORK_TRANSMISSION_EXCEPTION,
  NOT_OPEN_DATA_KIT_SERVER_EXCEPTION,
  UNEXPECTED_REDIRECT_EXCEPTION,
  INCOMPATIBLE_SERVER_VERSION_EXCEPTION,
  INTERNAL_SERVER_FAILURE_EXCEPTION,
  LOCAL_DATABASE_EXCEPTION,

  /** change server configuration Site Admin / Preferences */
  APPNAME_DOES_NOT_EXIST_ON_SERVER,
  /** server doesn't have anything to sync down -- is it configured? */
  CLIENT_VERSION_FILES_DO_NOT_EXIST_ON_SERVER,
  /** device has no files -- it doesn't make sense to reset the server */
  NO_LOCAL_TABLES_TO_RESET_ON_SERVER,
  /** server doesn't have any app level files (e.g., app.properties) -- is it configured? */
  NO_APP_LEVEL_FILES_ON_SERVER_TO_SYNC,
  /** server doesn't have any data tables -- is it configured? */
  NO_TABLES_ON_SERVER_TO_SYNC,
  /** server has one or more config files that are declared but do not have content bodies */
  INCOMPLETE_SERVER_CONFIG_MISSING_FILE_BODY,

  TABLE_DOES_NOT_EXIST_ON_SERVER,
  TABLE_SCHEMA_COLUMN_DEFINITION_MISMATCH,
  TABLE_CONTAINS_CHECKPOINTS,
  TABLE_CONTAINS_CONFLICTS,
  TABLE_PENDING_ATTACHMENTS,
  TABLE_REQUIRES_APP_LEVEL_SYNC;

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
