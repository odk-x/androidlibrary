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

import org.opendatakit.consts.SqliteConsts;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.DataTableColumns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
                .asList(DataTableColumns.ROW_ETAG, DataTableColumns.SYNC_STATE, DataTableColumns.CONFLICT_TYPE, DataTableColumns.SAVEPOINT_TIMESTAMP,
                        DataTableColumns.SAVEPOINT_CREATOR, DataTableColumns.SAVEPOINT_TYPE, DataTableColumns.DEFAULT_ACCESS, DataTableColumns.ROW_OWNER, DataTableColumns.GROUP_READ_ONLY,
                        DataTableColumns.GROUP_MODIFY, DataTableColumns.GROUP_PRIVILEGED, DataTableColumns.FORM_ID, DataTableColumns.LOCALE));
        /**
         * SQLite keywords ( http://www.sqlite.org/lang_keywords.html )
         */
        reservedNames.addAll(Arrays
                .asList(SqliteConsts.ABORT, SqliteConsts.ACTION, SqliteConsts.ADD, SqliteConsts.AFTER, SqliteConsts.ALL, SqliteConsts.ALTER, SqliteConsts.ANALYZE, SqliteConsts.AND, SqliteConsts.AS, SqliteConsts.ASC,
                        SqliteConsts.ATTACH, SqliteConsts.AUTOINCREMENT, SqliteConsts.BEFORE, SqliteConsts.BEGIN, SqliteConsts.BETWEEN, SqliteConsts.BY, SqliteConsts.CASCADE, SqliteConsts.CASE,
                        SqliteConsts.CAST, SqliteConsts.CHECK, SqliteConsts.COLLATE, SqliteConsts.COLUMN, SqliteConsts.COMMIT, SqliteConsts.CONFLICT, SqliteConsts.CONSTRAINT, SqliteConsts.CREATE,
                        SqliteConsts.CROSS, SqliteConsts.CURRENT_DATE, SqliteConsts.CURRENT_TIME, SqliteConsts.CURRENT_TIMESTAMP, SqliteConsts.DATABASE, SqliteConsts.DEFAULT,
                        SqliteConsts.DEFERRABLE, SqliteConsts.DEFERRED, SqliteConsts.DELETE, SqliteConsts.DESC, SqliteConsts.DETACH, SqliteConsts.DISTINCT, SqliteConsts.DROP, SqliteConsts.EACH,
                        SqliteConsts.ELSE, SqliteConsts.END, SqliteConsts.ESCAPE, SqliteConsts.EXCEPT, SqliteConsts.EXCLUSIVE, SqliteConsts.EXISTS, SqliteConsts.EXPLAIN, SqliteConsts.FAIL, SqliteConsts.FOR,
                        SqliteConsts.FOREIGN, SqliteConsts.FROM, SqliteConsts.FULL, SqliteConsts.GLOB, SqliteConsts.GROUP, SqliteConsts.HAVING, SqliteConsts.IF, SqliteConsts.IGNORE, SqliteConsts.IMMEDIATE, SqliteConsts.IN,
                        SqliteConsts.INDEX, SqliteConsts.INDEXED, SqliteConsts.INITIALLY, SqliteConsts.INNER, SqliteConsts.INSERT, SqliteConsts.INSTEAD, SqliteConsts.INTERSECT, SqliteConsts.INTO,
                        SqliteConsts.IS, SqliteConsts.ISNULL, SqliteConsts.JOIN, SqliteConsts.KEY, SqliteConsts.LEFT, SqliteConsts.LIKE, SqliteConsts.LIMIT, SqliteConsts.MATCH, SqliteConsts.NATURAL, SqliteConsts.NO, SqliteConsts.NOT,
                        SqliteConsts.NOTNULL, SqliteConsts.NULL, SqliteConsts.OF, SqliteConsts.OFFSET, SqliteConsts.ON, SqliteConsts.OR, SqliteConsts.ORDER, SqliteConsts.OUTER, SqliteConsts.PLAN, SqliteConsts.PRAGMA,
                        SqliteConsts.PRIMARY, SqliteConsts.QUERY, SqliteConsts.RAISE, SqliteConsts.REFERENCES, SqliteConsts.REGEXP, SqliteConsts.REINDEX, SqliteConsts.RELEASE, SqliteConsts.RENAME,
                        SqliteConsts.REPLACE, SqliteConsts.RESTRICT, SqliteConsts.RIGHT, SqliteConsts.ROLLBACK, SqliteConsts.ROW, SqliteConsts.SAVEPOINT, SqliteConsts.SELECT, SqliteConsts.SET,
                        SqliteConsts.TABLE, SqliteConsts.TEMP, SqliteConsts.TEMPORARY, SqliteConsts.THEN, SqliteConsts.TO, SqliteConsts.TRANSACTION, SqliteConsts.TRIGGER, SqliteConsts.UNION, SqliteConsts.UNIQUE,
                        SqliteConsts.UPDATE, SqliteConsts.USING, SqliteConsts.VACUUM, SqliteConsts.VALUES, SqliteConsts.VIEW, SqliteConsts.VIRTUAL, SqliteConsts.WHEN, SqliteConsts.WHERE));

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
