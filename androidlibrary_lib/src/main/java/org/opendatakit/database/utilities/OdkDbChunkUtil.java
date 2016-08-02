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
import android.os.Parcelable;
import android.util.Log;

import org.opendatakit.database.service.OdkDbChunk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public final class OdkDbChunkUtil {

  private static final String TAG = OdkDbChunkUtil.class.getSimpleName();

  private OdkDbChunkUtil() {
    // This class should never be instantiated
    throw new IllegalStateException("Never Instantiate this static class");
  }

  /**
   * Construct an OdkDbChunk from a serialized parcel.
   *
   * @param sourceData The serialized parcel payload
   * @param dataIndex  Where to start reading bytes
   * @param chunkSize  How many bytes to read
   * @param prevChunk  The previous chunk in the payload. Needed for adding linked list pointers
   * @return The chunk
   */
  private static OdkDbChunk createChunk(byte[] sourceData, int dataIndex, int chunkSize,
      OdkDbChunk prevChunk) {

    UUID chunkID = UUID.randomUUID();

    byte[] chunkData = new byte[chunkSize];
    System.arraycopy(sourceData, dataIndex, chunkData, 0, chunkSize);

    OdkDbChunk chunk = new OdkDbChunk(chunkData, chunkID);
    if (prevChunk != null) {
      prevChunk.setNextID(chunkID);
    }

    return chunk;
  }

  /**
   * Convert a parcelable object into an ordered list of OdkDbChunks of a specified size.
   *
   * @param parcelable The object to be serialized
   * @param chunkSize  The size of the chunks
   * @return Ordered list of serialized chunks
   */
  public static final List<OdkDbChunk> convertToChunks(Parcelable parcelable, int chunkSize) {

    if (parcelable == null || chunkSize <= 0) {
      Log.w(TAG, "convertToChunks: Invalid input. Empty chunk list returned");
      return null;
    }

    // Convert to bytes
    Parcel parcel = Parcel.obtain();
    parcelable.writeToParcel(parcel, 0);
    byte[] bytes = parcel.marshall();
    parcel.recycle();

    return createChunkList(bytes, chunkSize);
  }

  /**
   * Convert a serializable object into an ordered list of OdkDbChunks of a specified size.
   *
   * @param serializable The object to be serialized
   * @param chunkSize    The size of the chunks
   * @return Ordered list of serialized chunks
   */
  public static final List<OdkDbChunk> convertToChunks(Serializable serializable, int chunkSize)
      throws IOException {

    if (serializable == null || chunkSize <= 0) {
      Log.w(TAG, "convertToChunks: Invalid input. Empty chunk list returned");
      return null;
    }

    // Convert to bytes
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutput out = new ObjectOutputStream(bos);
    out.writeObject(serializable);
    byte[] bytes = bos.toByteArray();
    out.close();

    return createChunkList(bytes, chunkSize);
  }

  /**
   * Partition the serialized data into chunks and create the chunk objects
   *
   * @param bytes
   * @param chunkSize
   * @return
   */
  private static List<OdkDbChunk> createChunkList(byte[] bytes, int chunkSize) {
    // Partition bytes into chunks, inserting them into the list along the way
    int dataIndex = 0;
    OdkDbChunk currChunk = null;
    List<OdkDbChunk> chunkList = new LinkedList<>();
    while (dataIndex + chunkSize < bytes.length) {
      currChunk = createChunk(bytes, dataIndex, chunkSize, currChunk);
      chunkList.add(currChunk);

      dataIndex += chunkSize;
    }

    // Create final chunk from remainder data. Return the list
    int remainderSize = bytes.length - dataIndex;
    currChunk = createChunk(bytes, dataIndex, remainderSize, currChunk);
    chunkList.add(currChunk);

    return chunkList;
  }

  /**
   * Rebuild a parcelable object that was converted into a list of chunks by convertToChunks().
   *
   * @param chunks  The ordered list of chunks
   * @param creator The parcelable creator to rebuild the original object
   * @param <T>     The type of the parcelable object to rebuild
   * @return The original object
   */
  public static final <T> T rebuildFromChunks(List<OdkDbChunk> chunks,
      Parcelable.Creator<T> creator) {

    if (chunks == null || chunks.size() == 0 || creator == null) {
      Log.w(TAG, "rebuildFromChunks: Invalid input. Null returned");
      return null;
    }

    byte[] data = getData(chunks);

    // Unmarshall the parcel
    Parcel parcel = Parcel.obtain();
    parcel.unmarshall(data, 0, data.length);
    parcel.setDataPosition(0);

    return creator.createFromParcel(parcel);
  }

  /**
   * Rebuild a serializable object that was converted into a list of chunks by convertToChunks().
   *
   * @param chunks       The ordered list of chunks
   * @param serializable The type to cast the object to
   * @param <T>          The type of the parcelable object to rebuild
   * @return The original object
   */
  @SuppressWarnings("unchecked")
  public static final <T> T rebuildFromChunks(List<OdkDbChunk> chunks, Class<T> serializable)
      throws IOException, ClassNotFoundException {

    if (chunks == null || chunks.size() == 0 || serializable == null) {
      Log.w(TAG, "rebuildFromChunks: Invalid input. Null returned");
      return null;
    }

    byte[] data = getData(chunks);

    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    ObjectInput in = new ObjectInputStream(bis);
    T result = (T) in.readObject();
    bis.close();
    in.close();

    return result;
  }

  /**
   * Extract the bytes from the chunks and concatonate them into a single byte array
   *
   * @param chunks
   * @return
   */
  private static byte[] getData(List<OdkDbChunk> chunks) {
    // Find the size of data
    int dataSize = 0;
    Iterator<OdkDbChunk> iterator = chunks.iterator();
    while (iterator.hasNext()) {
      dataSize += iterator.next().getData().length;
    }

    // Rebuild the byte array
    int dataIndex = 0;
    byte[] data = new byte[dataSize];
    iterator = chunks.iterator();
    while (iterator.hasNext()) {
      byte[] currChunkData = iterator.next().getData();
      System.arraycopy(currChunkData, 0, data, dataIndex, currChunkData.length);
      dataIndex += currChunkData.length;
    }

    return data;
  }

}
