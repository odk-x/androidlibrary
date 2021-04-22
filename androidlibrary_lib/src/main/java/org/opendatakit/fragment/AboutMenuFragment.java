/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.fragment;

import androidx.fragment.app.Fragment;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.application.IToolAware;
import org.opendatakit.listener.LicenseReaderListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.WebLoggerIf;
import org.opendatakit.task.LicenseReaderTask;

/**
 * Used in ConflictResolutionActivity, CheckpointResolutionActivity,
 * AllConflictsResolutionActivity, MainMenuActivity, SyncBaseActivity, MainActivity, SyncActivity
 */
@SuppressWarnings("unused")
public class AboutMenuFragment extends Fragment implements LicenseReaderListener {
  private static final String TAG = AboutMenuFragment.class.getSimpleName();

  public static final String NAME = "About";
  public static final int ID = R.layout.about_menu_layout;
  /**
   * Key for savedInstanceState
   */
  private static final String LICENSE_TEXT = "LICENSE_TEXT";

  /**
   * The license reader task. Once this completes, we never re-run it.
   */
  private static LicenseReaderTask licenseReaderTask = null;

  private TextView mTextView;
  
  /**
   * retained value...
   */
  private String mLicenseText = null;

  @SuppressWarnings("deprecation")
  private Spanned licenseTextFormattedAsHtml() {
    Spanned html;
    if (Build.VERSION.SDK_INT >= 24) {
      html = Html.fromHtml(mLicenseText, Html.FROM_HTML_MODE_LEGACY);
    } else {
      //noinspection deprecation
      html = Html.fromHtml(mLicenseText);
    }
    return html;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View aboutMenuView = inflater.inflate(ID, container, false);

    TextView versionBox = aboutMenuView.findViewById(R.id.versionText);
    versionBox.setText(((IToolAware) getActivity().getApplication()).getVersionedToolName());

    {
      IAppAwareActivity appAwareActivity = (IAppAwareActivity) getActivity();
      int logLevel = WebLogger.getLogger(appAwareActivity.getAppName()).getMinimumSystemLogLevel();
      String suppressLevel;
      switch (logLevel) {
      case WebLoggerIf.ASSERT:
      case WebLoggerIf.TIP:
        suppressLevel = getString(R.string.log_threshold_assert);
        break;
      case WebLoggerIf.ERROR:
      case WebLoggerIf.SUCCESS:
        suppressLevel = getString(R.string.log_threshold_error);
        break;
      case WebLoggerIf.WARN:
        suppressLevel = getString(R.string.log_threshold_warn);
        break;
      case WebLoggerIf.INFO:
        suppressLevel = getString(R.string.log_threshold_info);
        break;
      case WebLoggerIf.DEBUG:
        suppressLevel = getString(R.string.log_threshold_debug);
        break;
      case WebLoggerIf.VERBOSE:
        suppressLevel = getString(R.string.log_threshold_verbose);
        break;
      default:
        throw new IllegalStateException("Unexpected log level filter value");
      }
      TextView logLevelBox = aboutMenuView.findViewById(R.id.logLevelText);
      logLevelBox.setText(suppressLevel);
    }

    mTextView = aboutMenuView.findViewById(R.id.text1);
    mTextView.setAutoLinkMask(Linkify.WEB_URLS);
    mTextView.setClickable(true);

    if (savedInstanceState != null && savedInstanceState.containsKey(LICENSE_TEXT)) {
      mLicenseText = savedInstanceState.getString(LICENSE_TEXT);
      Spanned html = licenseTextFormattedAsHtml();
      mTextView.setText(html);
    } else {
      readLicenseFile();
    }

    return aboutMenuView;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(LICENSE_TEXT, mLicenseText);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void readLicenseComplete(String result) {
    IAppAwareActivity activity = (IAppAwareActivity) getActivity();
    WebLogger.getLogger(activity.getAppName()).i(TAG, "Read license complete");
    if (result != null) {
      // Read license file successfully
      mLicenseText = result;
      Spanned html = licenseTextFormattedAsHtml();
      mTextView.setText(html);
    } else {
      // had some failures
      WebLogger.getLogger(activity.getAppName()).e(TAG, "Failed to read license file");
      Toast.makeText(getActivity(), R.string.read_license_fail, Toast.LENGTH_LONG).show();
    }
  }

  private synchronized void readLicenseFile() {
    IAppAwareActivity activity = (IAppAwareActivity) getActivity();
    String appName = activity.getAppName();

    if ( licenseReaderTask == null ) {
      LicenseReaderTask lrt = new LicenseReaderTask();
      lrt.setApplication(getActivity().getApplication());
      lrt.setAppName(appName);
      lrt.setLicenseReaderListener(this);
      licenseReaderTask = lrt;
      licenseReaderTask.execute();
    } else {
      // update listener
      licenseReaderTask.setLicenseReaderListener(this);
      if (licenseReaderTask.getStatus() != AsyncTask.Status.FINISHED) {
        Toast.makeText(getActivity(), getString(R.string.still_reading_license_file), Toast.LENGTH_LONG)
            .show();
      } else {
        // it is already done -- grab the result and display it.
        licenseReaderTask.setLicenseReaderListener(null);
        this.readLicenseComplete(licenseReaderTask.getResult());
      }
    }
  }

  @Override public void onDestroy() {
    if ( licenseReaderTask != null ) {
      licenseReaderTask.clearLicenseReaderListener(null);
    }
    super.onDestroy();
  }
}
