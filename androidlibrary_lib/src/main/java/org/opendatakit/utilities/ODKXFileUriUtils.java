package org.opendatakit.utilities;

import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import java.io.File;
import java.util.List;

public final class ODKXFileUriUtils {

    // Used for logging
    private static final String TAG = ODKXFileUriUtils.class.getSimpleName();

    // Default app name when unspecified
    private static final String ODKX_DEFAULT_APP_NAME ="default";
    // base path
    private static final String ODKX_FOLDER_NAME = "opendatakit";


    private static final String CONFIG_FOLDER_NAME = "config";
    private static final String DATA_FOLDER_NAME = "data";
    private static final String OUTPUT_FOLDER_NAME = "output";
    private static final String SYSTEM_FOLDER_NAME = "system";
    private static final String PERMANENT_FOLDER_NAME = "permanent";

    // under output and config/assets
    private static final String ASSETS_FOLDER_NAME = "assets";

    // under output
    private static final String TABLES_FOLDER_NAME = "tables";
    private static final String WEB_DB_FOLDER_NAME = "webDb";

    // under system
    private static final String GEO_CACHE_FOLDER_NAME = "geoCache";
    private static final String APP_CACHE_FOLDER_NAME = "appCache";

    // under the config/tables directory...
    private static final String CSV_FOLDER_NAME = "csv";
    private static final String LOGGING_FOLDER_NAME = "logging";
    /**
     * The name of the folder where the debug objects are written.
     */
    private static final String DEBUG_FOLDER_NAME = "debug";

    private static final String ANDROID_EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents";
//    public static final String AUTHORITY = DocumentsContract.EXTERNAL_STORAGE_PROVIDER_AUTHORITY;/
    private static final String CONTENT_SCHEME = "content";
    private static final String PRIMARY_PATH= "/document/primary:opendatakit";

    public static String ODKXRemainingPath(String appName, Uri uri) {
        String pathBeginning = "/" + ODKX_FOLDER_NAME + "/" + appName;
        String uriPath = uri.getPath();
        if (uriPath.startsWith(pathBeginning)) {
            return uriPath.substring(pathBeginning.length() + 1);
        }
        return null;
    }

    /**
     * Used all over the place as root uri
     *
     * @return uriOfODKXFolder
     */
    @SuppressWarnings("WeakerAccess")
    public static Uri getOdkXUri() {
        Uri.Builder base = new Uri.Builder();
        base.scheme(CONTENT_SCHEME);
        return base.appendPath(ODKX_FOLDER_NAME).build();
    }


    /**
     * Gets the uri that contains all the configuration for a particular app
     * @param appName the app name
     * @return opendatakit/:app_name
     */
    public static Uri getAppUri(String appName) {
        Uri.Builder base = getOdkXUri().buildUpon();
        return base.appendPath(appName).build();
    }

    /**
     * Gets the uri that contains all the configuration for a particular application
     *
     * @param appName the app name
     * @return :app_name/config
     */
    @SuppressWarnings("WeakerAccess")
    public static Uri getConfigUri(String appName) {
        Uri.Builder base = getAppUri(appName).buildUpon();
        return base.appendPath(CONFIG_FOLDER_NAME).build();
    }

    /**
     * Gets the uri with the static app configuration data
     * @param appName the app name
     * @return :app_name/config/assets
     */
    public static Uri getAssetsUri(String appName) {
        Uri.Builder base = getConfigUri(appName).buildUpon();
        return base.appendPath(ASSETS_FOLDER_NAME).build();
    }

    /**
     * Gets the Uri with the csv files to import on first run
     * @param appName the app name
     * @return :app_name/config/assets/csv
     */
    public static Uri getAssetsCsvUri(String appName) {
        Uri.Builder base = getAssetsUri(appName).buildUpon();
        return base.appendPath(CSV_FOLDER_NAME).build();
    }

}
