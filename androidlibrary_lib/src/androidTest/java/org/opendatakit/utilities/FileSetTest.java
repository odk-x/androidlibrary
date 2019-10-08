/*
 * Copyright (C) 2015 University of Washington
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

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class FileSetTest {
  private static final String APP_NAME = "fileSetTest";
  private static final String TABLE_ID_1 = "myTableId_1";
  private static final String TABLE_ID_2 = "myTableId_2";
  private static final String INSTANCE_ID_1 = "myInstanceId_1";
  private static final String INSTANCE_ID_2 = "myInstanceId_2";
  private static final String INSTANCE_FILENAME = "submission.xml";
  private static final String FILENAME_1 = "foo.jpg";
  private static final String FILENAME_2 = "bar.wav";

  private static final String MIME_1 = "image/jpg";
  private static final String MIME_2 = "audio/wav";

  @Rule
  public GrantPermissionRule writeRuntimePermissionRule = GrantPermissionRule .grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
  @Rule
  public GrantPermissionRule readtimePermissionRule = GrantPermissionRule .grant(Manifest.permission.READ_EXTERNAL_STORAGE);

  @Before
  public void setUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testFileSetSerialization() throws IOException {
    FileSet fileSet = new FileSet("fileSetTest");

    String firstDir = ODKFileUtils.getInstanceFolder(APP_NAME, TABLE_ID_1, INSTANCE_ID_1);
    File instanceFilename = new File(firstDir, INSTANCE_FILENAME);
    File attachment1 = new File(firstDir, FILENAME_1);
    File attachment2 = new File(firstDir, FILENAME_2);

    fileSet.instanceFile = instanceFilename;
    fileSet.addAttachmentFile(attachment1, MIME_1);
    fileSet.addAttachmentFile(attachment2, MIME_2);

    String value = fileSet.serializeUriFragmentList();

    ByteArrayInputStream bis = new ByteArrayInputStream(value.getBytes(Charset.forName("UTF-8")));
    FileSet outSet = FileSet.parse(APP_NAME, bis);

    assertEquals( fileSet.instanceFile, outSet.instanceFile);
    assertEquals( fileSet.attachmentFiles.size(), outSet.attachmentFiles.size());
    assertEquals( fileSet.attachmentFiles.get(0).contentType,
        outSet.attachmentFiles.get(0).contentType);
    assertEquals( fileSet.attachmentFiles.get(0).file, outSet.attachmentFiles.get(0).file);
    assertEquals( fileSet.attachmentFiles.get(1).contentType,
        outSet.attachmentFiles.get(1).contentType);
    assertEquals( fileSet.attachmentFiles.get(1).file, outSet.attachmentFiles.get(1).file);
  }
}
