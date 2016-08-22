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
package org.opendatakit.database.service.queries;

import org.opendatakit.database.service.BindArgs;
import org.opendatakit.database.utilities.OdkDbQueryUtil;

/**
 * This is a basic query with the typical building blocks.
 */
public class OdkDbSimpleQuery extends OdkDbResumableQuery {

   /**
    * The WHERE arguments for the SQL command
    */
   private final String mWhereClause;

   /**
    * The GROUP BY arguments for the SQL command
    */
   private final String[] mGroupByArgs;

   /**
    * The HAVING argument for the SQL command
    */
   private final String mHavingClause;


   /**
    * Construct the query
    *
    * @param tableId The table to query
    * @param bindArgs The sql selection args
    * @param whereClause The sql where clause
    * @param groupByArgs The sql group by arguments
    * @param havingClause The sql having clause
    * @param orderByColNames The columns to order by
    * @param orderByDirections The directions to order by
    * @param limit The maximum number of rows to return
    * @param offset The offset to start counting the limit from
    */
   public OdkDbSimpleQuery(String tableId, BindArgs bindArgs, String whereClause,
       String[] groupByArgs, String havingClause, String[] orderByColNames,
       String[] orderByDirections, Integer limit, Integer offset) {

      super(tableId, bindArgs, orderByColNames, orderByDirections, limit, offset);

      this.mWhereClause = whereClause;
      this.mGroupByArgs = groupByArgs;
      this.mHavingClause = havingClause;
   }

   public OdkDbSimpleQuery(String tableId, BindArgs bindArgs, String whereClause,
       String[] groupByArgs, String havingClause, String[] orderByColNames,
       String[] orderByDirections, QueryBounds bounds) {

      this(tableId, bindArgs, whereClause, groupByArgs, havingClause, orderByColNames,
          orderByDirections, (bounds != null ? bounds.mLimit : -1),
          (bounds != null ? bounds.mOffset : -1));
   }


   public String getSqlCommand() {
      return OdkDbQueryUtil.buildSqlStatement(mTableId, mWhereClause, mGroupByArgs, mHavingClause,
          mOrderByColNames, mOrderByDirections);
   }

   public String getPaginatedSqlCommand() {
      String originalSqlCommand = OdkDbQueryUtil.buildSqlStatement(mTableId, mWhereClause,
          mGroupByArgs, mHavingClause, null, null);

      if (!isResumable()) {
         return originalSqlCommand;
      }

      return OdkDbQueryUtil.wrapOrderBy(originalSqlCommand, mOrderByColNames, mOrderByDirections);
   }

   public String getWhereClause() {
      return mWhereClause;
   }

   public String[] getGroupByArgs() {
      return (mGroupByArgs != null ? mGroupByArgs.clone() : null);
   }

   public String getHavingClause() {
      return mHavingClause;
   }
}