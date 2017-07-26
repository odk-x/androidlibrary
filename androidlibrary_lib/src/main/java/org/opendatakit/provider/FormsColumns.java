/*
 * Copyright (C) 2013 University of Washington
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

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

/**
 * ODK Survey (only)
 * <p>
 * Tracks what forms are available in the ODK Survey forms directory.
 */
public final class FormsColumns implements BaseColumns {
  /**
   * Used in FormsProvider
   */
  @SuppressWarnings("unused")
  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.opendatakit.form";
  /**
   * Used in FormsProvider
   */
  @SuppressWarnings("unused")
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.opendatakit.form";
  /**
   * The form_id that holds the common javascript files for Survey
   */
  public static final String COMMON_BASE_FORM_ID = "framework";
  // These are the only things needed for an insert
  public static final String TABLE_ID = "tableId"; // for Tables linkage
  public static final String FORM_ID = "formId";
  /**
   * the entire JSON settings portion of the formDef.json file
   * Used in FormsInfo and FormsProvider
   */
  @SuppressWarnings("WeakerAccess")
  public static final String SETTINGS = "settings";

  // these are extracted from the formDef.json for you
  /**
   * form_version | value from the settings sheet
   * Used in FormIdStruct, FormListLoader, FormsProvider and FormInfo
   */
  @SuppressWarnings("WeakerAccess")
  public static final String FORM_VERSION = "formVersion"; // can be null
  /**
   * survey | display.title from the settings sheet
   * In general, this will be an localizable JS object.
   * Used in survey AndroidShortcuts and FormListLoader, also in services in FormsProvider,
   * FormInfo, OdkResolveConflictRowLoader and OdkResolveCheckpointRowLoader
   */
  @SuppressWarnings("WeakerAccess")
  public static final String DISPLAY_NAME = "displayName";
  /**
   * locale that the form should start in (extracted from settings)
   * Used in FormInfo and FormsProvider in services
   */
  @SuppressWarnings("WeakerAccess")
  public static final String DEFAULT_FORM_LOCALE = "defaultFormLocale";
  /**
   * column name for the 'instance_name' (display name) of a submission
   * Used in FormsProvider FormInfo OdkResolveConflictRowLoader and OdkResolveCheckpointRowLoader
   * (extracted from settings)
   */
  @SuppressWarnings("WeakerAccess")
  public static final String INSTANCE_NAME = "instanceName";
  /**
   * these are generated for you
   * Used in InitializationUtil, FormsProvider, FormInfo
   */
  public static final String JSON_MD5_HASH = "jsonMd5Hash";
  /**
   * Used in FormIdStruct, FormListLoader, FormProvider, FormInfo
   * last modification date of the file (long)
   */
  @SuppressWarnings("WeakerAccess")
  public static final String DATE = "date";
  /**
   * bytes in the formDef.json (long)
   * Used inFormsProvider, FormInfo
   */
  @SuppressWarnings("WeakerAccess")
  public static final String FILE_LENGTH = "fileLength";
  /**
   * NOTE: this omits _ID (the primary key)
   * Used in FormInfo
   */
  @SuppressWarnings("unused")
  public static final String[] formsDataColumnNames = { TABLE_ID, FORM_ID, SETTINGS, FORM_VERSION,
      DISPLAY_NAME, DEFAULT_FORM_LOCALE, INSTANCE_NAME, JSON_MD5_HASH, FILE_LENGTH, DATE };

  /**
   * This class cannot be instantiated
   */
  private FormsColumns() {
  }

  /**
   * Get the create sql for the forms table
   * Used in services ODKDatabaseImplUtils
   *
   * @return a sql query that can be used to create the table
   */
  @SuppressWarnings("unused")
  public static String getTableCreateSql(String tableName) {
    //@formatter:off
    return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
           + _ID + " integer not null primary key, " // for Google...
           + TABLE_ID + " text not null, " // PK part 1
           + FORM_ID + " text not null, "  // PK part 2
           + SETTINGS + " text not null, "
           + FORM_VERSION + " text, "
           + DISPLAY_NAME + " text not null, "
           + DEFAULT_FORM_LOCALE + " text null, "
           + INSTANCE_NAME + " text null, " 
           + JSON_MD5_HASH + " text not null, "
           + FILE_LENGTH + " integer not null, "
           + DATE + " integer not null " // milliseconds
           + ")";
    //@formatter:on
  }

  /**
   * Gets an app name from the given URI. Used in FormIdStruct
   * @param uri a URI that contains an app name
   * @return the app name from the URI
   */
  @SuppressWarnings("unused")
  public static String extractAppNameFromFormsUri(Uri uri) {
    List<String> segments = uri.getPathSegments();

    if (segments.size() < 1) {
      throw new IllegalArgumentException("Unknown URI (incorrect number of segments!) " + uri);
    }

    return segments.get(0);
  }
}