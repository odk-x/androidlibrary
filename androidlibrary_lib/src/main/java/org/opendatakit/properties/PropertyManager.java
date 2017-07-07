/*
 * Copyright (C) 2009-2013 University of Washington
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import java.util.HashMap;
import java.util.Locale;

/**
 * Used to return device properties to JavaRosa
 * <p>
 * Used in SubmissionProvider, AbsBaseWebActivity, MainMenuActivity, OdkCommon,
 * DynamicPropertiesCallback, DoActionUtils
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("WeakerAccess")
public class PropertyManager {

  public static final String DEVICE_ID_PROPERTY = "deviceid";
  public static final String OR_DEVICE_ID_PROPERTY = "uri:deviceid";
  /**
   * These properties are dynamic and accessed through the
   * DynamicPropertiesInterface. As with all property names,
   * they are compared in a case-insensitive manner.
   */

  public static final String APP_NAME = "appName";
  public static final String LOCALE = "locale";
  public static final String ACTIVE_USER = "active_user";
  // instanceDirectory -- directory containing media files for current instance
  public static final String INSTANCE_DIRECTORY = "instancedirectory";
  // uriFragmentNewFile -- the appName-relative uri for a non-existent file with the given extension
  public static final String URI_FRAGMENT_NEW_INSTANCE_FILE_WITHOUT_COLON = "urifragmentnewinstancefile";
  public static final String URI_FRAGMENT_NEW_INSTANCE_FILE =
      URI_FRAGMENT_NEW_INSTANCE_FILE_WITHOUT_COLON + ":";

  public final HashMap<String, String> mProperties;

  /**
   * Constructor used within the Application object to create a singleton of the
   * property manager. Access it through
   * Survey.getInstance().getPropertyManager()
   *
   * @param context the context/activity to execute in
   */
  @SuppressLint("HardwareIds")
  public PropertyManager(Context context) {

    mProperties = new HashMap<String, String>();
    String orDeviceId = null;
    String deviceId = null;

    // if it is still null, use ANDROID_ID
    if (deviceId == null) {
      deviceId = Settings.Secure
          .getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
      orDeviceId = Settings.Secure.ANDROID_ID + ":" + deviceId;
    }

    mProperties.put(DEVICE_ID_PROPERTY, deviceId);
    mProperties.put(OR_DEVICE_ID_PROPERTY, orDeviceId);

    String value;
  }

  /**
   * Used in SubmissionProvider, DoActionUtils, AbsBaseWebActivity, MainMenuActivity, OdkCommon
   *
   * @param rawPropertyName the name of the property
   * @param callback        a callback capable of getting the requested property
   * @return the requested property
   */
  @SuppressWarnings("unused")
  public String getSingularProperty(String rawPropertyName, DynamicPropertiesInterface callback) {

    String propertyName = rawPropertyName.toLowerCase(Locale.ENGLISH);

    // retrieve the dynamic values via the callback...
    if (ACTIVE_USER.equals(propertyName)) {
      return callback.getActiveUser();
    } else if (LOCALE.equals(propertyName)) {
      return callback.getLocale();
    } else if (APP_NAME.equals(propertyName)) {
      String value = callback.getAppName();
      if (value == null)
        return null;
      return value;
    } else if (INSTANCE_DIRECTORY.equals(propertyName)) {
      String value = callback.getInstanceDirectory();
      if (value == null)
        return null;
      return value;
    } else if (propertyName.startsWith(URI_FRAGMENT_NEW_INSTANCE_FILE_WITHOUT_COLON)) {
      // grab the requested extension, if any...
      String ext;
      if (propertyName.startsWith(URI_FRAGMENT_NEW_INSTANCE_FILE)) {
        ext = rawPropertyName.substring(URI_FRAGMENT_NEW_INSTANCE_FILE.length());
      } else {
        ext = "";
      }
      return callback.getUriFragmentNewInstanceFile(mProperties.get(OR_DEVICE_ID_PROPERTY), ext);
    } else {
      return mProperties.get(propertyName);
    }
  }

  /**
   * Used in DynamicPropertiesCallback
   */
  interface DynamicPropertiesInterface {
    String getActiveUser();

    String getLocale();

    String getAppName();

    String getInstanceDirectory();

    String getUriFragmentNewInstanceFile(String uriDeviceId, String extension);
  }
}
