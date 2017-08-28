package org.opendatakit.consts;

/**
 * Created by jbeorse on 7/17/17.
 */

public class RequestCodeConsts {
    /**
     * Request codes, used with startActivityForResult
     */
    public static final class RequestCodes {
        /**
         * Used to view a collection or a join table in a SpreadsheetFragment in
         * TableDisplayActivity
         */
        public static final int DISPLAY_VIEW = 1;
        /**
         * Used when launching the file picker to change the detail view file
         */
        public static final int CHOOSE_DETAIL_FILE = 2;
        /**
         * Used when launching the file picker to change the list view file
         */
        public static final int CHOOSE_LIST_FILE = 3;
        /**
         * Used when launching the file picker to change the map view file
         */
        public static final int CHOOSE_MAP_FILE = 4;
        /**
         * A generic code for now. Can refactor to make more specific if needed.
         */
        public static final int LAUNCH_VIEW = 5;
        /**
         * Used to launch the device preferences screen (the one with the server settings, device
         * settings, tables specific settings, user restrictions, etc... options)
         */
        public static final int LAUNCH_DISPLAY_PREFS = 6;
        /**
         * For launching an intent to import csv files into a table
         */
        public static final int LAUNCH_IMPORT = 7;
        /**
         * For launching an intent to sync
         */
        public static final int LAUNCH_SYNC = 8;
        /**
         * For launching an HTML file not associated with a table. Unused
         */
        public static final int LAUNCH_WEB_VIEW = 10;
        /**
         * For launching an intent to edit a table's properties.
         */
        public static final int LAUNCH_TABLE_PREFS = 11;
        /**
         * For launching an intent to list a column's color rules
         */
        public static final int LAUNCH_COLOR_RULE_LIST = 16;
        /**
         * For launching an intent to add a row in survey
         */
        public static final int ADD_ROW_SURVEY = 17;
        /**
         * For launching an intent to edit a row in survey
         */
        public static final int EDIT_ROW_SURVEY = 18;
        /**
         * For launching an intent to resolve checkpoints
         */
        public static final int LAUNCH_CHECKPOINT_RESOLVER = 19;
        /**
         * For launching an intent to resolve conflicts
         */
        public static final int LAUNCH_CONFLICT_RESOLVER = 20;
        /**
         * For launching an intent to export a table to a csv
         */
        public static final int LAUNCH_EXPORT = 21;
        /**
         * Used when the javascript has started a DoAction and needs to be notified of its result
         */
        public static final int LAUNCH_DOACTION = 22;
        /**
         * Used to launch an intent to open a column preference fragment via table level preference
         * activity
         */
        public static final int LAUNCH_COLUMN_PREFS = 23;
        /**
         * Used to launch the main activity to let the user grant Services permissions
         */
        public static final int LAUNCH_MAIN_ACTIVITY = 24;

        /**
         * Do not instantiate this class
         */
        private RequestCodes() {
        }
    }

}
