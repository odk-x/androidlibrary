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

package org.opendatakit.utilities;

import android.os.Bundle;
import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.database.service.DbChunk;
import org.opendatakit.database.utilities.DbChunkUtil;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class OdkDbChunkTest {

  /**
   * Test Data
   **/
  private static final int largeChunkSize = 946176;
  private static final int smallChunkSize = 10;

  private final String[] testData = { "Miscellaneous", "test", "data", "to", "parcel", "and", "unpack" };

  @Before
  public void setUp() {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testConvertSerializableNull() {
    String[] nullData = null;
    try {
      assertNull(DbChunkUtil.convertToChunks(nullData, largeChunkSize));
    } catch (IOException e) {
      fail("Should not throw exception on null input");
    }
  }

  @Test
  public void testRebuildSerializableNull() {
    List<DbChunk> nullList = null;

    try {
      assertNull(DbChunkUtil.rebuildFromChunks(nullList, String[].class));
    } catch (IOException | ClassNotFoundException e) {
      fail("Should not throw exception on null");
    }
  }

  @Test
  public void testConvertSerializableToAndFromChunk() {
    List<DbChunk> chunks = null;

    try {
      chunks = DbChunkUtil.convertToChunks(testData, largeChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
    }

    if (chunks == null) {
      fail("Failed to convert serializable to chunks");
    }

    assertEquals("Unexpected number of chunks", 1, chunks.size());

    DbChunk chunk = chunks.get(0);
    assertFalse("Single chunk shouldn't point to another", chunk.hasNextID());

    String[] results = null;
    try {
      results = DbChunkUtil.rebuildFromChunks(chunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
    }

    if (results == null) {
      fail("Failed to rebuild serializable from chunks");
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

  @Test
  public void testConvertSerializableToAndFromChunks() {
    List<DbChunk> chunks = null;

    try {
      chunks = DbChunkUtil.convertToChunks(testData, smallChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
    }

    if (chunks == null) {
      fail("Failed to convert serializable to chunks");
    }

    assertTrue("Unexpected number of chunks", chunks.size() > 1);

    // Test chunk list pointers
    ListIterator<DbChunk> iterator = chunks.listIterator();
    DbChunk currChunk = iterator.next();
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
    String[] results = null;
    try {
      results = DbChunkUtil.rebuildFromChunks(chunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
    }

    if (results == null) {
      fail("Failed to rebuild serializable from chunks");
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

  @Test
  public void testConvertParcelableNull() {
    Bundle nullData = null;

    assertNull(DbChunkUtil.convertToChunks(nullData, largeChunkSize));
  }

  @Test
  public void testRebuildParcelableNull() {
    List<DbChunk> nullList = null;

    assertNull(DbChunkUtil.rebuildFromChunks(nullList, Bundle.CREATOR));
  }

  @Test
  public void testConvertParcelableToAndFromChunk() {
    List<DbChunk> chunks;
    Bundle parcelableTestData = new Bundle();
    parcelableTestData.putStringArray("testData", testData);

    chunks = DbChunkUtil.convertToChunks(parcelableTestData, largeChunkSize);


    if (chunks == null) {
      fail("Failed to convert serializable to chunks");
    }

    assertEquals("Unexpected number of chunks", 1, chunks.size());

    DbChunk chunk = chunks.get(0);
    assertFalse("Single chunk shouldn't point to another", chunk.hasNextID());

    Bundle results = DbChunkUtil.rebuildFromChunks(chunks, Bundle.CREATOR);

    if (results == null) {
      fail("Failed to rebuild serializable from chunks");
    }

    assertEquals("Unexpected unpacked bundle size", results.size(), parcelableTestData.size());
    assertTrue("Data unpack error", results.containsKey("testData"));
    String[] resultsTestData = results.getStringArray("testData");

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], resultsTestData[i]);
    }
  }

  @Test
  public void testConvertParcelableToAndFromChunks() {
    List<DbChunk> chunks;
    Bundle parcelableTestData = new Bundle();
    parcelableTestData.putStringArray("testData", testData);

    chunks = DbChunkUtil.convertToChunks(parcelableTestData, smallChunkSize);


    if (chunks == null) {
      fail("Failed to convert serializable to chunks");
    }

    assertTrue("Unexpected number of chunks", chunks.size() > 1);

    // Test chunk list pointers
    ListIterator<DbChunk> iterator = chunks.listIterator();
    DbChunk currChunk = iterator.next();
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
    Bundle results = DbChunkUtil.rebuildFromChunks(chunks, Bundle.CREATOR);

    if (results == null) {
      fail("Failed to rebuild serializable from chunks");
    }

    assertEquals("Unexpected unpacked bundle size", results.size(), parcelableTestData.size());
    assertTrue("Data unpack error", results.containsKey("testData"));
    
    String[] resultsTestData = results.getStringArray("testData");
    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], resultsTestData[i]);
    }
  }

  @Test
  public void testChunkParcelation() {
    List<DbChunk> chunks = null;

    try {
      chunks = DbChunkUtil.convertToChunks(testData, largeChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
    }

    if (chunks == null) {
      fail("Failed to convert serializable to chunks");
    }

    assertEquals("Unexpected number of chunks", 1, chunks.size());

    DbChunk chunk = chunks.get(0);

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

    DbChunk result = DbChunk.CREATOR.createFromParcel(p);
    List<DbChunk> resultChunks = new ArrayList<>();
    resultChunks.add(result);

    String[] results = null;
    try {
      results = DbChunkUtil.rebuildFromChunks(resultChunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
    }

    if (results == null) {
      fail("Failed to rebuild serializable from chunks");
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

  @Test
  public void testChunksParcelation() {
    List<DbChunk> chunks = null;

    try {
      chunks = DbChunkUtil.convertToChunks(testData, smallChunkSize);
    } catch (IOException e) {
      fail("Failed to convert serializable to chunks: " + e.getMessage());
    }


    if (chunks == null) {
      fail("Failed to convert serializable to chunks");
    }

    assertTrue("Unexpected number of chunks", chunks.size() > 1);

    /*
     * Marshall the test data
     */
    List<byte[]> marshalledChunks = new LinkedList<>();
    List<DbChunk> resultChunks = new LinkedList<>();
    for (DbChunk chunk : chunks) {
      Parcel p = Parcel.obtain();
      chunk.writeToParcel(p, 0);
      byte[] bytes = p.marshall();
      marshalledChunks.add(bytes);
      p.recycle();
      p = Parcel.obtain();
      p.unmarshall(bytes, 0, bytes.length);
      p.setDataPosition(0);
      DbChunk result = DbChunk.CREATOR.createFromParcel(p);
      resultChunks.add(result);
    }

    // Test unpack
    String[] results = null;
    try {
      results = DbChunkUtil.rebuildFromChunks(chunks, String[].class);
    } catch (IOException e) {
      fail("Failed to rebuild serializable from chunks: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      fail("Failed to correctly handle generics: " + e.getMessage());
    }

    if (results == null) {
      fail("Failed to rebuild serializable from chunks");
    }

    assertEquals("Unexpected unpacked string array length", results.length, testData.length);

    for (int i = 0; i < testData.length; i++) {
      assertEquals("Data unpack mismatch", testData[i], results[i]);
    }
  }

}
