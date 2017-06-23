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

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import java.util.HashMap;
import java.util.Locale;

/**
 * Used to return device properties to JavaRosa
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author mitchellsundt@gmail.com
 */

public class PropertyManager {

  private static final String ANDROID6_FAKE_MAC = "02:00:00:00:00:00";

  public interface DynamicPropertiesInterface {
    String getActiveUser();

    String getLocale();

    String getUsername();

    String getUserEmail();

    String getAppName();

    String getInstanceDirectory();

    String getUriFragmentNewInstanceFile(String uriDeviceId, String extension);
  }

  private final HashMap<String, String> mProperties;

  public final static String DEVICE_ID_PROPERTY = "deviceid"; // imei

  public final static String OR_DEVICE_ID_PROPERTY = "uri:deviceid"; // imei

  /**
   * These properties are dynamic and accessed through the
   * DynamicPropertiesInterface. As with all property names,
   * they are compared in a case-insensitive manner.
   */

  public final static String LOCALE = "locale";

  public final static String ACTIVE_USER = "active_user";

  // username -- current username
  public final static String USERNAME = "username";
  public final static String OR_USERNAME = "uri:username";
  // email -- current account email
  public final static String EMAIL = "email";
  public final static String OR_EMAIL = "uri:email";
  public final static String APP_NAME = "appName";
  // instanceDirectory -- directory containing media files for current instance
  public final static String INSTANCE_DIRECTORY = "instancedirectory";
  // uriFragmentNewFile -- the appName-relative uri for a non-existent file with the given extension
  public final static String URI_FRAGMENT_NEW_INSTANCE_FILE_WITHOUT_COLON = "urifragmentnewinstancefile";
  public final static String URI_FRAGMENT_NEW_INSTANCE_FILE = URI_FRAGMENT_NEW_INSTANCE_FILE_WITHOUT_COLON + ":";
  /**
   * Constructor used within the Application object to create a singleton of the
   * property manager. Access it through
   * Survey.getInstance().getPropertyManager()
   *
   * @param context
   */
  public PropertyManager(Context context) {

    mProperties = new HashMap<String, String>();
    String orDeviceId = null;
    String deviceId = null;

    if (deviceId == null) {
      // no SIM -- WiFi only
      // Retrieve WiFiManager
      WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

      // Get WiFi status
      WifiInfo info = wifi.getConnectionInfo();
      if (info != null) {
        String macId = info.getMacAddress();
        if ( macId != null && !ANDROID6_FAKE_MAC.equals(macId) ) {
          deviceId = macId;
          orDeviceId = "mac:" + deviceId;
        }
      }
    }

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

  public String getSingularProperty(String rawPropertyName, DynamicPropertiesInterface callback) {

    String propertyName = rawPropertyName.toLowerCase(Locale.ENGLISH);

    // retrieve the dynamic values via the callback...
    if (ACTIVE_USER.equals(propertyName)) {
      return callback.getActiveUser();
    } else if (LOCALE.equals(propertyName)) {
      return callback.getLocale();
    } else if (USERNAME.equals(propertyName)) {
      return callback.getUsername();
    } else if (OR_USERNAME.equals(propertyName)) {
      String value = callback.getUsername();
      if (value == null)
        return null;
      return "username:" + value;
    } else if (EMAIL.equals(propertyName)) {
      return callback.getUserEmail();
    } else if (OR_EMAIL.equals(propertyName)) {
      String value = callback.getUserEmail();
      if (value == null)
        return null;
      return "mailto:" + value;
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
      String value = callback
          .getUriFragmentNewInstanceFile(mProperties.get(OR_DEVICE_ID_PROPERTY), ext);
      return value;
    } else {
      return mProperties.get(propertyName);
    }
  }
}
