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
import org.opendatakit.androidlibrary.R;
import org.opendatakit.utilities.StaticStateManipulator;

import java.util.TreeMap;

/****************************************************************
 * CommonToolPropertiesSingletonFactory (opendatakit.properties)
 */
public final class CommonToolProperties {

  /**
   * Used in TableUtil
   */
  @SuppressWarnings("WeakerAccess")
  public static final int DEFAULT_FONT_SIZE = 16;

  /**
   * Used in ODKServicesPropertyUtils, AbsBaseWebActivity, MainMenuActivity
   */
  @SuppressWarnings("unused")
  public static final String ANONYMOUS_USER = "anonymous";

  // TODO finish linting this file

  /*******************
   * Garbage 'properties' to control PreferencesCategories and PreferencesScreens
   * Used in AdminPasswordSettingsFragment, ServerSettingsFragment
   */
  @SuppressWarnings("unused")
  public static final String GROUPING_PASSWORD_SCREEN = "group.common.password_screen";

  /**
   * Used in DeviceSettingsFragment
   */
  @SuppressWarnings("unused")
  public static final String GROUPING_DEVICE_CATEGORY = "group.common.device";

  /**
   * Used in ServerSettingsFragment
   */
  @SuppressWarnings("unused")
  public static final String GROUPING_SERVER_CATEGORY = "group.common.server";

  /**
   * Used in TablesSettingsFragment
   */
  @SuppressWarnings("unused")
  public static final String GROUPING_TOOL_TABLES_CATEGORY = "group.common.tables";

  //******************
  // General Settings

  // server identity
  /**
   * ODK 2.0 server URL
   * Used in SyncExecutionContext, SyncFragment, VerifyServerSettingsFragment,
   * InstanceUploaderTask, LoginFragment, ServerSettingsFragment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_SYNC_SERVER_URL = "common.sync_server_url";

  public static final String KEY_AUTHENTICATION_TYPE = "common.auth_credentials";

  // account identity
  /**
   * ODK Aggregate username
   * Used in VerifyServerSettingsFragment, SyncFragment, LoginFragment, InstanceUploaderTask,
   * AbsBaseWebActivity, ServerSettingsFragment, SubmissionProvider, MainMenuActivity,
   * ODKServicesPropertyUtils, SyncExecutionContext, OdkCommon, DoActionUtils
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_USERNAME = "common.username";
  /** User id as given by the server. This is the getActiveUser() value */
  public static final String KEY_AUTHENTICATED_USER_ID = "common.userid";

  // general settings

  /**
   * Used in SyncActivity
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_FIRST_LAUNCH = "common.first_launch";

  /**
   * null if we should use Android system locale
   * Used in PropertiesSingleton, CommonTranslationsLocaleScreen, OdkCommon, DeviceSettingsFragment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_COMMON_TRANSLATIONS_LOCALE = "common.common_translations_locale";

  public static final String KEY_FONT_SIZE = "common.font_size";

  /**
   * Used in SplashScreenActivity, DeviceSettingsFragment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_SHOW_SPLASH = "common.show_splash";

  /**
   * Used in DeviceSettingsFragment, SplashScreenActivity
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_SPLASH_PATH = "common.splash_path";

  /**
   * ODK Tables specific
   * Used in MainActivity, TablesSettingsFragment
   */
  @SuppressWarnings("unused")
  public static final String KEY_USE_HOME_SCREEN = "tables.custom_home_screen";

  /**
   * Used to perform initialization common to all tools
   */
  public static final String KEY_COMMON_INITIALIZATION = "common.initialization";

  //******************
  // Admin Settings
  /**
   * These four used in ServerSettingsFragment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_CHANGE_SYNC_SERVER = "common.change_sync_server";
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_CHANGE_AUTHENTICATION_TYPE = "common.change_auth_credentials";
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_CHANGE_GOOGLE_ACCOUNT = "common.change_google_account";
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_CHANGE_USERNAME_PASSWORD = "common.change_username_password";

  /**
   * These two used in DeviceSettingsFragment
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_CHANGE_FONT_SIZE = "common.change_font_size";
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_CHANGE_SPLASH_SETTINGS = "common.change_splash_settings";

  /**
   * Used in TablesSettingsFragment
   */
  @SuppressWarnings("unused")
  public static final String KEY_CHANGE_USE_HOME_SCREEN = "tables.change_custom_home_screen";

  //**********************************************************************
  // Secure properties (always move into appName-secure location).
  // e.g., authentication codes and passwords.

  ////////////////////
  // General Settings
  /**
   * Roles that the user is known to have. JSON encoded list of strings
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_ROLES_LIST = "common.roles";
  /**
   * Default group of the current user.
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_DEFAULT_GROUP = "common.default_group";
  /**
   * List of all users and their roles on the server.
   * JSON encoded list of { "user_id": "...", "full_name": "...", "roles": ["...","...",...]}
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_USERS_LIST = "common.users";
  /**
   *  Unique code for this installation. Cleared only via device's app settings
   */
  public static final String KEY_INSTALLATION_ID = "common.installationId";
  /**
   * Flag indicating that we are running in non-secure communications mode.
   * Used for developer testing.
   */
  public static final String KEY_ALLOW_NON_SECURE_AUTHENTICATION = "common.non_secure_authentication";

  /**
   *  Admin Settings password
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_ADMIN_PW = "common.admin_pw";

  private static CommonPropertiesSingletonFactory factory = null;
  /**
   * ODK Aggregate password
   * Used in VerifyServerSettingsFragment, SyncFragment, InstanceUploaderTask,
   * ServerSettingsFragment, LoginFragment, SyncExecutionContext
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_PASSWORD = "common.password";

  /**
   * ODK Tables : Key used to store sort order as Device Property
   */
  @SuppressWarnings("WeakerAccess")
  public static final String KEY_PREF_TABLES_SORT_BY_ORDER = "org.opendatakit.tables.list.sortbyorder";

  // key used to sort forms in survery
  public static final String KEY_SURVEY_SORT_ORDER = "survey.sort_order";

  // key used to store the timestamp of last successful sync in SyncFragment
  public static final String KEY_LAST_SYNC_INFO = "common.last_sync_info";

  // key used to store the sync type spinner's preference in SyncFragment
  public static final String KEY_SYNC_ATTACHMENT_STATE = "common.sync_attachment_state";

  static {
    // register a state-reset manipulator for 'commonPropertiesSingletonFactory' field.
    StaticStateManipulator.get().register(new StaticStateManipulator.IStaticFieldManipulator() {

      @Override
      public void reset() {
        factory = null;
      }

    });
  }

  /**
   * Do not instantiate this class
   */
  private CommonToolProperties() {
  }

  public static void accumulateProperties(Context context,
      TreeMap<String, String> generalProperties, TreeMap<String, String> deviceProperties,
      TreeMap<String, String> secureProperties) {

    // Set default values as necessary

    // the properties managed through the general settings pages.
    if (generalProperties != null) {
      generalProperties
          .put(KEY_SYNC_SERVER_URL, context.getString(R.string.default_sync_server_url));
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
    }
    // device properties can be set on a per-device basis and are not
    // over-written by the server configuration. Admins can manually set
    // these via the generalProperties; that is not secure as these values
    // will be left in the syncable file.
    if (deviceProperties != null) {
      deviceProperties.put(KEY_AUTHENTICATION_TYPE, "none");
      deviceProperties.put(KEY_USERNAME, "");
      deviceProperties.put(KEY_COMMON_INITIALIZATION, "");
	  deviceProperties.put(KEY_FIRST_LAUNCH, "true");
      deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("survey"), "");
      deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("scan"), "");
      deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("tables"), "");
      deviceProperties.put(PropertiesSingleton.toolVersionPropertyName("sensors"), "");
      deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("survey"), "");
      deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("scan"), "");
      deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("tables"), "");
      deviceProperties.put(PropertiesSingleton.toolFirstRunPropertyName("sensors"), "");
      deviceProperties.put(KEY_SURVEY_SORT_ORDER,"sortByName");
      deviceProperties.put(KEY_PREF_TABLES_SORT_BY_ORDER,"SORT_ASC");
    }
    // handle the secure properties. If these are in the incoming syncable general
    // property file, those values will be used to initialize these fields (if there is not an
    // existing value set for them). BUT: they won't be removed from that syncable file.
    //
    // I.e., that is a lazy way to distribute these values, but it is not robustly secure.
    //
    if (secureProperties != null) {
      secureProperties.put(KEY_PASSWORD, "");
      secureProperties.put(KEY_AUTHENTICATED_USER_ID, "");
      secureProperties.put(KEY_DEFAULT_GROUP, "");
      secureProperties.put(KEY_ROLES_LIST, "");
      secureProperties.put(KEY_USERS_LIST, "");
      secureProperties.put(KEY_INSTALLATION_ID, "");
      secureProperties.put(KEY_ALLOW_NON_SECURE_AUTHENTICATION, "");
      secureProperties.put(KEY_ADMIN_PW, "");
    }
  }

  public static synchronized PropertiesSingleton get(Context context, String appName) {
    if (factory == null) {
      TreeMap<String, String> generalProperties = new TreeMap<>();
      TreeMap<String, String> deviceProperties = new TreeMap<>();
      TreeMap<String, String> secureProperties = new TreeMap<>();

      CommonToolProperties
          .accumulateProperties(context, generalProperties, deviceProperties, secureProperties);

      factory = new CommonPropertiesSingletonFactory(generalProperties, deviceProperties,
          secureProperties);
    }
    return factory.getSingleton(context, appName);
  }

  /**
   * Used in AndroidShortcuts, CommonToolProperties, ODKWebView
   * @param context the context to get properties from
   * @param appName the app name
   * @return the font size to use
   */
  @SuppressWarnings("unused")
  public static int getQuestionFontsize(Context context, String appName) {
    PropertiesSingleton props = CommonToolProperties.get(context, appName);
    Integer question_font = props.getIntegerProperty(CommonToolProperties.KEY_FONT_SIZE);
    return question_font == null ? CommonToolProperties.DEFAULT_FONT_SIZE : question_font;
  }

  private static final class CommonPropertiesSingletonFactory extends PropertiesSingletonFactory {

    private CommonPropertiesSingletonFactory(TreeMap<String, String> generalDefaults,
        TreeMap<String, String> deviceDefaults, TreeMap<String, String> secureDefaults) {
      super(generalDefaults, deviceDefaults, secureDefaults);
    }
  }

}
