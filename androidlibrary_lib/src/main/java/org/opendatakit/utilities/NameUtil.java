/*
 * Copyright (C) 2013 University of Washington
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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opendatakit.logging.WebLogger;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Methods for dealing with naming conventions.
 *
 * @author sudar.sam@gmail.com
 */
public final class NameUtil {

  /**
   * Because Android content provider internals do not quote the
   * column field names when constructing SQLite queries, we need
   * to either prevent the user from using SQLite keywords or code
   * up our own mapping code for Android. Rather than bloat our
   * code, we restrict the set of keywords the user can use.
   * <p>
   * To this list, we add our metadata element names. This further
   * simplifies references to these fields, as we can just consider
   * them to be hidden during display and non-modifiable by the
   * UI, but accessible by the end user (though perhaps not mutable).
   * <p>
   * Fortunately, the server code properly quotes all column and
   * table names, so we only have to track the SQLite reserved names
   * and not all MySQL or PostgreSQL reserved names.
   */
  private static final ArrayList<String> reservedNamesSortedList;

  private static final Pattern letterFirstPattern;

  static {
    /**
     * This pattern does not support (?U) (UNICODE_CHARACTER_CLASS)
     */
    letterFirstPattern = Pattern
        .compile("^\\p{L}\\p{M}*(\\p{L}\\p{M}*|\\p{Nd}|_)*$", Pattern.UNICODE_CASE);

    ArrayList<String> reservedNames = new ArrayList<>();

    /**
     * ODK Metadata reserved names
     */
    reservedNames.addAll(Arrays
        .asList("ROW_ETAG", "SYNC_STATE", "CONFLICT_TYPE", "SAVEPOINT_TIMESTAMP",
            "SAVEPOINT_CREATOR", "SAVEPOINT_TYPE", "DEFAULT_ACCESS", "ROW_OWNER", "GROUP_READ_ONLY",
            "GROUP_MODIFY", "GROUP_PRIVILEGED", "FORM_ID", "LOCALE"));
    /**
     * SQLite keywords ( http://www.sqlite.org/lang_keywords.html )
     */
    reservedNames.addAll(Arrays
        .asList("ABORT", "ACTION", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC",
            "ATTACH", "AUTOINCREMENT", "BEFORE", "BEGIN", "BETWEEN", "BY", "CASCADE", "CASE",
            "CAST", "CHECK", "COLLATE", "COLUMN", "COMMIT", "CONFLICT", "CONSTRAINT", "CREATE",
            "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DATABASE", "DEFAULT",
            "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DETACH", "DISTINCT", "DROP", "EACH",
            "ELSE", "END", "ESCAPE", "EXCEPT", "EXCLUSIVE", "EXISTS", "EXPLAIN", "FAIL", "FOR",
            "FOREIGN", "FROM", "FULL", "GLOB", "GROUP", "HAVING", "IF", "IGNORE", "IMMEDIATE", "IN",
            "INDEX", "INDEXED", "INITIALLY", "INNER", "INSERT", "INSTEAD", "INTERSECT", "INTO",
            "IS", "ISNULL", "JOIN", "KEY", "LEFT", "LIKE", "LIMIT", "MATCH", "NATURAL", "NO", "NOT",
            "NOTNULL", "NULL", "OF", "OFFSET", "ON", "OR", "ORDER", "OUTER", "PLAN", "PRAGMA",
            "PRIMARY", "QUERY", "RAISE", "REFERENCES", "REGEXP", "REINDEX", "RELEASE", "RENAME",
            "REPLACE", "RESTRICT", "RIGHT", "ROLLBACK", "ROW", "SAVEPOINT", "SELECT", "SET",
            "TABLE", "TEMP", "TEMPORARY", "THEN", "TO", "TRANSACTION", "TRIGGER", "UNION", "UNIQUE",
            "UPDATE", "USING", "VACUUM", "VALUES", "VIEW", "VIRTUAL", "WHEN", "WHERE"));

    Collections.sort(reservedNames);

    reservedNamesSortedList = reservedNames;
  }

  /**
   * Do not instantiate this class
   */
  private NameUtil() {
  }

  /**
   * Determines whether or not the given name is valid for a user-defined
   * entity in the database. Valid names are determined to not begin with a
   * single underscore, not to begin with a digit, and to contain only unicode
   * appropriate word characters.
   *
   * @param name The string to be looked up in the list of definitely not allowed words
   * @return true if valid else false
   */
  public static boolean isValidUserDefinedDatabaseName(String name) {
    boolean matchHit = letterFirstPattern.matcher(name).matches();
    // TODO: uppercase is bad...
    boolean reserveHit =
        Collections.binarySearch(reservedNamesSortedList, name.toUpperCase(Locale.US)) >= 0;
    return !reserveHit && matchHit;
  }

  /**
   * Used in ColumnUtil, TableUtil, FormsProvider, OdkResolveConflictRowLoader,
   * ODkResolveConflictFieldLoader, OdkResolveCheckpointRowLoader,
   * OdkResolveCheckpointFieldLoader and SyncExecutionContext
   *
   * @param name a name that might have underscores
   * @return A suitable display name given the passed string
   */
  @SuppressWarnings("WeakerAccess")
  public static String constructSimpleDisplayName(String name) {
    String displayName = name.replaceAll("_", " ");
    if (displayName.startsWith(" ")) {
      displayName = "_" + displayName;
    }
    if (displayName.endsWith(" ")) {
      displayName += "_";
    }
    Map<String, Object> displayEntry = new HashMap<>();
    displayEntry.put("text", displayName);
    try {
      return ODKFileUtils.mapper.writeValueAsString(displayEntry);
    } catch (JsonProcessingException e) {
      WebLogger.getContextLogger().printStackTrace(e);
      throw new IllegalStateException("constructSimpleDisplayName: " + displayName);
    }
  }

  /**
   * Used in MediaCaptureVideoActivity, MediaCaptureImageActivity, MediaDeleteAudioActivity,
   * MediaChooseAudioActivity, MediaCaptureAudioActivity, MediaChooseImageActivity,
   * DeviceSettingsFragment, MediaDeleteImageActivity, MediaChooseVideoActivity,
   * MediaDeleteVideoActivity
   *
   * @param displayName a display name to normalize
   * @return a normalized version of that display name
   */
  @SuppressWarnings("WeakerAccess")
  public static String normalizeDisplayName(String displayName) {
    // TODO this seems backwards
    //noinspection UnnecessaryParentheses
    if ((displayName.startsWith("\"") && displayName.endsWith("\"")) || (displayName.startsWith("{")
        && displayName.endsWith("}"))) {
      return displayName;
    } else {
      try {
        return ODKFileUtils.mapper.writeValueAsString(displayName);
      } catch (JsonProcessingException e) {
        WebLogger.getContextLogger().printStackTrace(e);
        throw new IllegalArgumentException(
            "normalizeDisplayName: Invalid displayName " + displayName);
      }
    }
  }
}
