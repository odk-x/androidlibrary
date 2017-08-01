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

import java.util.HashMap;

/**
 * The mapping of a table to the SyncOutcome of its synchronization.
 * Also includes interesting metrics on the scope of change.
 * <p>
 * Used in SyncOverallResult, (in services.sync.service.logic) ProcessAppAndTableLevelChanges,
 * ProcessRowDataSyncAttachments, ProcessRowDataShardBase, ProcessRowDataPushLocalChanges,
 * ProcessRowDataPullServerUpdates, ProcessRowDataOrchestrateChanges, (services.sync.service)
 * SyncExecutionContext, AppSynchronizer
 *
 * TODO it looks like we set things but never read them
 *
 * @author sudar.sam@gmail.com
 */
@SuppressWarnings("WeakerAccess")
public class TableLevelResult implements Parcelable {
  public static final Creator<TableLevelResult> CREATOR = new Creator<TableLevelResult>() {
    @Override
    public TableLevelResult createFromParcel(Parcel in) {
      return new TableLevelResult(in);
    }

    @Override
    public TableLevelResult[] newArray(int size) {
      return new TableLevelResult[size];
    }
  };
  /**
   * Used for logging
   */
  @SuppressWarnings("unused")
  private static final String TAG = TableLevelResult.class.getSimpleName();
  private final String mTableId;
  private String mDisplayName;
  private String mMessage = SyncOutcome.WORKING.name();
  private SyncOutcome mSyncOutcome = SyncOutcome.WORKING;

  /**
   * Flag if schema was pulled from the server.
   */
  private boolean mPulledServerSchema;
  /**
   * Flag if data was pulled from the server.
   */
  private boolean mPulledServerData;
  /**
   * Flag if data was pushed to the server.
   */
  private boolean mPushedLocalData;
  /**
   * Flag if local data had to be pushed to the server.
   */
  private boolean mHadLocalDataChanges;
  /**
   * Flag if schema had to be pulled from the server.
   */
  private boolean mHadServerSchemaChanges;
  /**
   * Flat if data had to be pulled from the server.
   */
  private boolean mHadServerDataChanges;
  private int mServerNumUpserts = 0;
  private int mServerNumDeletes = 0;
  private int mLocalNumInserts = 0;
  private int mLocalNumDeletes = 0;
  private int mLocalNumAttachmentRetries = 0;

  protected TableLevelResult(Parcel in) {
    mTableId = in.readString();
    mDisplayName = in.readString();
    mMessage = in.readString();
    mPulledServerSchema = in.readByte() != 0;
    mPulledServerData = in.readByte() != 0;
    mPushedLocalData = in.readByte() != 0;
    mHadLocalDataChanges = in.readByte() != 0;
    mHadServerSchemaChanges = in.readByte() != 0;
    mHadServerDataChanges = in.readByte() != 0;
    mServerNumUpserts = in.readInt();
    mServerNumDeletes = in.readInt();
    mLocalNumInserts = in.readInt();
    mLocalNumDeletes = in.readInt();
    mLocalNumAttachmentRetries = in.readInt();
  }

  /**
   * Create a table result with a status of {@link SyncOutcome#FAILURE}. This should
   * then only be updated in the case of success or exceptions. The boolean
   * flags are initialized to false;
   *
   * @param tableId the id of the table to create a result for
   */
  public TableLevelResult(String tableId) {
    this.mTableId = tableId;
    this.mDisplayName = tableId;
    this.mPulledServerData = false;
    this.mPulledServerSchema = false;
    this.mPushedLocalData = false;
    this.mHadLocalDataChanges = false;
    this.mHadServerDataChanges = false;
    this.mHadServerSchemaChanges = false;
  }

  public HashMap<String, Object> getStatusMap() {
    HashMap<String, Object> statusMap = new HashMap<String, Object>();
    statusMap.put("tableId", mTableId);
    statusMap.put("syncOutcome", mSyncOutcome.name());
    statusMap.put("message", mMessage);

    // some of these will be under-reported if server schema is bad
    statusMap.put("pulledServerSchemaInfo", mPulledServerSchema);
    statusMap.put("serverSchemaDiffered", mHadServerSchemaChanges);
    if ( mSyncOutcome != SyncOutcome.TABLE_SCHEMA_COLUMN_DEFINITION_MISMATCH ) {

      statusMap.put("hadServerDataChanges", mHadServerDataChanges);
      statusMap.put("pulledServerDataChanges", mPulledServerData);
      statusMap.put("hadLocalDataChanges", mHadLocalDataChanges);
      statusMap.put("pushedLocalChanges", mPushedLocalData);

      statusMap.put("serverNumUpserts", mServerNumUpserts);
      statusMap.put("serverNumDeletes", mServerNumDeletes);
      statusMap.put("localNumInserts", mLocalNumInserts);
      statusMap.put("localNumDeletes", mLocalNumDeletes);
      statusMap.put("localNumAttachmentRetries", mLocalNumAttachmentRetries);

    }
    return statusMap;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mTableId);
    dest.writeString(mDisplayName);
    dest.writeString(mMessage);
    dest.writeByte((byte) (mPulledServerSchema ? 1 : 0));
    dest.writeByte((byte) (mPulledServerData ? 1 : 0));
    dest.writeByte((byte) (mPushedLocalData ? 1 : 0));
    dest.writeByte((byte) (mHadLocalDataChanges ? 1 : 0));
    dest.writeByte((byte) (mHadServerSchemaChanges ? 1 : 0));
    dest.writeByte((byte) (mHadServerDataChanges ? 1 : 0));
    dest.writeInt(mServerNumUpserts);
    dest.writeInt(mServerNumDeletes);
    dest.writeInt(mLocalNumInserts);
    dest.writeInt(mLocalNumDeletes);
    dest.writeInt(mLocalNumAttachmentRetries);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  /**
   * Used in Aggregate TableResults, services.sync.service.logic.ProcessRowDataPushLocalChanges
   */
  @SuppressWarnings("unused")
  public void incServerUpserts() {
    mServerNumUpserts++;
  }

  /**
   * @see #incServerUpserts for usages
   */
  @SuppressWarnings("unused")
  public void incServerDeletes() {
    mServerNumDeletes++;
  }

  /**
   * @see #incServerUpserts for usages
   */
  @SuppressWarnings("unused")
  public void incLocalInserts() {
    mLocalNumInserts++;
  }

  /**
   * @see #incServerUpserts for usages
   */
  @SuppressWarnings("unused")
  public void incLocalDeletes() {
    mLocalNumDeletes++;
  }

  /**
   * @see #incServerUpserts for usages
   */
  @SuppressWarnings("unused")
  public void incLocalAttachmentRetries() {
    mLocalNumAttachmentRetries++;
  }

  public String getTableId() {
    return this.mTableId;
  }

  /**
   * Used in aggregate TableResults, (services.sync.service.logic)
   * ProcessAppAndTableLevelChanges, ProcessRowDataOrchestrateChanges, and
   * services.sync.service.SyncExecutionContext
   * @return the display name for this table
   */
  @SuppressWarnings("unused")
  public String getTableDisplayName() {
    return this.mDisplayName;
  }

  /**
   * @see #getTableDisplayName() for usages
   * @param displayName the display name to update this object to use
   */
  @SuppressWarnings("unused")
  public void setTableDisplayName(String displayName) {
    this.mDisplayName = displayName;
  }

  /**
   * Used in lots of places in services.sync.service.logic.Process*
   * @return the sync outcome
   */
  @SuppressWarnings("unused")
  public SyncOutcome getSyncOutcome() {
    return this.mSyncOutcome;
  }

  /**
   * Update the status of this result
   * Used in lots of places in services.sync.service.logic.Process*
   *
   * @param newSyncOutcome the sync outcome to update to
   * @throws UnsupportedOperationException if the status is not currently {@link SyncOutcome#WORKING}.
   */
  @SuppressWarnings("unused")
  public void setSyncOutcome(SyncOutcome newSyncOutcome) {
    if (this.mSyncOutcome != SyncOutcome.WORKING) {
      throw new UnsupportedOperationException(
          "Tried to set TableLevelResult status to " + newSyncOutcome.name()
              + " when it had already been set to " + mSyncOutcome.name());
    }
    this.mSyncOutcome = newSyncOutcome;
  }

  /**
   * Used in services.sync.service.logic.ProcessRowDataPullServerUpdates
   * @param pulledData whether we pulled down data from the server
   */
  @SuppressWarnings("unused")
  public void setPulledServerData(boolean pulledData) {
    this.mPulledServerData = pulledData;
  }

  /**
   * Used in services.sync.service.logic.ProcessAppAndTableLevelChanges
   * @param pulledSchema whether we pulled down the schema from the server
   */
  @SuppressWarnings("unused")
  public void setPulledServerSchema(boolean pulledSchema) {
    this.mPulledServerSchema = pulledSchema;
  }

  /**
   * Used in ProcessRowDataSyncAttachments, ProcessRowDataPushLocalChanges
   * @param pushedData whether we pushed data or not
   */
  @SuppressWarnings("unused")
  public void setPushedLocalData(boolean pushedData) {
    this.mPushedLocalData = pushedData;
  }

  /**
   * Used in ProcessRowDataPushLocalChanges
   * @param hadChanges whether there were changes to be pushed or not
   */
  @SuppressWarnings("unused")
  public void setHadLocalDataChanges(boolean hadChanges) {
    this.mHadLocalDataChanges = hadChanges;
  }

  /**
   * Used in ProcessAppAndTableLevelChanges, ProcessRowDataOrchestrateChanges
   * @param serverHadChanges whether the server had schema changes or not
   */
  @SuppressWarnings("unused")
  public void setServerHadSchemaChanges(boolean serverHadChanges) {
    this.mHadServerSchemaChanges = serverHadChanges;
  }

  /**
   * Used in ProcessRowDataPullServerUpdates
   * @param serverHadChanges whether the server had data changes
   */
  @SuppressWarnings("unused")
  public void setServerHadDataChanges(boolean serverHadChanges) {
    this.mHadServerDataChanges = serverHadChanges;
  }

  /**
   * Set a message that might be passed back to the user. Likely a place to pass
   * the error message back to the user in case of exceptions.
   *
   * Used in Services somewhere
   *
   * @param message the message to be passed back
   */
  @SuppressWarnings("unused")
  public void setMessage(String message) {
    this.mMessage = message;
  }
}
