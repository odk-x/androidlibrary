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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This holds the list of objects that are bind arguments to the SQLite database.
 * The objects are primitive objects (String, Integer, Long, Float, Double, Boolean)
 */
@SuppressWarnings("serial")
public class BindArgs implements Parcelable, Serializable {

  /**
   * Used in OdkDatabaseServiceImpl, probably more
   */
  @SuppressWarnings("WeakerAccess")
  public final Object[] bindArgs;

  public BindArgs() {
    bindArgs = null;
  }

  public BindArgs(Object[] args) {
    bindArgs = args;
    if ( args != null ) {
      for (Object o : args) {
        if (o == null) continue;
        if (o instanceof String) continue;
        if (o instanceof Integer) continue;
        if (o instanceof Boolean) continue;
        if (o instanceof Double) continue;
        if (o instanceof Float) continue;
        if (o instanceof Long) continue;
        throw new IllegalArgumentException("bind args must be a primitive type");
      }
    }
  }

  public BindArgs(String fromJSON) {
    if ( fromJSON == null || fromJSON.isEmpty()) {
      bindArgs = new Object[0];
      return;
    }

    TypeReference<ArrayList<Object>> type = new TypeReference<ArrayList<Object>>() {};
    try {
      ArrayList<Object> values = ODKFileUtils.mapper.readValue(fromJSON, type);
      if ( values == null ) {
        bindArgs = new Object[0];
      } else {
        bindArgs = values.toArray(new Object[values.size()]);
      }
    } catch (IOException ignored) {
      // not expected
      throw new IllegalStateException("Unable to deserialize bindArgs");
    }
  }

  /**
   * Used in IntentUtil
   * @return the arguments, encoded to json
   */
  @SuppressWarnings("unused")
  public String asJSON() {
    try {
      return ODKFileUtils.mapper.writeValueAsString(bindArgs);
    } catch (JsonProcessingException ignored) {
      // not expected
      throw new IllegalStateException("Unable to serialize bindArgs");
    }
  }

  private static void marshallObject(Parcel out, Object toMarshall) {
    if (toMarshall == null) {
      out.writeInt(0);
    } else if ( toMarshall instanceof String ) {
      out.writeInt(1);
      out.writeString((String) toMarshall);
    } else if ( toMarshall instanceof Integer ) {
      out.writeInt(2);
      out.writeInt((Integer) toMarshall);
    } else if ( toMarshall instanceof Boolean ) {
      if ( (Boolean) toMarshall ) {
        out.writeInt(3);
      } else {
        out.writeInt(4);
      }
    } else if ( toMarshall instanceof Double ) {
      out.writeInt(5);
      out.writeDouble((Double) toMarshall);
    } else if ( toMarshall instanceof Float ) {
      out.writeInt(6);
      out.writeFloat((Float) toMarshall);
    } else if ( toMarshall instanceof Long ) {
      out.writeInt(7);
      out.writeLong((Long) toMarshall);
    } else {
      throw new IllegalStateException("should have been prevented in constructor");
    }
  }

  private static Object unmarshallObject(Parcel in) {
    int dataType = in.readInt();
    switch(dataType) {
    case 0:
      return null;
    case 1:
      return in.readString();
    case 2:
      return in.readInt();
    case 3:
      return Boolean.TRUE;
    case 4:
      return Boolean.FALSE;
    case 5:
      return in.readDouble();
    case 6:
      return in.readFloat();
    case 7:
      return in.readLong();
    default:
      throw new IllegalStateException("should have been prevented in constructor");
    }
  }

  protected BindArgs(Parcel in) {
    int dataCount = in.readInt();
    if (dataCount < 0) {
      bindArgs = null;
    } else {
      Object[] result = new Object[dataCount];
      for ( int i = 0 ; i < dataCount ; ++i ) {
        result[i] = unmarshallObject(in);
      }
      bindArgs = result;
    }
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    if (bindArgs == null) {
      dest.writeInt(-1);
    } else {
      dest.writeInt(bindArgs.length);
      for (Object bindArg : bindArgs) {
        marshallObject(dest, bindArg);
      }
    }
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<BindArgs> CREATOR = new Creator<BindArgs>() {
    @Override public BindArgs createFromParcel(Parcel in) {
      return new BindArgs(in);
    }

    @Override public BindArgs[] newArray(int size) {
      return new BindArgs[size];
    }
  };
}
