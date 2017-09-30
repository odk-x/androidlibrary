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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manage a fileLock to control read and write access to the properties files.
 *
 * Used in ODKFileUtils, PropertiesSingleton
 *
 * @author mitchellsundt@gmail.com
 */
class GainPropertiesLock {
  private static final String LOCK_FILENAME = "properties.lock";
  private final String mAppName;
  private final ReentrantLock mAppLock;

  private FileOutputStream lockStream = null;
  private FileLock lockFileLock = null;

  GainPropertiesLock(String appName, ReentrantLock appLock) {
    mAppName = appName;
    mAppLock = appLock;
    File lockFile = new File(ODKFileUtils.getDataFolder(mAppName), LOCK_FILENAME);
    int count = 0;
    do {
      try {
        ++count;
        lockStream = new FileOutputStream(lockFile);
      } catch (FileNotFoundException ignored) {
        WebLogger.getLogger(mAppName).i("PropertiesSingleton", "Unable to open lock file");
        if (count > 100) {
          throw new IllegalStateException("Unable to open properties lock file");
        }
        lockStream = null;
        try {
          Thread.sleep(10L);
        } catch (InterruptedException ignored2) {
          // ignore
        }
      }
    } while (lockStream == null);

    try {
      FileChannel lockChannel = lockStream.getChannel();
      count = 0;
      do {
        try {
          ++count;
          lockFileLock = lockChannel.lock();
        } catch (FileLockInterruptionException | OverlappingFileLockException ignored) {
          if (count > 100) {
            throw new IllegalStateException("Unable to obtain lock on properties lock file");
          }
          try {
            Thread.sleep(10L);
          } catch (InterruptedException ignored2) {
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
    // at this point, we own the file lock.
    // now gain the ReentrantLock for this appName
    mAppLock.lock();
  }

   void release() {
    if (lockFileLock != null) {
      // we hold the file lock
      // release the ReentrantLock
      mAppLock.unlock();
      // and release the file lock.
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
