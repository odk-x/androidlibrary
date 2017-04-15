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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharSet;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class LocalizationUtils {

  public static String genUUID() {
    return "uuid:" + UUID.randomUUID().toString();
  }

  private static String savedAppName;
  private static Map<String, Object> commonTranslations;
  private static Map<String, Map<String, Object>> tableSpecificTranslationMap =
      new HashMap<String, Map<String, Object>>();

  private synchronized static void clearTranslations() {
    commonTranslations = null;
    tableSpecificTranslationMap.clear();
  }

  private synchronized static void loadTranslations(String appName, String tableId) throws
      IOException {
    if ( savedAppName != null && !savedAppName.equals(appName) ) {
      clearTranslations();
    }
    savedAppName = appName;
    TypeReference<Map<String,Object>> ref = new TypeReference<Map<String,Object>>() {};
    if ( commonTranslations == null ) {
      File commonFile = new File(ODKFileUtils.getCommonTranslationsFile(appName));
      if ( commonFile.exists() ) {
        InputStream stream = null;
        BufferedReader reader = null;
        try {
          stream = new FileInputStream(commonFile);
          reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
          reader.mark(1);
          int ch = reader.read();
          while ( ch != -1 && ch != '{') {
            reader.mark(1);
            ch = reader.read();
          }
          reader.reset();
          commonTranslations = ODKFileUtils.mapper.readValue(reader, ref);
        } finally {
          if ( reader != null ) {
            reader.close();
          } else if ( stream != null ) {
            stream.close();
          }
        }
      }
    }

    if ( tableId != null) {
      File tableFile = new File(ODKFileUtils.getTableSpecificTranslationsFile(appName, tableId));
      if (!tableFile.exists()) {
        tableSpecificTranslationMap.remove(tableId);
      } else {
        // assume it is current if it exists
        if (tableSpecificTranslationMap.containsKey(tableId)) {
          return;
        }
        InputStream stream = null;
        BufferedReader reader = null;
        try {
          stream = new FileInputStream(tableFile);
          reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
          reader.mark(1);
          int ch = reader.read();
          while (ch != -1 && ch != '{') {
            reader.mark(1);
            ch = reader.read();
          }
          reader.reset();
          Map<String, Object> tableSpecificTranslations = ODKFileUtils.mapper.readValue(reader, ref);
          if (tableSpecificTranslations != null) {
            tableSpecificTranslationMap.put(tableId, tableSpecificTranslations);
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

  private synchronized static Map<String, Object>  resolveTranslation(String appName,
          String tableId, String translationToken) throws IOException  {
    if ( appName == null ) {
      throw new IllegalArgumentException("appName cannot be null");
    }
    if ( tableId == null ) {
      throw new IllegalArgumentException("tableId cannot be null");
    }
    if ( translationToken == null ) {
      throw new IllegalArgumentException("translationToken cannot be null");
    }
    if ( savedAppName == null || !savedAppName.equals(appName) ) {
      clearTranslations();
    }
    if ( commonTranslations == null ||
        (tableId != null && !tableSpecificTranslationMap.containsKey(tableId)) ) {
      loadTranslations(appName, tableId);
    }

    Map<String, Object>  value = null;
    if ( tableId != null) {
      Map<String, Object> tableSpecificTranslations = tableSpecificTranslationMap.get(tableId);
      if (tableSpecificTranslations != null) {
        Map<String, Object> tokens = (Map<String, Object>) tableSpecificTranslations.get("_tokens");
        value = (Map<String, Object>) tokens.get(translationToken);
      }
    }
    if ( commonTranslations != null && value == null ) {
      Map<String, Object> tokens = (Map<String, Object>) commonTranslations.get("_tokens");
      value = (Map<String, Object>) tokens.get(translationToken);
    }
    return value;
  }

  public static String getLocalizedDisplayName(String appName, String tableId, String displayName) {

    // retrieve the localeMap from the JSON string stored in this field.
    // this JSON serialization will either be a string that is a translationToken
    // that points to a translation in the common or table-specific translations
    // or it will be a localization object with { "text":..., "image":..., ... } fields.
    //
    Map<String, Object> localizationMap = null;
    if (displayName.startsWith("\"") && displayName.endsWith("\"")) {
      String translationToken;
      try {
        translationToken = ODKFileUtils.mapper.readValue(displayName, String.class);
      } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalStateException("bad displayName: " + displayName);
      }
      try {
        localizationMap = resolveTranslation(appName, tableId, translationToken);
      } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalStateException(
            "unable to retrieve display localization from string " + "token: " + translationToken);
      }
      if ( localizationMap == null ) {
        throw new IllegalStateException(
            "no translations found for translation token: " +
                translationToken);
      }
    } else {
      TypeReference<Map<String, Object>> ref = new TypeReference<Map<String, Object>>() {
      };
      try {
        localizationMap = ODKFileUtils.mapper.readValue(displayName, ref);
      } catch (JsonParseException e) {
        e.printStackTrace();
        throw new IllegalStateException("bad displayName: " + displayName);
      } catch (JsonMappingException e) {
        e.printStackTrace();
        throw new IllegalStateException("bad displayName: " + displayName);
      } catch (IOException e) {
        e.printStackTrace();
        throw new IllegalStateException("bad displayName: " + displayName);
      }
    }

    if ( localizationMap == null ) {
      throw new IllegalStateException(
          "bad displayName (no localization map found): " + displayName);
    }

    // the localization has "text", "image", etc. keys.
    // pull out the text entry.
    Object textEntry = localizationMap.get("text");
    if ( textEntry == null ) {
      throw new IllegalStateException(
          "bad displayName (no text entry in localization map): " + displayName);
    }

    if ( textEntry instanceof String ) {
      return (String) textEntry;
    }

    Map<String, Object> textMap = (Map<String, Object>) textEntry;

    Locale locale = Locale.getDefault();
    String full_locale = locale.toString();
    int underscore = full_locale.indexOf('_');
    String lang_only_locale = (underscore == -1)
        ? full_locale : full_locale.substring(0, underscore);

    String candidate = (String) textMap.get(full_locale);
    if (candidate != null) {
      return candidate;
    }
    candidate = (String) textMap.get(lang_only_locale);
    if (candidate != null) {
      return candidate;
    }
    candidate = (String) textMap.get("default");
    if (candidate != null) {
      return candidate;
    }
    return null;
  }

}
