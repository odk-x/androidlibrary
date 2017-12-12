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

package org.opendatakit.provider;

import android.provider.BaseColumns;

/**
 * Used in database.utilities.SyncETagsUtils and a little bit in ODKDatabaseImplUtils
 * (both services) and provider.SyncETagColumns in androidlibrary
 * <p>
 * <p>Performs two functions</p>
 * <ul><li>Tracks the md5 hash of local files and their modification
 * times.  Used to optimize the sync time by not re-computing
 * the md5 hash of the local files if their modification times
 * have not changed.</li>
 * <li>Tracks the md5 hash of the last manifest returned by the
 * server. Subsequent requests will send this in an if-modified
 * header to minimize the size of data transmitted across the wire.
 * </li></ul>
 */
@SuppressWarnings("unused")
public final class SyncETagColumns {

  /**
   * These first four used in SyncETagsUtils
   */
  @SuppressWarnings("WeakerAccess")
  public static final String TABLE_ID = "_table_id";
  @SuppressWarnings("WeakerAccess")
  public static final String IS_MANIFEST = "_is_manifest";
  @SuppressWarnings("WeakerAccess")
  public static final String LAST_MODIFIED_TIMESTAMP = "_last_modified";
  @SuppressWarnings("WeakerAccess")
  public static final String ETAG_MD5_HASH = "_etag_md5_hash";
  public static final String URL = "_url";

  /**
   * This class cannot be instantiated
   */
  private SyncETagColumns() {
  }

  /**
   * Get the create sql for the syncETags table
   * Used in ODKDataImplUtils
   *
   * @return a sql statement for creating the relevant table
   */
  public static String getTableCreateSql(String tableName) {
    //@formatter:off
    return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        + BaseColumns._ID + " integer primary key, "
        + TABLE_ID + " TEXT NULL, "
        + IS_MANIFEST + " INTEGER, "
        + URL + " TEXT NOT NULL, "
        + LAST_MODIFIED_TIMESTAMP + " TEXT NOT NULL, "
        + ETAG_MD5_HASH + " TEXT NOT NULL)";
    //@formatter:on
  }
}