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

import org.opendatakit.common.android.provider.DataTableColumns;

public class OdkDbQueryUtil {

   private static final String TAG = OdkDbQueryUtil.class.getSimpleName();

   private OdkDbQueryUtil() {
      // This class should never be instantiated
      throw new IllegalStateException("Never Instantiate this static class");
   }

   public static final String buildSqlStatement(String tableId, String whereClause,
       String[] groupBy, String having, String[] orderByElementKey, String[] orderByDirection) {
      StringBuilder s = new StringBuilder();
      s.append("SELECT * FROM \"").append(tableId).append("\" ");

      if (whereClause != null && whereClause.length() != 0) {
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
         if (having != null && having.length() != 0) {
            s.append(" HAVING ").append(having);
         }
      }

      boolean directionSpecified =
          (orderByDirection != null && orderByElementKey != null) && (orderByDirection.length
              == orderByElementKey.length);
      if (orderByElementKey != null && orderByElementKey.length != 0) {
         boolean first = true;
         for (int i = 0; i < orderByElementKey.length; i++) {
            if (orderByElementKey == null || orderByElementKey.length == 0) {
               continue;
            }

            if (first) {
               s.append(" ORDER BY ");
            } else {
               s.append(", ");
               first = false;
            }
            s.append(orderByElementKey[i]);

            if (directionSpecified && orderByDirection[i] != null && orderByDirection.length > 0) {
               s.append(" " + orderByDirection[i]);
            } else {
               s.append(" ASC");
            }
         }
      }

      return s.toString();
   }

   /* Standard Queries we commonly use */
   public static final String GET_ROWS_WITH_ID_WHERE = DataTableColumns.ID + "=?";
   public static final String[] GET_ROWS_WITH_ID_GROUP_BY = null;
   public static final String GET_ROWS_WITH_ID_HAVING = null;
   public static final String[] GET_ROWS_WITH_ID_ORDER_BY_KEYS = {
       DataTableColumns.SAVEPOINT_TIMESTAMP };
   public static final String[] GET_ROWS_WITH_ID_ORDER_BY_DIR = { "DESC" };
}
