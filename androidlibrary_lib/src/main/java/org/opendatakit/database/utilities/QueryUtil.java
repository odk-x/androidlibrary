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
package org.opendatakit.database.utilities;

import org.opendatakit.provider.DataTableColumns;

public final class QueryUtil {

  /**
   * Standard Queries we commonly use
   * All five are used in ODKDatabaseImplUtils
   */
  @SuppressWarnings("unused")
  public static final String WHERE_CLAUSE_ROWS_WITH_ID_EQUALS = DataTableColumns.ID + "=?";
  @SuppressWarnings("unused")
  public static final String[] ORDER_BY_SAVEPOINT_TIMESTAMP = {
      DataTableColumns.SAVEPOINT_TIMESTAMP };
  @SuppressWarnings("unused")
  public static final String[] ORDER_BY_DESCENDING = { "DESC" };

  /**
   * This class should never be instantiated
   */
  private QueryUtil() {
    throw new IllegalStateException("Never Instantiate this static class");
  }

  public static String buildSqlStatement(String tableId, String whereClause, String[] groupBy,
      String having, String[] orderByElementKey, String[] orderByDirection) {
    StringBuilder s = new StringBuilder();
    s.append("SELECT * FROM \"").append(tableId).append("\" ");

    if (whereClause != null && !whereClause.isEmpty()) {
      s.append(" WHERE ").append(whereClause);
    }

    if (groupBy != null && groupBy.length != 0) {
      s.append(" GROUP BY ");
      boolean first = true;
      for (String elementKey : groupBy) {
        if (!first) {
          s.append(", ");
        }
        first = false;
        s.append(elementKey);
      }
      if (having != null && !having.isEmpty()) {
        s.append(" HAVING ").append(having);
      }
    }

    boolean directionSpecified = orderByDirection != null && orderByElementKey != null
        && orderByDirection.length == orderByElementKey.length;
    if (orderByElementKey != null && orderByElementKey.length != 0) {
      boolean first = true;
      for (int i = 0; i < orderByElementKey.length; i++) {
        if (orderByElementKey[i] == null || orderByElementKey[i].isEmpty()) {
          continue;
        }

        if (first) {
          s.append(" ORDER BY ");
          first = false;
        } else {
          s.append(", ");
        }
        s.append(orderByElementKey[i]);

        if (directionSpecified && orderByDirection[i] != null && orderByDirection.length > 0) {
          s.append(" ").append(orderByDirection[i]);
        } else {
          s.append(" ASC");
        }
      }
    }

    return s.toString();
  }

  // TODO: This is generally used to convert single string order by arguments into arrays. It should
  // be plumped all the way to the Javascript so that this conversion isn't necessary
  public static String[] convertStringToArray(String arg) {
    String[] emptyArray = {};
    return (arg == null ? emptyArray : new String[]{arg});
  }
}

