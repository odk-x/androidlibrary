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
package org.opendatakit.common.android.database.queries;

import org.opendatakit.common.android.database.utilities.QueryUtil;

/**
 * This is a basic query with the typical building blocks.
 */
public class SimpleQuery extends ResumableQuery {

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
    * The ORDER BY arguments
    */
   private final String[] mOrderByColNames;

   /**
    * The direction of each ORDER BY argument
    */
   private final String[] mOrderByDirections;



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
   public SimpleQuery(String tableId, BindArgs bindArgs, String whereClause,
       String[] groupByArgs, String havingClause, String[] orderByColNames,
       String[] orderByDirections, Integer limit, Integer offset) {

      super(tableId, bindArgs, limit, offset);

      this.mWhereClause = whereClause;
      this.mGroupByArgs = groupByArgs;
      this.mHavingClause = havingClause;

      this.mOrderByColNames = orderByColNames;
      this.mOrderByDirections = orderByDirections;
   }

   public SimpleQuery(String tableId, BindArgs bindArgs, String whereClause,
       String[] groupByArgs, String havingClause, String[] orderByColNames,
       String[] orderByDirections, QueryBounds bounds) {

      this(tableId, bindArgs, whereClause, groupByArgs, havingClause, orderByColNames,
          orderByDirections, (bounds != null ? bounds.mLimit : -1),
          (bounds != null ? bounds.mOffset : -1));
   }


   public String getSqlCommand() {
      return QueryUtil.buildSqlStatement(mTableId, mWhereClause, mGroupByArgs, mHavingClause,
          mOrderByColNames, mOrderByDirections);
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

   /**
    * Return the list of columns to order the results by
    *
    * @return column names to order by
    */
   public String[] getOrderByColNames() {
      return (mOrderByColNames != null ? mOrderByColNames.clone() : null);
   }

   /**
    * Return the list of direction values corresponding to the order by columns
    *
    * @return directions to order results by for each column
    */
   public String[] getOrderByDirections() {
      return (mOrderByDirections != null ? mOrderByDirections.clone() : null);
   }
}