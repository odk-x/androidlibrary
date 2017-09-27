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

/**
 * This is a basic query with the typical building blocks.
 */
public class ArbitraryQuery extends ResumableQuery {

   /**
    * The the abstract SQL command
    */
   private final String mSqlCommand;

   /**
    * Constructor the query
    *
    * @param tableId    The table to query
    * @param bindArgs   The sql selection args
    * @param sqlCommand The arbitrary sql command
    * @param limit      The maximum number of rows to return
    * @param offset     The offset to start counting the limit from
    */
   public ArbitraryQuery(String tableId, BindArgs bindArgs, String sqlCommand, Integer limit,
       Integer offset) {
      super(tableId, bindArgs, limit, offset);

      if (sqlCommand == null) {
         throw new IllegalArgumentException("SQL command must not be null");
      }

      this.mSqlCommand = sqlCommand;
   }

   /**
    * Used in UserDbInterfaceImpl
    * @param tableId the table id
    * @param bindArgs the arguments to the query
    * @param sqlCommand the raw sql query
    * @param bounds a limit
    */
   @SuppressWarnings("unused")
   public ArbitraryQuery(String tableId, BindArgs bindArgs, String sqlCommand,
       QueryBounds bounds) {

      this(tableId, bindArgs, sqlCommand, bounds != null ? bounds.mLimit : -1,
          bounds != null ? bounds.mOffset : -1);
   }


   public ArbitraryQuery(Parcel in) {
      super(in);

      this.mSqlCommand = readStringFromParcel(in);
   }

   public String getSqlCommand() {
      return mSqlCommand;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);

      writeStringToParcel(dest, mSqlCommand);
   }

   public static final Parcelable.Creator<ArbitraryQuery> CREATOR =
           new Parcelable.Creator<ArbitraryQuery>() {
              public ArbitraryQuery createFromParcel(Parcel in) {
                 return new ArbitraryQuery(in);
              }

              public ArbitraryQuery[] newArray(int size) {
                 return new ArbitraryQuery[size];
              }
           };

}