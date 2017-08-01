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

import android.os.Parcel;

public final class MarshallUtil {

  /**
   * This class should never be instantiated
   */
  private MarshallUtil() {
    throw new IllegalStateException("Never Instantiate this static class");
  }

  public static void marshallStringArray(Parcel out, String[] toMarshall) {
    if (toMarshall == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(toMarshall.length);
      out.writeStringArray(toMarshall);
    }
  }

  public static String[] unmarshallStringArray(Parcel in) {
    int dataCount = in.readInt();
    if (dataCount < 0) {
      return null;
    }
    String[] result = new String[dataCount];
    in.readStringArray(result);
    return result;
  }
}
