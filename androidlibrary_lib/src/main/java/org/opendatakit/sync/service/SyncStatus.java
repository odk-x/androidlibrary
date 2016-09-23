/*
 * Copyright (C) 2014 University of Washington
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
 * If no sync is in progress, and there are no results from an earlier sync, the sync service will
 * return NONE.
 *
 * If a sync is in progress, it will return SYNCING and the current
 * state of the sync action can be retrieved by requesting the most recent SyncProgressEvent
 *
 * If there is a prior result from a sync, the overall outcome of that sync will be reported
 * through the other enum values.
 */
public enum SyncStatus implements Parcelable {
  /** no earlier sync and no active sync */
  NONE,
  /** active sync -- get SyncProgressEvent to see current status */
  SYNCING,
  /** earlier sync ended with socket or lower level transport or protocol error (e.g., 300's) */
  NETWORK_TRANSPORT_ERROR,
  /** earlier sync ended with Authorization denied (authentication and/or access) error */
  AUTHENTICATION_ERROR,
  /** earlier sync ended with a 500 error from server */
  SERVER_INTERNAL_ERROR,
  /** earlier sync ended with a 400 error that wasn't Authorization denied */
  REQUEST_OR_PROTOCOL_ERROR,
  /** server URL does not appear to be correct or server is non-conformant */
  SERVER_IS_NOT_ODK_SERVER,
  /** error accessing or updating database */
  DEVICE_ERROR,
  /** the server is not configured for this appName -- Site Admin / Preferences */
  APPNAME_NOT_SUPPORTED_BY_SERVER,
  /** the server does not have any configuration, or no configuration for this client version */
  SERVER_MISSING_CONFIG_FILES,
  /** the device does not have any configuration to push to server */
  SERVER_RESET_FAILED_DEVICE_HAS_NO_CONFIG_FILES,
  /** while a sync was in progress, another device reset the app config, requiring a restart of
   * our sync */
  RESYNC_BECAUSE_CONFIG_HAS_BEEN_RESET_ERROR,
  /** earlier sync ended with one or more tables containing row conflicts or checkpoint rows */
  CONFLICT_RESOLUTION,
  /** earlier sync ended successfully without conflicts and all row-level attachments sync'd */
  SYNC_COMPLETE,
  /** earlier sync ended successfully without conflicts but needs row-level attachments sync'd */
  SYNC_COMPLETE_PENDING_ATTACHMENTS;

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.name());
  }

  public static final Parcelable.Creator<SyncStatus> CREATOR = new Parcelable.Creator<SyncStatus>() {
    public SyncStatus createFromParcel(Parcel in) {
      return SyncStatus.valueOf(in.readString());
    }

    public SyncStatus[] newArray(int size) {
      return new SyncStatus[size];
    }
  };

}
