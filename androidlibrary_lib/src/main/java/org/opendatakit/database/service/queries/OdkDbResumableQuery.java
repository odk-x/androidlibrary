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

public abstract class OdkDbResumableQuery implements OdkDbQuery {

   /**
    * The table to select from
    */
   protected final String mTableId;

   /**
    * The SELECT arguments for the SQL command
    */
   protected final BindArgs mBindArgs;

   /**
    * The ORDER BY arguments
    */
   protected final String[] mOrderByColNames;

   /**
    * The direction of each ORDER BY argument
    */
   protected final String[] mOrderByDirections;

   /**
    * The maximum number of rows to return
    */
   protected int mLimit;

   /**
    * The index into the full results to start counting the limit from
    */
   protected int mOffset;

   public OdkDbResumableQuery (String tableId, BindArgs bindArgs, String[] orderByColNames,
       String[] orderByDirections, Integer limit, Integer offset) {
      if (tableId == null) {
         throw new IllegalArgumentException("Table ID must not be null");
      }

      this.mTableId = tableId;
      this.mBindArgs = bindArgs;
      this.mOrderByColNames = orderByColNames;
      this.mOrderByDirections = orderByDirections;
      this.mLimit = limit != null ? limit.intValue() : -1;
      this.mOffset = offset != null ? offset.intValue() : -1;
   }

   public OdkDbResumableQuery(String tableId, BindArgs bindArgs, String[] orderByColNames,
       String[] orderByDirections, QueryBounds bounds) {

      this(tableId, bindArgs, orderByColNames, orderByDirections, bounds.mLimit, bounds.mOffset);
   }

   /**
    * Use the limit, direction, and the resume points to construct a SQL command to retrieve the
    * next or previous set of rows
    *
    * @return the paginated SQL command string
    */
   abstract public String getPaginatedSqlCommand();

   /**
    * Retrieve the id of the table to be queried
    *
    * @return id of the table to be queried
    */
   public String getTableId() {
      return mTableId;
   }

   /**
    * Return the SQL selection arguments
    *
    * @return the selection arguments
    */
   public BindArgs getSqlBindArgs() {
      return mBindArgs;
   }

   /**
    * Return the SQL query bounds
    *
    * @return the query bounds
    */
   public QueryBounds getSqlQueryBounds() {
      if (mLimit < 0 && mOffset < 0) {
         return null;
      }

      return new QueryBounds(mLimit, getSqlOffset());
   }

   /**
    * Set the maximum number of rows to return from the paginated query
    *
    * @param limit max paginated rows
    */
   public void setSqlLimit(int limit) {
      this.mLimit = limit;
   }

   /**
    * Get the value for the maximum number of rows to return from the paginated query
    *
    * @return max paginated rows
    */
   public int getSqlLimit() {
      return mLimit;
   }

   /**
    * Set the row index to start from when resuming a query
    *
    * @param offset the offset to use when resuming a query
    */
   public void setSqlOffset(int offset) {
      mOffset = offset;
   }

   /**
    * Return the offset of the query
    *
    * @return query offset
    */
   public int getSqlOffset() {
      return mOffset;
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

   /**
    * Return whether sufficient conditions have been met to resume this query
    *
    * @return if the query can be resumed
    */
   public boolean isResumable() {
      return (mOrderByColNames != null && mOrderByColNames.length > 0);
   }
}
