/*
 * Copyright (C) 2017 University of Washington
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

package org.opendatakit.properties;

import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.OverlappingFileLockException;

/**
 * Manage a fileLock to control read and write access to the properties files.
 *
 * @author mitchellsundt@gmail.com
 */
public class GainPropertiesLock {
  private static final String LOCK_FILENAME = "properties.lock";
  String mAppName;

  FileOutputStream lockStream = null;
  FileLock lockFileLock = null;

  GainPropertiesLock(String appName) {
    mAppName = appName;
    File lockFile = new File(ODKFileUtils.getDataFolder(mAppName), LOCK_FILENAME);
    int count = 0;
    do {
      try {
        lockStream = new FileOutputStream(lockFile);
        ++count;
      } catch (FileNotFoundException e) {
        WebLogger.getLogger(mAppName).i("PropertiesSingleton", "Unable to open lock file");
        if (count > 100) {
          throw new IllegalStateException("Unable to open properties lock file");
        }
        lockStream = null;
        try {
          Thread.sleep(10L);
        } catch (InterruptedException e1) {
          // ignore
        }
      }
    } while (lockStream == null);

    try {
      FileChannel lockChannel = lockStream.getChannel();
      do {
        try {
          lockFileLock = lockChannel.lock();
           ++count;
        } catch (FileLockInterruptionException e) {
          if (count > 100) {
            throw new IllegalStateException("Unable to obtain lock on properties lock file");
          }
          try {
            Thread.sleep(10L);
          } catch (InterruptedException e1) {
            // ignore
          }
        } catch (OverlappingFileLockException e) {
          if (count > 100) {
            throw new IllegalStateException("Unable to obtain lock on properties lock file");
          }
          try {
            Thread.sleep(10L);
          } catch (InterruptedException e1) {
            // ignore
          }
        } catch (IOException e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
          throw new IllegalStateException("Unable to obtain lock on properties lock file");
        }
      } while (lockFileLock == null);
    } finally {
      if (lockFileLock == null) {
        // we were unable to gain the lock -- close file output stream
        try {
          lockStream.close();
        } catch (IOException e) {
          WebLogger.getLogger(mAppName).printStackTrace(e);
        }
      }
    }
  }

   void release() {
    if (lockFileLock != null) {
      try {
        lockFileLock.release();
      } catch (IOException e) {
        WebLogger.getLogger(mAppName).printStackTrace(e);
      }
    }
    if (lockStream != null) {
      try {
        lockStream.close();
      } catch (IOException e) {
        WebLogger.getLogger(mAppName).printStackTrace(e);
      }
    }
  }
}
