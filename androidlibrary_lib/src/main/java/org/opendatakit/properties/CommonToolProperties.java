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

package org.opendatakit.properties;

import android.content.Context;
import android.app.Application;

import org.opendatakit.androidlibrary.R;
import org.opendatakit.utilities.StaticStateManipulator;

import java.util.TreeMap;

public class CommonToolProperties {

  public static final int DEFAULT_FONT_SIZE = 16;
  /****************************************************************
   * CommonToolPropertiesSingletonFactory (opendatakit.properties)
   */

   /*******************
    * Garbage 'properties' to control PreferencesCategories and PreferencesScreens
    */
  public static final String GROUPING_PASSWORD_SCREEN = "group.common.password_screen";

  public static final String GROUPING_DEVICE_CATEGORY = "group.common.device";

  public static final String GROUPING_SERVER_CATEGORY = "group.common.server";


  public static final String GROUPING_TOOL_TABLES_CATEGORY = "group.common.tables";

  public static final String GROUPING_TOOL_SURVEY_CATEGORY = "group.common.survey";

  public static final String GROUPING_TOOL_SCAN_CATEGORY = "group.common.scan";

  public static final String GROUPING_TOOL_SENSORS_CATEGORY = "group.common.sensors";

  /*******************
   * General Settings
   */

  // server identity
  /** ODK 2.0 server URL */
  public static final String KEY_SYNC_SERVER_URL = "common.sync_server_url";

  public static final String KEY_AUTHENTICATION_TYPE = "common.auth_credentials";

  // account identity
  /** gmail account */
  public static final String KEY_ACCOUNT = "common.account";
  /** ODK Aggregate username */
  public static final String KEY_USERNAME = "common.username";

  // general settings

  // null if we should use Android system locale
  public static final String KEY_COMMON_TRANSLATIONS_LOCALE = "common.common_translations_locale";

  public static final String KEY_FONT_SIZE = "common.font_size";

  public static final String KEY_SHOW_SPLASH = "common.show_splash";

  public static final String KEY_SPLASH_PATH = "common.splash_path";

  /////////////////////////////////////////////////////////////
  // ODK Tables specific
  public static final String KEY_USE_HOME_SCREEN = "tables.custom_home_screen";

  /*******************
   * Admin Settings 
   */
  public static final String KEY_CHANGE_SYNC_SERVER = "common.change_sync_server";

  public static final String KEY_CHANGE_AUTHENTICATION_TYPE = "common.change_auth_credentials";
  public static final String KEY_CHANGE_GOOGLE_ACCOUNT = "common.change_google_account";
  public static final String KEY_CHANGE_USERNAME_PASSWORD = "common.change_username_password";

  public static final String KEY_CHANGE_FONT_SIZE = "common.change_font_size";

  public static final String KEY_CHANGE_SPLASH_SETTINGS = "common.change_splash_settings";

  public static final String KEY_CHANGE_USE_HOME_SCREEN = "tables.change_custom_home_screen";

  /***********************************************************************
   * Secure properties (always move into appName-secure location).
   * e.g., authentication codes and passwords.
   */

  /*******************
   * General Settings
   */
  
  /** gmail account OAuth 2.0 token */
  public static final String KEY_AUTH = "common.auth";
  /** ODK Aggregate password */
  public static final String KEY_PASSWORD = "common.password";
  /** Roles that the user is known to have. JSON encoded list of strings */
  public static final String KEY_ROLES_LIST = "common.roles";
  /** Default group of the current user. */
  public static final String KEY_DEFAULT_GROUP = "common.default_group";
  /** List of all users and their roles on the server.
   * JSON encoded list of { "user_id": "...", "full_name": "...", "roles": ["...","...",...]} */
  public static final String KEY_USERS_LIST = "common.users";
  /** Admin Settings password */
  public static final String KEY_ADMIN_PW = "common.admin_pw";
  
  public static void accumulateProperties( Context context, 
      TreeMap<String,String> generalProperties, TreeMap<String,String> deviceProperties,
      TreeMap<String,String> secureProperties) {
    
    // Set default values as necessary
    
    // the properties managed through the general settings pages.

    generalProperties.put(KEY_SYNC_SERVER_URL,
       context.getString(R.string.default_sync_server_url));

    generalProperties.put(KEY_FONT_SIZE, Integer.toString(DEFAULT_FONT_SIZE));
    generalProperties.put(KEY_SHOW_SPLASH, "true");
    generalProperties.put(KEY_SPLASH_PATH, "ODK Default");

    // the properties that are managed through the admin settings pages.

    generalProperties.put(KEY_CHANGE_SYNC_SERVER, "true");

    generalProperties.put(KEY_CHANGE_AUTHENTICATION_TYPE, "true");
    generalProperties.put(KEY_CHANGE_GOOGLE_ACCOUNT, "true");
    generalProperties.put(KEY_CHANGE_USERNAME_PASSWORD, "true");

    generalProperties.put(KEY_CHANGE_FONT_SIZE, "true");
    generalProperties.put(KEY_CHANGE_SPLASH_SETTINGS, "true");

    // device properties can be set on a per-device basis and are not
    // over-written by the server configuration. Admins can manually set
    // these via the generalProperties; that is not secure as these values
    // will be left in the syncable file.

    deviceProperties.put(KEY_AUTHENTICATION_TYPE, "none");
    deviceProperties.put(KEY_ACCOUNT, "");
    deviceProperties.put(KEY_USERNAME, "");
    deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("survey"), "");
    deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("scan"), "");
    deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("tables"), "");
    deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("sensors"), "");
    deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("survey"), "");
    deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("scan"), "");
    deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("tables"), "");
    deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("sensors"), "");

    // handle the secure properties. If these are in the incoming syncable general
    // property file, those values will be used to initialize these fields (if there is not an
    // existing value set for them). BUT: they won't be removed from that syncable file.
    //
    // I.e., that is a lazy way to distribute these values, but it is not robustly secure.
    //
    secureProperties.put(KEY_AUTH, "");
    secureProperties.put(KEY_PASSWORD, "");
    secureProperties.put(KEY_DEFAULT_GROUP, "");
    secureProperties.put(KEY_ROLES_LIST, "");
    secureProperties.put(KEY_USERS_LIST, "");
    secureProperties.put(KEY_ADMIN_PW, "");
  }

  private static class CommonPropertiesSingletonFactory extends PropertiesSingletonFactory {

    private CommonPropertiesSingletonFactory(TreeMap<String,String> generalDefaults,
        TreeMap<String,String> deviceDefaults, TreeMap<String,String> secureDefaults) {
      super(generalDefaults, deviceDefaults, secureDefaults);
    }
  }
  
  private static CommonPropertiesSingletonFactory factory = null;
  static {
    // register a state-reset manipulator for 'connectionFactory' field.
    StaticStateManipulator.get().register(50, new StaticStateManipulator.IStaticFieldManipulator() {

      @Override
      public void reset() {
        factory = null;
      }

    });
  }

  public static synchronized PropertiesSingleton get(Context context, String appName) {
    if ( factory == null ) {
      TreeMap<String,String> generalProperties = new TreeMap<String,String>();
      TreeMap<String,String> deviceProperties = new TreeMap<String,String>();
      TreeMap<String,String> secureProperties = new TreeMap<String,String>();
      
      CommonToolProperties.accumulateProperties(context, generalProperties, deviceProperties, secureProperties);
      
      factory = new CommonPropertiesSingletonFactory(generalProperties, deviceProperties, secureProperties);
    }
    return factory.getSingleton(context, appName);
  }

  public static int getQuestionFontsize(Context context, String appName) {
    PropertiesSingleton props = CommonToolProperties.get(context, appName);
    Integer question_font = props.getIntegerProperty(CommonToolProperties.KEY_FONT_SIZE);
    int questionFontsize = (question_font == null) ? CommonToolProperties.DEFAULT_FONT_SIZE : question_font;
    return questionFontsize;
  }

}
