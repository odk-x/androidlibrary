/*
 * Copyright (C) 2013 University of Washington
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

import org.opendatakit.properties.PropertyManager.DynamicPropertiesInterface;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.io.FileFilter;

/**
 * Implements property access methods that return dynamic values
 * <p>
 * Used in OdkCommon, AbsBaseWebActivity, MainMenuActivity, DoActionUtils, SubmissionProvider
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("unused")
public class DynamicPropertiesCallback implements DynamicPropertiesInterface {

  private final String appName;
  private final String tableId;
  private final String instanceId;
  private final String activeUser;
  private final String locale;

  public DynamicPropertiesCallback(String appName, String tableId, String instanceId,
      String activeUser, String locale) {
    this.appName = appName;
    this.tableId = tableId;
    this.instanceId = instanceId;
    this.activeUser = activeUser;
    this.locale = locale;
  }

  @Override
  public String getActiveUser() {
    // Get the activeUser as determined by
    // the authentication type in use (property) and the
    // value of the username or account properties.
    return activeUser;
  }

  @Override
  public String getLocale() {
    // Get the locale in use
    return locale;
  }

  @Override
  public String getAppName() {
    return appName;
  }

  @Override
  public String getInstanceDirectory() {
    if (tableId == null) {
      throw new IllegalStateException(
          "getInstanceDirectory() unexpectedly invoked outside of a form.");
    }
    return ODKFileUtils.getInstanceFolder(appName, tableId, instanceId);
  }

  @Override
  public String getUriFragmentNewInstanceFile(String uriDeviceId, String extension) {
    if (tableId == null) {
      throw new IllegalStateException(
          "getUriFragmentNewInstanceFile(...) unexpectedly invoked outside of a form.");
    }
    String mediaPath = ODKFileUtils.getInstanceFolder(appName, tableId, instanceId);
    File f = new File(mediaPath);
    if (!f.exists() && !f.mkdirs()) {
      throw new RuntimeException("Could not create instance folder " + f.getPath());
    }
    String chosenFileName;
    while (true) {
      String candidate = Long.toString(System.currentTimeMillis()) + "_" + uriDeviceId;
      final String fileName = candidate.replaceAll("[\\p{Punct}\\p{Space}]", "_");
      chosenFileName = fileName;
      // see if there are any files with this fileName, with or without file
      // extensions
      File[] files = f.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          String name = pathname.getName();
          if (!name.startsWith(fileName)) {
            return false;
          }
          // strip of any extension...
          int idx = name.indexOf('.');
          if (idx != -1) {
            String firstPart = name.substring(0, idx);
            if (firstPart.equals(fileName)) {
              return true;
            }
          } else if (name.equals(fileName)) {
            return true;
          }
          return false;
        }
      });
      if (files == null || files.length == 0) {
        break;
      }
    }
    String filePath = mediaPath + File.separator + chosenFileName + (extension != null
        && !extension.isEmpty() ? "." + extension : "");
    return ODKFileUtils.asUriFragment(appName, new File(filePath));
  }
}