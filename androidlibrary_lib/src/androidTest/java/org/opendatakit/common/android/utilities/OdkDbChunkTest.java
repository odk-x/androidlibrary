/*
 * Copyright (C) 2016 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.common.android.utilities;

import android.os.Bundle;
import android.os.Parcel;
import android.test.AndroidTestCase;

import org.opendatakit.common.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.database.service.OdkDbChunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class OdkDbChunkTest extends AndroidTestCase {

  /**
   * Test Data
   **/
  private static int largeChunkSize = 946176;
  private static int smallChunkSize = 10;

  String[] testData = { "Miscellaneous", "test", "data", "to", "parcel", "and", "unpack" };

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  public void testConvertSerializableNull() {
    String[] nullData = null;
    try {
      assertNull(OdkDbChunkUtil.convertToChunks(nullData, largeChunkSize));
    } catch (IOException e) {
      fail("Should not throw exception on null input");
      return;
    }
  }

  public void testRebuildSerializableNull() {
    List<OdkDbChunk> nullList = null;

    try {
      assertNull(OdkDbChunkUtil.rebuildFromChunks(nullList, String[].class));
    } catch (IOException e) {
      fail("Should not throw exception on null");
      return;
    } catch (ClassNotFoundException e) {
      fail("Should not throw exception on null");
      return;
    }
  }

  public void testConvertSerializableToAndFromChunk() {
    List<OdkDbChunk> chunks;

    try {
      chunks = OdkDbChunkUtil.convertToChunks(testData, largeChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
      return;
    }

    assertTrue("Unexpected number of chunks", chunks.size() == 1);

    OdkDbChunk chunk = chunks.get(0);
    assertFalse("Single chunk shouldn't point to another", chunk.hasNextID());

    String[] results;
    try {
      results = OdkDbChunkUtil.rebuildFromChunks(chunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
      return;
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
      return;
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

  public void testConvertSerializableToAndFromChunks() {
    List<OdkDbChunk> chunks;

    try {
      chunks = OdkDbChunkUtil.convertToChunks(testData, smallChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
      return;
    }

    assertTrue("Unexpected number of chunks", chunks.size() > 1);

    // Test chunk list pointers
    ListIterator<OdkDbChunk> iterator = chunks.listIterator();
    OdkDbChunk currChunk = iterator.next();
    assertTrue("Missing pointer to next chunk", currChunk.hasNextID());
    UUID nextChunkId = currChunk.getNextID();
    while(iterator.hasNext()) {
      currChunk = iterator.next();
      assertEquals("Invalid chunk next pointers", currChunk.getThisID(), nextChunkId);

      if (iterator.hasNext()) {
        assertTrue("Missing chunk next pointers", currChunk.hasNextID());
        nextChunkId = currChunk.getNextID();
      }
    }

    // Test unpack
    String[] results;
    try {
      results = OdkDbChunkUtil.rebuildFromChunks(chunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
      return;
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
      return;
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

  public void testConvertParcelableNull() {
    Bundle nullData = null;

    assertNull(OdkDbChunkUtil.convertToChunks(nullData, largeChunkSize));
  }

  public void testRebuildParcelableNull() {
    List<OdkDbChunk> nullList = null;

    assertNull(OdkDbChunkUtil.rebuildFromChunks(nullList, Bundle.CREATOR));
  }

  public void testConvertParcelableToAndFromChunk() {
    List<OdkDbChunk> chunks;
    Bundle parcelableTestData = new Bundle();
    parcelableTestData.putStringArray("testData", testData);

    chunks = OdkDbChunkUtil.convertToChunks(parcelableTestData, largeChunkSize);

    assertTrue("Unexpected number of chunks", chunks.size() == 1);

    OdkDbChunk chunk = chunks.get(0);
    assertFalse("Single chunk shouldn't point to another", chunk.hasNextID());

    Bundle results = OdkDbChunkUtil.rebuildFromChunks(chunks, Bundle.CREATOR);

    assertEquals("Unexpected unpacked bundle size", results.size(), parcelableTestData.size());
    assertTrue("Data unpack error", results.containsKey("testData"));
    String[] resultsTestData = results.getStringArray("testData");

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], resultsTestData[i]);
    }
  }

  public void testConvertParcelableToAndFromChunks() {
    List<OdkDbChunk> chunks;
    Bundle parcelableTestData = new Bundle();
    parcelableTestData.putStringArray("testData", testData);

    chunks = OdkDbChunkUtil.convertToChunks(parcelableTestData, smallChunkSize);

    assertTrue("Unexpected number of chunks", chunks.size() > 1);

    // Test chunk list pointers
    ListIterator<OdkDbChunk> iterator = chunks.listIterator();
    OdkDbChunk currChunk = iterator.next();
    assertTrue("Missing pointer to next chunk", currChunk.hasNextID());
    UUID nextChunkId = currChunk.getNextID();
    while(iterator.hasNext()) {
      currChunk = iterator.next();
      assertEquals("Invalid chunk next pointers", currChunk.getThisID(), nextChunkId);

      if (iterator.hasNext()) {
        assertTrue("Missing chunk next pointers", currChunk.hasNextID());
        nextChunkId = currChunk.getNextID();
      }
    }

    // Test unpack
    Bundle results = OdkDbChunkUtil.rebuildFromChunks(chunks, Bundle.CREATOR);

    assertEquals("Unexpected unpacked bundle size", results.size(), parcelableTestData.size());
    assertTrue("Data unpack error", results.containsKey("testData"));
    String[] resultsTestData = results.getStringArray("testData");

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], resultsTestData[i]);
    }
  }

  public void testChunkParcelation() {
    List<OdkDbChunk> chunks;

    try {
      chunks = OdkDbChunkUtil.convertToChunks(testData, largeChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
      return;
    }

    assertTrue("Unexpected number of chunks", chunks.size() == 1);

    OdkDbChunk chunk = chunks.get(0);

     /*
     * Marshall the test data
     */
    Parcel p = Parcel.obtain();
    chunk.writeToParcel(p, 0);
    byte[] bytes = p.marshall();
    p.recycle();

    p = Parcel.obtain();
    p.unmarshall(bytes, 0, bytes.length);
    p.setDataPosition(0);

    OdkDbChunk result = OdkDbChunk.CREATOR.createFromParcel(p);
    List<OdkDbChunk> resultChunks = new ArrayList<OdkDbChunk>();
    resultChunks.add(result);

    String[] results;
    try {
      results = OdkDbChunkUtil.rebuildFromChunks(resultChunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
      return;
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
      return;
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

  public void testChunksParcelation() {
    List<OdkDbChunk> chunks;

    try {
      chunks = OdkDbChunkUtil.convertToChunks(testData, smallChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
      return;
    }

    assertTrue("Unexpected number of chunks", chunks.size() > 1);

    /*
     * Marshall the test data
     */
    List<byte[]> marshalledChunks = new LinkedList<byte[]>();
    List<OdkDbChunk> resultChunks = new LinkedList<OdkDbChunk>();
    ListIterator<OdkDbChunk> iterator = chunks.listIterator();
    while (iterator.hasNext()) {
      OdkDbChunk chunk = iterator.next();

      Parcel p = Parcel.obtain();
      chunk.writeToParcel(p, 0);
      byte[] bytes = p.marshall();
      marshalledChunks.add(bytes);
      p.recycle();

      p = Parcel.obtain();
      p.unmarshall(bytes, 0, bytes.length);
      p.setDataPosition(0);
      OdkDbChunk result = OdkDbChunk.CREATOR.createFromParcel(p);
      resultChunks.add(result);
    }

    // Test unpack
    String[] results;
    try {
      results = OdkDbChunkUtil.rebuildFromChunks(chunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
      return;
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
      return;
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

}
