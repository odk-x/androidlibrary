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
package org.opendatakit.database.queries;

/**
 * Extended by ArbitraryQuery and SimpleQuery
 */
public abstract class ResumableQuery implements Query {

   /**
    * The table to select from
    */
   final String mTableId;

   /**
    * The SELECT arguments for the SQL command
    */
   private final BindArgs mBindArgs;

   /**
    * The maximum number of rows to return
    */
   private int mLimit;

   /**
    * The index into the full results to start counting the limit from
    */
   private int mOffset;

   ResumableQuery(String tableId, BindArgs bindArgs, Integer limit, Integer offset) {
      this.mTableId = tableId;
      this.mBindArgs = bindArgs != null ? bindArgs : new BindArgs(new Object[0]);
      this.mLimit = limit != null ? limit : -1;
      this.mOffset = offset != null ? offset : -1;
   }

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
}
