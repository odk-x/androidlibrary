/*
 * Copyright (C) 2015 University of Washington
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
 * All tools.
 * <p>
 * Holds a mapping of an appName-unique choiceListID to
 * the choiceListJSON string.
 * <p>
 * This allows for future sharing of choiceList definitions
 * across forms and minimizes the size of the KVS content
 * when a large form reuses the same choiceList for many
 * questions (e.g, Yes/No prompts).
 * <p>
 * Used in ODKDatabaseImplUtils and ChoiceListUtils
 */
@SuppressWarnings("unused")
public final class ChoiceListColumns {

  /**
   * Used in ChoiceListUtils
   */
  @SuppressWarnings("WeakerAccess")
  public static final String CHOICE_LIST_ID = "_choice_list_id";
  /**
   * Used in ChoiceListUtils
   */
  @SuppressWarnings("WeakerAccess")
  public static final String CHOICE_LIST_JSON = "_choice_list_json";

  /**
   * This class cannot be instantiated
   */
  private ChoiceListColumns() {
  }

  /**
   * Get the create sql for the choiceList table.
   *
   * @return a sql query that wil create the table
   */
  public static String getTableCreateSql(String tableName) {
    //@formatter:off
    return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        + BaseColumns._ID + " integer primary key, "
        + CHOICE_LIST_ID + " TEXT NOT NULL, "
        + CHOICE_LIST_JSON + " TEXT NOT NULL)";
    //@formatter:on
  }

}