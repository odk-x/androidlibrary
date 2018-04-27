/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.database;

/**
 * Used in ODKDatabaseImplUtils, SubmissionProvider, TablesProvider,
 * ODKDatabaseImplUtilsResetState, SyncETagsUtils, OdkDatabaseServiceInterface,
 * OdkResolveConflictRowLoader, InstanceProvider, ChoiceListUtils, FormsProvider,
 * OdkResolveCheckpointRowLoader
 */
@SuppressWarnings("unused")
public final class DatabaseConstants {

  /**
   * key-value store table
   * Used in ODKDatabaseImplUtils, InstanceProvider, SubmissionProvider,
   * ODKDatabaseImplUtilsResetState
   */
  public static final String KEY_VALUE_STORE_ACTIVE_TABLE_NAME = "_key_value_store_active";

  /**
   * table definitions table
   */

  // only one of these...
  public static final String TABLE_DEFS_TABLE_NAME = "_table_definitions";
  /**
   * column definitions table
   */

  // only one of these...
  public static final String COLUMN_DEFINITIONS_TABLE_NAME = "_column_definitions";

  /**
   * For ODK Survey (only)
   *
   * Tracks which rows have been sent to the server. TODO: rework to accommodate
   * publishing to multiple formids for a given table row
   */

  public static final String UPLOADS_TABLE_NAME = "_uploads";

  /**
   * For ODK Survey (only)
   *
   * Tracks all the forms present in the forms directory.
   */

  public static final String FORMS_TABLE_NAME = "_formDefs";

  /**
   * For Sync (only)
   * 
   * Tracks the ETag values for manifests and files.
   */
  
  public static final String SYNC_ETAGS_TABLE_NAME = "_sync_etags";

  /**
   * All tools
   *
   * Maintains the (choiceListId, choiceListJSON) mapping.
   */
  public static final String CHOICE_LIST_TABLE_NAME = "_choice_lists";


  // The size of data chunks to pass across the AIDL wire.
  public static final int PARCEL_SIZE = 946176; // 100 KB shy of 1 MB, the max size

  public static final String INT_TRUE_STRING = "1";
  public static final String INT_FALSE_STRING = "0";

  /**
   * Do not instantiate this class
   */
  private DatabaseConstants() {
  }
}
