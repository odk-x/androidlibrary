package org.opendatakit.fragment;

import androidx.fragment.app.FragmentManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import org.opendatakit.logging.WebLogger;

/**
 * Created by wbrunette on 1/14/2018.
 */

public class AlertNProgessMsgFragmentMger {

   // used for logging
   private static final String TAG = AlertNProgessMsgFragmentMger.class.getSimpleName();

   // keys for the data being retained
   private static final String TITLE_KEY = "IMdialogTitleKey";
   private static final String MSG_KEY = "IMdialogMsgKey";
   private static final String DIALOG_STATE_KEY = "IMdialogStateKey";
   private static final String APPNAME_KEY = "IMappNameKey";
   private static final String ALERT_TAG_KEY = "IMalertDialogKey";
   private static final String PROGRESS_TAG_KEY = "IMprogressDialogKey";
   private static final String ALERT_DISMISS_KEY = "IMalertDismissKey";
   private static final String PROGRESS_DISMISS_KEY = "IMprogessDismissKey";
   private static final String PROGRESS_VALUE_KEY = "IMprogessValueKey";
   private static final String PROGRESS_MAX_VALUE_KEY = "IMprogessMAXValueKey";

   /***
    * Static funtion used to restore AlertNProgessMsgFragmentMger. NOTE: AlertNProgessMsgFragmentMger will
    * ONLY try to restore itself if it existed before, otherwise returns null. To verify it
    * existed before it compares the AppName, ProgressDialogTag, and AlertDialogTag. Therefore
    * arguments cannot be null
    *
    * @param appName    appName of executing app
    * @param initAlertDialogTag  the unique tag used to identify the alert dialog fragment
    * @param initDialogProgressTag the unique tag used to identify the alert dialog fragment
    * @param savedInstanceState the bundle passed by Android lifecycle manager when creating
    * @return
    */
   public static final AlertNProgessMsgFragmentMger restoreInitMessaging(@NonNull String appName,
       @NonNull String initAlertDialogTag, @NonNull String initDialogProgressTag, Bundle savedInstanceState) {

      if (appName == null || initAlertDialogTag == null || initDialogProgressTag == null) {
         throw new IllegalArgumentException(
             "AlertNProgessMsgFragmentMger willl ONLY try to restore "
                 + "itself if it existed before. To verify it existed before it compares the AppName,"
                 + " ProgressDialogTag, and AlertDialogTag. Therefore arguments cannot be null");
      }

      if (savedInstanceState != null) {
         String bundleAppName = savedInstanceState.getString(APPNAME_KEY);
         String bundleAlertTag = savedInstanceState.getString(ALERT_TAG_KEY);
         String bundleProgressTag = savedInstanceState.getString(PROGRESS_TAG_KEY);
         boolean alertDismissActivity = savedInstanceState.getBoolean(ALERT_DISMISS_KEY);
         boolean progressDismissActivity = savedInstanceState.getBoolean(PROGRESS_DISMISS_KEY);

         // verify appName, AlertTag, and ProgressTag before proceed with recreating
         // AlertNProgessMsgFragmentMger
         if (appName.equals(bundleAppName) && initAlertDialogTag.equals(bundleAlertTag)
             && initDialogProgressTag.equals(bundleProgressTag)) {

            AlertNProgessMsgFragmentMger restore = new AlertNProgessMsgFragmentMger(appName,
                bundleAlertTag, bundleProgressTag, alertDismissActivity, progressDismissActivity);
            restore.currentTitle = savedInstanceState.getString(TITLE_KEY);
            restore.currentMessage = savedInstanceState.getString(MSG_KEY);
            restore.mDialogState = DialogState
                .valueOf(savedInstanceState.getString(DIALOG_STATE_KEY));
            restore.progressValue = savedInstanceState.getInt(PROGRESS_VALUE_KEY);
            restore.maxValue = savedInstanceState.getInt(PROGRESS_MAX_VALUE_KEY);
            return restore;
         } else {
            throw new RuntimeException("Some how retrieved saved date for the WRONG dialog!!!");
         }
      }
      return null;
   }

   // The types of dialogs we handle
   public enum DialogState {
      Progress, Alert, None
   }

   // data to save across orientation changes
   private String currentTitle;
   private String currentMessage;
   private DialogState mDialogState;
   private int progressValue;
   private int maxValue;

   // member variables not saved
   private String appName;
   private String progressDialogTag;
   private String alertDialogTag;

   private ProgressDialogFragment progressDialogFragment;
   private AlertDialogFragment alertDialogFragment;

   private boolean dialogsClearedForFragmentShutdown;

   private boolean alertDismissActivity;
   private boolean progressDismissActivity;

   private boolean onSaveInstanceStateCalled;

   // Make default constructor private
   private AlertNProgessMsgFragmentMger() {
   }

   /**
    * @param appName               appName of executing app
    * @param initAlertDialogTag    the unique tag used to identify the alert dialog fragment
    * @param initDialogProgressTag the unique tag used to identify the alert dialog fragment
    */
   public AlertNProgessMsgFragmentMger(String appName, String initAlertDialogTag,
       String initDialogProgressTag, boolean alertDismissActivity, boolean
       progressDismissActivity) {
      mDialogState = DialogState.None;
      dialogsClearedForFragmentShutdown = false;
      onSaveInstanceStateCalled = false;
      this.appName = appName;
      this.alertDialogTag = initAlertDialogTag;
      this.progressDialogTag = initDialogProgressTag;
      this.alertDismissActivity = alertDismissActivity;
      this.progressDismissActivity = progressDismissActivity;
   }

   /**
    * To be able to restore state, saves manager state in bundle
    *
    * @param bundleOfState
    */
   public void addStateToSaveStateBundle(Bundle bundleOfState) {
      onSaveInstanceStateCalled = true;
      bundleOfState.putString(APPNAME_KEY, appName);
      bundleOfState.putString(ALERT_TAG_KEY, alertDialogTag);
      bundleOfState.putString(PROGRESS_TAG_KEY, progressDialogTag);
      bundleOfState.putString(TITLE_KEY, currentTitle);
      bundleOfState.putString(MSG_KEY, currentMessage);
      bundleOfState.putString(DIALOG_STATE_KEY, mDialogState.name());
      bundleOfState.putBoolean(ALERT_DISMISS_KEY, alertDismissActivity);
      bundleOfState.putBoolean(PROGRESS_DISMISS_KEY, progressDismissActivity);
      bundleOfState.putInt(PROGRESS_VALUE_KEY, progressValue);
      bundleOfState.putInt(PROGRESS_MAX_VALUE_KEY, maxValue);
   }

   /**
    * Restores previous dialog if any was present
    */
   public void restoreDialog(FragmentManager fragmentManager, int fragmentId) {
      dialogsClearedForFragmentShutdown = false;
      onSaveInstanceStateCalled = false;
      switch (mDialogState) {
      case Progress:
         restoreProgressDialog(fragmentManager);
         break;
      case Alert:
         restoreAlertDialog(fragmentManager, fragmentId);
         break;
      default:
      case None:
         //do nothing
      }
   }

   public boolean shutdownCalled() {
      return dialogsClearedForFragmentShutdown || onSaveInstanceStateCalled;
   }

   public boolean displayingProgressDialog() {
      return mDialogState == DialogState.Progress && !shutdownCalled();
   }

   public boolean hasDialogBeenCreated() {
      switch (mDialogState) {
      case Progress:
         if(progressDialogFragment != null) {
            return progressDialogFragment.createDialogCalled();
         }
         break;
      case Alert:
         if(alertDialogFragment != null) {
            return alertDialogFragment.createDialogCalled();
         }
         break;
      default:
      case None:
         return true;
      }
     return false;
   }

    public DialogState getDialogState() {
        return mDialogState;
    }

   public void clearDialogsAndRetainCurrentState(FragmentManager fragmentManager) {
      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }
      dialogsClearedForFragmentShutdown = true;

      ProgressDialogFragment.dismissDialogs(progressDialogTag, progressDialogFragment,
          fragmentManager);
      progressDialogFragment = null;
      AlertDialogFragment.dismissDialogs(alertDialogTag, alertDialogFragment,fragmentManager);
      alertDialogFragment = null;
   }

   /**
    * Creates an alert dialog with the and message. If shouldExit is
    * set to true, the activity will exit when the user clicks "ok".
    *
    * @param title           the title for the dialog
    * @param message         the message for the dialog
    * @param fragmentManager reference to fragment manager
    * @param fragmentId      the id of the fragment that the alert is associated with
    */
   public void createAlertDialog(String title, String message, FragmentManager fragmentManager,
       int fragmentId) {
      WebLogger.getLogger(appName)
          .d(TAG, "in private void createAlertDialog(String title, String message) {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      if(dialogsClearedForFragmentShutdown) {
         throw new IllegalStateException("Getting an create" +
                 "alert when the dialogs have been cleared for shutdown");
      }

      if(onSaveInstanceStateCalled) {
         WebLogger.getLogger(appName).e(TAG, "Trying to create an Alert Dialog after" +
                 "onSaveStateInstance is called");
      }

      if (mDialogState == DialogState.Progress) {
         dismissProgressDialog(fragmentManager);
      }

      currentTitle = title;
      currentMessage = message;
      restoreAlertDialog(fragmentManager, fragmentId);
   }

   /**
    * Creates an alert dialog with the and message. If shouldExit is
    * set to true, the activity will exit when the user clicks "ok".
    *
    * @param title           the title for the dialog
    * @param message         the message for the dialog
    * @param fragmentManager reference to fragment manager
    */
   public void createProgressDialog(String title, String message, FragmentManager fragmentManager) {
      WebLogger.getLogger(appName)
          .d(TAG, "in private void createAlertDialog(String title, String message) {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      if(dialogsClearedForFragmentShutdown) {
         throw new IllegalStateException("Getting an create" +
              "progress when the dialogs have been cleared for shutdown");
      }

      if(onSaveInstanceStateCalled) {
         WebLogger.getLogger(appName).e(TAG, "Trying to create an Progress Dialog after" +
                 "onSaveStateInstance is called");
      }

      if (mDialogState == DialogState.Alert) {
         dismissAlertDialog(fragmentManager);
      }

      currentTitle = title;
      currentMessage = message;
      restoreProgressDialog(fragmentManager);
   }

   /**
    * Sets the message based on the passed message
    *
    * @param message         the message to show in the progress dialog
    * @param fragmentManager reference to fragment manager
    */
   public void updateProgressDialogMessage(String message, FragmentManager fragmentManager) {
      WebLogger.getLogger(appName)
          .d(TAG, "in private void updateProgressDialogMessage(String message) {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      if(onSaveInstanceStateCalled) {
         // we are in the middle of shutting down, update will be lost
         WebLogger.getLogger(appName).e(TAG, "Trying to Update an Progress Dialog after" +
                 "onSaveStateInstance is called");
      }

      if(dialogsClearedForFragmentShutdown) {
         throw new IllegalStateException("Getting an update when the dialogs have been cleared "
             + "for shutdown, should check displayingProgressDialog()");
      }

      if (mDialogState == DialogState.Progress) {
         if (progressDialogFragment == null) {
            restoreProgressDialog(fragmentManager);
         }
         progressDialogFragment.setMessage(message);
         currentMessage = message;
      } else {
         throw new IllegalStateException("No Progress Dialog is currently showing");
      }
   }

   /**
    * Sets the message based on the passed message
    *
    * @param message         the message to show in the progress dialog
    * @param progressStep    the current progress, a value between 0 and maxStep
    * @param maxStep         the maximum of the scale for progressStep
    * @param fragmentManager reference to fragment manager
    */
   public void updateProgressDialogMessage(String message, int progressStep, int maxStep,
       FragmentManager fragmentManager) {
      WebLogger.getLogger(appName)
          .d(TAG, "in private void updateProgressDialogMessage(String message) {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      if(onSaveInstanceStateCalled) {
         // we are in the middle of shutting down, update will be lost
         WebLogger.getLogger(appName).e(TAG, "Trying to Update an Progress Dialog after" +
                 "onSaveStateInstance is called");
         return;
      }

      if(dialogsClearedForFragmentShutdown) {
         throw new IllegalStateException("Getting an update when the dialogs have been cleared "
             + "for shutdown, should check displayingProgressDialog()");
      }

      if (mDialogState == DialogState.Progress) {
         if (progressDialogFragment == null) {
            restoreProgressDialog(fragmentManager);
         }
         currentMessage = message;
         progressValue = progressStep;
         maxValue = maxStep;
         progressDialogFragment.setMessage(message, progressValue, maxValue);
      } else {
         throw new IllegalStateException("No Progress Dialog is currently showing");
      }
   }

   /**
    * Tries to find and dismiss a progressDialog
    *
    * @param fragmentManager reference to fragment manager
    */
   public void dismissProgressDialog(FragmentManager fragmentManager) {
      WebLogger.getLogger(appName).d(TAG, "in void dismissProgressDialog() {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      if (mDialogState == DialogState.Progress) {
         mDialogState = DialogState.None;
      }

      ProgressDialogFragment.dismissDialogs(progressDialogTag, progressDialogFragment,
          fragmentManager);
      progressDialogFragment = null;
   }

   /**
    * Tries to find and dismiss alertDialog
    *
    * @param fragmentManager reference to fragment manager
    */
   public void dismissAlertDialog(FragmentManager fragmentManager) {
      WebLogger.getLogger(appName).d(TAG, "in void dismissAlertDialog() {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      if (mDialogState == DialogState.Alert) {
         mDialogState = DialogState.None;
      }

      AlertDialogFragment.dismissDialogs(alertDialogTag, alertDialogFragment,fragmentManager);
      alertDialogFragment = null;
   }

   ////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * Dismisses an alertDialog if it already exists, sets the message, title and state for an
    * existing progress dialog if one exists, otherwise creates a new dialog and shows it
    *
    * @param fragmentManager reference to fragment manager
    */
   private void restoreProgressDialog(FragmentManager fragmentManager) {
      WebLogger.getLogger(appName).d(TAG, "in private void restoreProgressDialog() {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      mDialogState = DialogState.Progress;

      progressDialogFragment = ProgressDialogFragment.eitherReuseOrCreateNew(
          progressDialogTag, progressDialogFragment, fragmentManager, currentTitle, currentMessage, progressDismissActivity);
   }



   /**
    * Dismisses a progressDialog if it already exists, sets the message, title and state for an
    * existing alert dialog if one exists, otherwise creates a new alert and shows it
    *
    * @param fragmentManager reference to fragment manager
    */
   private void restoreAlertDialog(FragmentManager fragmentManager, int fragmentId) {
      WebLogger.getLogger(appName).d(TAG, "in private void restoreAlertDialog() {");

      if (fragmentManager == null) {
         throw new IllegalArgumentException("FragmentManager cannot be null");
      }

      mDialogState = DialogState.Alert;

      alertDialogFragment = AlertDialogFragment.eitherReuseOrCreateNew(alertDialogTag, alertDialogFragment,
          fragmentManager, alertDismissActivity, fragmentId, currentTitle, currentMessage);
   }
}
