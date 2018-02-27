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

import android.os.Parcel;
import android.os.Parcelable;
import org.opendatakit.database.utilities.QueryUtil;

/**
 * This is a basic query with the typical building blocks.
 */
public class SimpleQuery extends ResumableQuery {

   /**
    * The WHERE arguments for the SQL command
    */
   protected final String mWhereClause;

   /**
    * The GROUP BY arguments for the SQL command
    */
   protected final String[] mGroupByArgs;

   /**
    * The HAVING argument for the SQL command
    */
   protected final String mHavingClause;

   /**
    * The ORDER BY arguments
    */
   protected final String[] mOrderByColNames;

   /**
    * The direction of each ORDER BY argument
    */
   protected final String[] mOrderByDirections;



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

      if (tableId == null) {
         throw new IllegalArgumentException("Table ID must not be null");
      }

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
          orderByDirections, bounds != null ? bounds.mLimit : -1,
          bounds != null ? bounds.mOffset : -1);
   }

   public SimpleQuery(Parcel in) {
      super(in);

      this.mWhereClause = readStringFromParcel(in);
      this.mGroupByArgs = readStringArrFromParcel(in);
      this.mHavingClause = readStringFromParcel(in);
      this.mOrderByColNames = readStringArrFromParcel(in);
      this.mOrderByDirections = readStringArrFromParcel(in);
   }


   public String getSqlCommand() {
      return QueryUtil.buildSqlStatement(mTableId, mWhereClause, mGroupByArgs, mHavingClause,
          mOrderByColNames, mOrderByDirections);
   }

   /**
    * Totally unused
    * @return the where clause for the query
    */
   public String getWhereClause() {
      return mWhereClause;
   }

   /**
    * Totally unused
    * @return the group by arguments for the query
    */
   public String[] getGroupByArgs() {
      return mGroupByArgs != null ? mGroupByArgs.clone() : null;
   }

   /**
    * Totally unused
    * @return the having clause for the query
    */
   public String getHavingClause() {
      return mHavingClause;
   }

   /**
    * Return the list of columns to order the results by
    * totally unused
    *
    * @return column names to order by
    */
   public String[] getOrderByColNames() {
      return mOrderByColNames != null ? mOrderByColNames.clone() : null;
   }

   /**
    * Return the list of direction values corresponding to the order by columns
    * totally unused
    *
    * @return directions to order results by for each column
    */
   public String[] getOrderByDirections() {
      return mOrderByDirections != null ? mOrderByDirections.clone() : null;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);

      writeStringToParcel(dest, mWhereClause);
      writeStringArrToParcel(dest, mGroupByArgs);
      writeStringToParcel(dest, mHavingClause);
      writeStringArrToParcel(dest, mOrderByColNames);
      writeStringArrToParcel(dest, mOrderByDirections);
   }

   public static final Parcelable.Creator<SimpleQuery> CREATOR =
           new Parcelable.Creator<SimpleQuery>() {
              public SimpleQuery createFromParcel(Parcel in) {
                 return new SimpleQuery(in);
              }

              public SimpleQuery[] newArray(int size) {
                 return new SimpleQuery[size];
              }
           };
}