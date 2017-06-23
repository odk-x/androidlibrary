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

import java.io.Serializable;

/**
 * This holds the bounds of the query to be performed. How many rows to return, and what index to
 * start from
 */
@SuppressWarnings("serial")
public class QueryBounds implements Parcelable, Serializable {

   /**
    * Used in ODKDatabaseImplUtils
    */
   @SuppressWarnings("WeakerAccess")
   public final int mLimit;

   public final int mOffset;

   public QueryBounds() {
      mLimit = -1;
      mOffset = 0;
   }

   public QueryBounds(int limit, int offset) {
      mLimit = limit;
      mOffset = offset;
   }

   protected QueryBounds(Parcel in) {
      mLimit = in.readInt();
      mOffset = in.readInt();
   }

   @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(mLimit);
      dest.writeInt(mOffset);
   }

   @Override public int describeContents() {
      return 0;
   }

   public static final Creator<QueryBounds> CREATOR = new Creator<QueryBounds>() {
      @Override public QueryBounds createFromParcel(Parcel in) {
         return new QueryBounds(in);
      }

      @Override public QueryBounds[] newArray(int size) {
         return new QueryBounds[size];
      }
   };
}
