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

package org.opendatakit.database.service;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class DbChunk implements Parcelable {

  private byte[] data = null;
  private UUID thisID = null;
  private UUID nextID = null;

  public DbChunk(byte[] data, UUID thisID) {
    if ( data == null ) {
      throw new IllegalArgumentException("null data");
    }
    this.data = data;
    this.thisID = thisID;
  }

  public DbChunk(Parcel in) {
    int dataLength = in.readInt();
    if ( dataLength < 0 ) {
      throw new IllegalArgumentException("invalid data length");
    }

    data = new byte[dataLength];
    in.readByteArray(data);

    thisID = (UUID) in.readSerializable();

    byte hasNext = in.readByte();
    if (hasNext > 0) {
      nextID = (UUID) in.readSerializable();
    }
  }

  public byte[] getData() {
    return this.data;
  }

  public UUID getThisID() {
    return this.thisID;
  }

  public UUID getNextID() {
    return this.nextID;
  }

  public boolean hasNextID() {
    return this.nextID != null;
  }

  public void setNextID(UUID nextID) {
    this.nextID = nextID;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(data.length);
    dest.writeByteArray(data);
    dest.writeSerializable(thisID);

    if (nextID == null) {
      dest.writeByte((byte) 0);
    } else {
      dest.writeByte((byte) 1);
      dest.writeSerializable(nextID);
    }

  }

  public static final Parcelable.Creator<DbChunk> CREATOR =
      new Parcelable.Creator<DbChunk>() {
    public DbChunk createFromParcel(Parcel in) {
      return new DbChunk(in);
    }

    public DbChunk[] newArray(int size) {
      return new DbChunk[size];
    }
  };
}

