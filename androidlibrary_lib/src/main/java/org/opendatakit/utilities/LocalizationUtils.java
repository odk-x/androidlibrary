/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import org.opendatakit.consts.CharsetConsts;
import org.opendatakit.logging.WebLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LocalizationUtils {

  private static String savedAppName;
  private static final String TAG = LocalizationUtils.class.getSimpleName();
  private static Map<String, Object> commonDefinitions;
  private static Map<String, Map<String, Object>> tableSpecificDefinitionsMap = new HashMap<>();
  private LocalizationUtils() {
  }

  public static String genUUID() {
    return "uuid:" + UUID.randomUUID();
  }

  /**
   * Used in CommonApplication
   */
  @SuppressWarnings("WeakerAccess")
  public static synchronized void clearTranslations() {
    commonDefinitions = null;
    tableSpecificDefinitionsMap.clear();
  }

  private static synchronized void loadTranslations(String appName, String tableId)
      throws IOException {
    if (savedAppName != null && !savedAppName.equals(appName)) {
      clearTranslations();
    }
    savedAppName = appName;
    TypeReference<HashMap<String, Object>> ref = new TypeReference<HashMap<String, Object>>() {
    };
    if (commonDefinitions == null) {
      File commonFile = new File(ODKFileUtils.getCommonDefinitionsFile(appName));
      if (commonFile.exists() && commonFile.isFile()) {
        InputStream stream = null;
        BufferedReader reader = null;
        String value;
        try {
          stream = new FileInputStream(commonFile);
          reader = new BufferedReader(new InputStreamReader(stream, CharsetConsts.UTF_8));
          int ch = reader.read();
          while (ch != -1 && ch != '{') {
            ch = reader.read();
          }

          StringBuilder b = new StringBuilder();
          b.append((char) ch);
          ch = reader.read();
          while (ch != -1) {
            b.append((char) ch);
            ch = reader.read();
          }
          reader.close();
          stream.close();
          value = b.toString().trim();
          if (value.endsWith(";")) {
            value = value.substring(0, value.length() - 1).trim();
          }

        } finally {
          if (reader != null) {
            reader.close();
          } else if (stream != null) {
            stream.close();
          }
        }

        try {
          commonDefinitions = ODKFileUtils.mapper.readValue(value, ref);
        } catch (IOException e) {
          WebLogger.getLogger(appName).printStackTrace(e);
          throw new IllegalStateException("Unable to read commonDefinitions.js file");
        }
      }
    }

    if (tableId != null) {
      File tableFile = new File(ODKFileUtils.getTableSpecificDefinitionsFile(appName, tableId));
      if (!tableFile.exists()) {
        tableSpecificDefinitionsMap.remove(tableId);
      } else {
        // assume it is current if it exists
        if (tableSpecificDefinitionsMap.containsKey(tableId)) {
          return;
        }
        InputStream stream = null;
        BufferedReader reader = null;
        try {
          stream = new FileInputStream(tableFile);
          reader = new BufferedReader(new InputStreamReader(stream, CharsetConsts.UTF_8));
          reader.mark(1);
          int ch = reader.read();
          while (ch != -1 && ch != '{') {
            reader.mark(1);
            ch = reader.read();
          }
          reader.reset();
          Map<String, Object> tableSpecificTranslations = ODKFileUtils.mapper
              .readValue(reader, ref);
          if (tableSpecificTranslations != null) {
            tableSpecificDefinitionsMap.put(tableId, tableSpecificTranslations);
          }
        } finally {
          if (reader != null) {
            reader.close();
          } else if (stream != null) {
            stream.close();
          }
        }
      }
    }
  }

  /**
   * Used in commonTranslationLocaleScreen
   *
   * @param appName the app name
   * @return a list of locales provided by that app
   * @throws IOException if the file couldn't be opened
   */
  @SuppressWarnings("unused")
  public static List<Map<String, Object>> getCommonLocales(String appName) throws IOException {
    if (commonDefinitions == null) {
      loadTranslations(appName, null);
    }

    if (commonDefinitions != null && commonDefinitions.containsKey("_locales")) {
      Map<String, Object> localesObject = (Map<String, Object>) commonDefinitions.get("_locales");
      if (localesObject != null && localesObject.containsKey("value")) {
        return (List<Map<String, Object>>) localesObject.get("value");
      }
    }
    return null;
  }

  /**
   * Used in commonTranslationLocaleScreen
   *
   * @param appName the app name
   * @return a list of locales provided by that app
   * @throws IOException if the file couldn't be opened
   */
  @SuppressWarnings("unused")
  public static String getCommonLocaleDefault(String appName) throws IOException {
    if (commonDefinitions == null) {
      loadTranslations(appName, null);
    }

    if (commonDefinitions != null && commonDefinitions.containsKey("_default_locale")) {
      Map<String, Object> default_locale = (Map<String, Object>) commonDefinitions
          .get("_default_locale");
      String value = (String) default_locale.get("value");
      if (value != null && !value.isEmpty()) {
        return value;
      }
    }
    return "default";
  }

  private static synchronized Map<String, Object> resolveTranslation(String appName, String tableId,
      String translationToken) throws IOException {
    if (appName == null) {
      throw new IllegalArgumentException("appName cannot be null");
    }
    if (tableId == null) {
      throw new IllegalArgumentException("tableId cannot be null");
    }
    if (translationToken == null) {
      throw new IllegalArgumentException("translationToken cannot be null");
    }
    if (savedAppName == null || !savedAppName.equals(appName)) {
      clearTranslations();
    }
    if (commonDefinitions == null || !tableSpecificDefinitionsMap.containsKey(tableId)) {
      loadTranslations(appName, tableId);
    }

    Map<String, Object> value = null;
    Map<String, Object> tableSpecificDefinitions = tableSpecificDefinitionsMap.get(tableId);
    if (tableSpecificDefinitions != null) {
      Map<String, Object> tokens = (Map<String, Object>) tableSpecificDefinitions.get("_tokens");
      value = (Map<String, Object>) tokens.get(translationToken);
    }
    if (commonDefinitions != null && value == null) {
      Map<String, Object> tokens = (Map<String, Object>) commonDefinitions.get("_tokens");
      value = (Map<String, Object>) tokens.get(translationToken);
    }
    return value;
  }

  /**
   * Retrieve a translation from the localizationMap. The map might be for text, image,
   * audio, etc. entries.
   * <p>
   * If localizationMap is a string, return it as-is, otherwise, it should be a
   * Map<String,Object> with locale as the key and value as the internationalized string.
   * <p>
   * full_locale is assumed to be of the form: language + "_" + country, with language not
   * containing an underscore. It first tries exact-case match of full_locale then tries for
   * case-insensitive matching of full_locale and then of just language. And if none of these
   * are present, returns the default translation or null if none is available.
   *
   * @param localizationMap a map of locale IDs to translations
   * @param full_locale     the locale to get the translation for
   * @return null or the translation string.
   */
  private static String processLocalizationMap(Object localizationMap, String full_locale) {
    if (localizationMap == null) {
      throw new IllegalStateException("null localizationMap");
    }

    if (localizationMap instanceof String) {
      return (String) localizationMap;
    }

    Map<String, Object> aMap = (Map<String, Object>) localizationMap;

    int underscore = full_locale.indexOf('_');
    String lang_only_locale = underscore <= 0 ? null : full_locale.substring(0, underscore);

    String langOnlyMatch = null;
    String defaultMatch = null;

    if (aMap.containsKey(full_locale)) {
      return (String) aMap.get(full_locale);
    }

    // otherwise, do a case-independent compare to find a match
    // and also consider language-only comparisons and, finally
    // retrieve the default translation.
    for (Map.Entry<String, Object> entry : aMap.entrySet()) {
      String key = entry.getKey();
      if (key.compareToIgnoreCase(full_locale) == 0) {
        return (String) entry.getValue();
      }
      if (lang_only_locale != null && key.compareToIgnoreCase(lang_only_locale) == 0) {
        langOnlyMatch = (String) entry.getValue();
      }

      if (key.compareToIgnoreCase("default") == 0) {
        defaultMatch = (String) entry.getValue();
      }
    }

    if (langOnlyMatch != null) {
      return langOnlyMatch;
    }

    return defaultMatch;
  }

  /**
   * displayName is a JSON serialization of either an i18n token or of a Map&lt;String,Object&gt;.
   * <p>
   * full_locale is assumed to be of the form: language + "_" + country, with language not
   * containing an underscore. It first tries exact-case match of full_locale then tries for
   * case-insensitive matching of full_locale and then of just language. And if none of these
   * are present, returns the default translation or null if none is available.
   *
   * Used all over the place
   *
   * @param appName the app name
   * @param tableId the table id to get the display name for
   * @param full_locale the locale to use to decode the display name
   * @param displayName the raw display name json from the properties
   * @return a display name in the requested locale, if available
   */
  @SuppressWarnings("WeakerAccess")
  public static String getLocalizedDisplayName(String appName, String tableId, String full_locale,
      String displayName) {

    // retrieve the localeMap from the JSON string stored in this field.
    // this JSON serialization will either be a string that is a translationToken
    // that points to a translation in the common or table-specific translations
    // or it will be a localization object with { "text":..., "image":..., ... } fields.
    Map<String, Object> localizationMap = null;
    if (displayName.startsWith("\"") && displayName.endsWith("\"")) {
      String translationToken;
      try {
        translationToken = ODKFileUtils.mapper.readValue(displayName, String.class);
      } catch (IOException e) {
        WebLogger.getLogger(appName).printStackTrace(e);
        throw new IllegalStateException("bad displayName: " + displayName);
      }
      try {
        localizationMap = resolveTranslation(appName, tableId, translationToken);
      } catch (IOException e) {
        WebLogger.getLogger(appName).printStackTrace(e);
        throw new IllegalStateException(
            "unable to retrieve display localization from string " + "token: " + translationToken);
      }
      if (localizationMap == null) {
        //throw new IllegalStateException(
            //"no translations found for translation token: " + translationToken);
        WebLogger.getLogger(appName).e(TAG, "No translation tokens found!");
        return "Error translating \"" + translationToken + "\"";
      }
    } else {
      TypeReference<Map<String, Object>> ref = new TypeReference<Map<String, Object>>() {
      };
      try {
        localizationMap = ODKFileUtils.mapper.readValue(displayName, ref);
      } catch (IOException e) {
        WebLogger.getLogger(appName).printStackTrace(e);
        throw new IllegalStateException("bad displayName: " + displayName);
      }
    }

    if (localizationMap == null) {
      throw new IllegalStateException(
          "bad displayName (no localization map found): " + displayName);
    }

    // the localization has "text", "image", etc. keys.
    // pull out the text entry.
    Object textEntry = localizationMap.get("text");
    if (textEntry == null) {
      throw new IllegalStateException("no text entry for displayname: " + displayName);
    }

    return processLocalizationMap(textEntry, full_locale);
  }

  /**
   * candidateLocalizationMap may either be an i18n token or a Map&lt;String,Object&gt;.
   * If the former, then it is resolved into a map via the common or table-specific
   * translations.
   * <p>
   * The internationalization for the full_locale is then retrieved from this map.
   * <p>
   * full_locale is assumed to be of the form: language + "_" + country, with language not
   * containing an underscore. It first tries exact-case match of full_locale then tries for
   * case-insensitive matching of full_locale and then of just language. And if none of these
   * are present, returns the default translation or null if none is available.
   * <p>
   * Used in CommonTranslationsLocaleScreen
   *
   * @param appName the app name
   * @param tableId the table id, used to get a localization map using an i18n token
   * @param full_locale of the form language + "_" + country
   * @param candidateLocalizationMap a map from locales to localized strings or an i18n token
   * @return the localized string
   */
  @SuppressWarnings("unused")
  public static String getLocalizationFromMap(String appName, String tableId, String full_locale,
      Object candidateLocalizationMap) {

    //noinspection UnusedAssignment
    Map<String, Object> localizationMap = null;

    if (candidateLocalizationMap == null) {
      throw new IllegalStateException("null passed as localizationMap or i18nToken");
    }

    if (candidateLocalizationMap instanceof String) {
      // actually an internationalization token.
      // Retrieve the entry in the localeMap from this token.
      // this will be a localization object with { "text":..., "image":..., ... } fields.
      String translationToken = (String) candidateLocalizationMap;
      try {
        localizationMap = resolveTranslation(appName, tableId, translationToken);
      } catch (IOException e) {
        WebLogger.getLogger(appName).printStackTrace(e);
        throw new IllegalStateException(
            "unable to retrieve display localization from string " + "token: " + translationToken);
      }
      if (localizationMap == null) {
        throw new IllegalStateException(
            "no translations found for translation token: " + translationToken);
      }
    } else {
      // otherwise, it should be localization object with { "text":..., "image":..., ... } fields.
      localizationMap = (Map<String, Object>) candidateLocalizationMap;
    }

    // the localization has "text", "image", etc. keys.
    // pull out the text entry.
    Object textEntry = localizationMap.get("text");
    if (textEntry == null) {
      throw new IllegalStateException(
          "no text entry in localization map: " + candidateLocalizationMap);
    }
    return processLocalizationMap(textEntry, full_locale);
  }
}
