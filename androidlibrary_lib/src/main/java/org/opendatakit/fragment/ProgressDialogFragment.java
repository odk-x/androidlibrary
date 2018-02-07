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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.androidlibrary.R;
import org.opendatakit.logging.WebLogger;

/**
 * Fragment-version of Progress dialog, used all over the place
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("unused") public class ProgressDialogFragment extends DialogFragment
    implements DialogInterface.OnClickListener {

   private static final String t = ProgressDialogFragment.class.getSimpleName();
   private static final String BUTTON_CLICKED_LOGTXT = "button clicked - button text:";
   private static final String NULL_TXT = "null";
   private static final String FRAGMENT_MANAGER_NULL_ERROR = "FragmentManager cannot be null";

   /**
    * An activity that creates an instance of the progress dialog fragment must
    * implement this interface in order to receive event callbacks.
    */
   public interface ProgressDialogListener {
      public void onProgressDialogPositiveButtonClick(ProgressDialogFragment dialog);

      public void onProgressDialogNegativeButtonClick(ProgressDialogFragment dialog);

      public void onProgressDialogNeutralButtonClick(ProgressDialogFragment dialog);
   }

   public static final String BUNDLE_KEY_TITLE = "PDF_title";
   public static final String BUNDLE_KEY_MESSAGE = "PDF_message";
   public static final String BUNDLE_KEY_DISMISSABLE = "PDF_canDismiss";
   public static final String BUNDLE_KEY_POSI_BUTTON_TXT = "PDF_posButtonMsg";
   public static final String BUNDLE_KEY_NEGA_BUTTON_TXT = "PDF_negButtonMsg";
   public static final String BUNDLE_KEY_NEUT_BUTTON_TXT = "PDF_neuButtonMsg";

   /**
    * Returns a new ProgressDialogFragment with the given title and message
    *
    * @param title              the title to be displayed in the dialog
    * @param message            the message to be displayed in the dialog
    * @param canDismissDialog   the user can dismiss the dialog
    * @param positiveButtonText if non-null, willl create a button with that name
    * @param negativeButtonText if non-null, willl create a button with that name
    * @param neutralButtonText  if non-null, willl create a button with that name
    * @return a ProgressDialogFragment with the correct arguments set
    */
   public static ProgressDialogFragment newInstance(String title, String message,
       boolean canDismissDialog, String positiveButtonText, String negativeButtonText,
       String neutralButtonText) {
      ProgressDialogFragment frag = new ProgressDialogFragment();
      Bundle args = new Bundle();
      args.putString(BUNDLE_KEY_TITLE, title);
      args.putString(BUNDLE_KEY_MESSAGE, message);
      args.putBoolean(BUNDLE_KEY_DISMISSABLE, canDismissDialog);
      args.putString(BUNDLE_KEY_POSI_BUTTON_TXT, positiveButtonText);
      args.putString(BUNDLE_KEY_NEGA_BUTTON_TXT, negativeButtonText);
      args.putString(BUNDLE_KEY_NEUT_BUTTON_TXT, neutralButtonText);

      frag.setArguments(args);
      return frag;
   }

   // progress fragment save state
   private String appName;
   private String title;
   private String message;
   private boolean canDismissDialog;
   private String positiveButtonText;
   private String negativeButtonText;
   private String neutralButtonText;

   private boolean dismissCalled;

   // Use this instance of the interface to deliver action events
   private ProgressDialogListener progressDialogListener;

   public boolean dismissWasCalled() {
      return dismissCalled;
   }

   /**
    * Override the Fragment.onAttach() method to get appName, initailize variables,
    * and instantiate the NoticeDialogListener
    *
    * @param context
    */
   @Override public void onAttach(Context context) {
      super.onAttach(context);

      dismissCalled = false;

      Activity activity = getActivity();

      // Verify that the host activity implements the listener callback interface
      if (activity instanceof ProgressDialogListener) {
         // Instantiate the the listener so we can send events to the host
         progressDialogListener = (ProgressDialogListener) activity;
      }

      // get the appName from the ODK app aware infrastructure
      if (activity instanceof IAppAwareActivity) {
         appName = ((IAppAwareActivity) activity).getAppName();
      } else {
         throw new RuntimeException("The activity that ProgressDialogListener is attaching to is "
             + "NOT an IAppAwareActivity");
      }
   }

   @Override public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // first set internal state based on arugments
      initState(getArguments());

      // second update internal state based on any saved date
      initState(savedInstanceState);
   }

   private void initState(Bundle bundle) {
      if (bundle != null) {
         if (bundle.containsKey(BUNDLE_KEY_TITLE)) {
            title = bundle.getString(BUNDLE_KEY_TITLE);
         }
         if (bundle.containsKey(BUNDLE_KEY_MESSAGE)) {
            message = bundle.getString(BUNDLE_KEY_MESSAGE);
         }
         if (bundle.containsKey(BUNDLE_KEY_DISMISSABLE)) {
            canDismissDialog = bundle.getBoolean(BUNDLE_KEY_DISMISSABLE);
         }
         if (bundle.containsKey(BUNDLE_KEY_POSI_BUTTON_TXT)) {
            positiveButtonText = bundle.getString(BUNDLE_KEY_POSI_BUTTON_TXT);
         }
         if (bundle.containsKey(BUNDLE_KEY_NEGA_BUTTON_TXT)) {
            negativeButtonText = bundle.getString(BUNDLE_KEY_NEGA_BUTTON_TXT);
         }
         if (bundle.containsKey(BUNDLE_KEY_NEUT_BUTTON_TXT)) {
            neutralButtonText = bundle.getString(BUNDLE_KEY_NEUT_BUTTON_TXT);
         }
      }
   }

   @Override public void onSaveInstanceState(Bundle outState) {
      Log.e(t, "WRB: onSaveInstanceState called on ProgressDialogFragment");
      dismissCalled = true;
      outState.putString(BUNDLE_KEY_TITLE, title);
      outState.putString(BUNDLE_KEY_MESSAGE, message);
      outState.putBoolean(BUNDLE_KEY_DISMISSABLE, canDismissDialog);
      outState.putString(BUNDLE_KEY_POSI_BUTTON_TXT, positiveButtonText);
      outState.putString(BUNDLE_KEY_NEGA_BUTTON_TXT, negativeButtonText);
      outState.putString(BUNDLE_KEY_NEUT_BUTTON_TXT, neutralButtonText);
      super.onSaveInstanceState(outState);
   }

   /**
    * Updates the message on the dialog
    *
    * @param message the new mssage
    */
   public void setMessage(String message) {
      Dialog dialog = getDialog();
      if (dialog instanceof ProgressDialog) {
         ((ProgressDialog) dialog).setMessage(message);
      }
   }

   public void setMessage(String message, int progress, int max) {
      if (getDialog() instanceof ProgressDialog) {
         ProgressDialog dlg = (ProgressDialog) this.getDialog();
         dlg.setMessage(message);
         if (progress == -1) {
            dlg.setIndeterminate(true);
         } else {
            dlg.setIndeterminate(false);
            dlg.setMax(max);
            dlg.setProgress(progress);
         }
      }
   }

   /**
    * Called from onCreate when a dialog object needs to be created
    *
    * @param savedInstanceState unused
    * @return a Dialog object with the correct message and title
    */
   @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
      ProgressDialog mProgressDialog = new ProgressDialog(getActivity(), getTheme());
      mProgressDialog.setTitle(title);
      mProgressDialog.setMessage(message);
      mProgressDialog.setIndeterminate(true);
      mProgressDialog.setCancelable(false);
      mProgressDialog.setCanceledOnTouchOutside(false);

      if (positiveButtonText != null) {
         mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText, this);
      }

      if (negativeButtonText != null) {
         mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText, this);
      }

      if (neutralButtonText != null) {
         mProgressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, neutralButtonText, this);
      }

      if (canDismissDialog) {
         mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
      } else {
         mProgressDialog.setIcon(R.drawable.ic_info_outline_black_24dp);
      }

      return mProgressDialog;
   }

   @Override public void onClick(DialogInterface dialog, int whichButton) {
      if (progressDialogListener == null) {
         WebLogger.getLogger(appName)
             .i(t, t + "progressDialogListener is null so cannot " + "dispatch an onClickEvent");
      }
      switch (whichButton) {
      case DialogInterface.BUTTON_POSITIVE:
         WebLogger.getLogger(appName).i(t,
             t + "positive" + BUTTON_CLICKED_LOGTXT + ((positiveButtonText == null) ?
                 NULL_TXT :
                 positiveButtonText));
         progressDialogListener.onProgressDialogPositiveButtonClick(this);
         break;
      case DialogInterface.BUTTON_NEGATIVE:
         WebLogger.getLogger(appName).i(t,
             t + "negative " + BUTTON_CLICKED_LOGTXT + ((negativeButtonText == null) ?
                 NULL_TXT :
                 negativeButtonText));
         progressDialogListener.onProgressDialogNegativeButtonClick(this);
         break;
      case DialogInterface.BUTTON_NEUTRAL:
         WebLogger.getLogger(appName).i(t,
             t + "neutral" + BUTTON_CLICKED_LOGTXT + ((neutralButtonText == null) ?
                 NULL_TXT :
                 neutralButtonText));
         progressDialogListener.onProgressDialogNeutralButtonClick(this);
         break;
      default:
         //do nothing
         break;
      }
   }

   @Override public void onDismiss(DialogInterface dialog) {
      dismissCalled = true;
      Log.e(t, "WRB: onDimiss called on ProgressDialogFragment");
      super.onDismiss(dialog);

      if (canDismissDialog) {
         Activity a = getActivity();
         if (a != null) {
            a.setResult(Activity.RESULT_CANCELED);
            a.finish();
         }
      }
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   /////////////////   STATIC HELPER FUNCTIONS TO AVOID DUPLICATE CODE   /////////////////
   ///////////////////////////////////////////////////////////////////////////////////////
   public static ProgressDialogFragment eitherReuseOrCreateNew(String progressDialogTag,
       ProgressDialogFragment progressDialogFragment, FragmentManager fragmentManager, String title,
       String message, boolean canDismissDialog) {
      return ProgressDialogFragment.eitherReuseOrCreateNew(progressDialogTag, progressDialogFragment,
          fragmentManager, title, message, canDismissDialog,
      null, null,null);
   }


   public static ProgressDialogFragment eitherReuseOrCreateNew(String progressDialogTag,
       ProgressDialogFragment progressDialogFragment, FragmentManager fragmentManager, String title, String message, boolean canDismissDialog,
       String positiveButtonText, String negativeButtonText, String neutralButtonText) {

      Log.e(t, "WRB: in eitherReuseOrCreateNew");

      if (fragmentManager == null) {
         throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
      }

      Fragment dialog = fragmentManager.findFragmentByTag(progressDialogTag);
      Log.e(t, "WRB: search for pre-exising dialog");
      // attempt to get the pre-existing fragment that has not already been dismissed
      if (dialog != null && (dialog instanceof ProgressDialogFragment)) {
         Log.e(t, "WRB:FOUND pre-exising dialog");
         progressDialogFragment = (ProgressDialogFragment) dialog;
         if (progressDialogFragment.dismissWasCalled()) {
            progressDialogFragment = null;
         }
      } else {
         Log.e(t, "WRB: NOT found pre-exising dialog");
         // failed to find the progress dialog, dismiss the dangling reference if dismiss
         // wasn't previously called, probably unnecessary
         if (progressDialogFragment != null && !progressDialogFragment.dismissWasCalled()) {
            progressDialogFragment.dismiss();
         }
         progressDialogFragment = null;
      }

      // if no-prexising fragment create one, else update the message
      if (progressDialogFragment == null) {
         Log.e(t, "WRB: creating NEW progress dialog");
         progressDialogFragment = ProgressDialogFragment
             .newInstance(title, message, canDismissDialog, positiveButtonText, negativeButtonText,
                 neutralButtonText);
      } else {
         Log.e(t, "WRB: REUSING progress dialog");
         progressDialogFragment.getDialog().setTitle(title);
         progressDialogFragment.setMessage(message);
      }

      return progressDialogFragment;
   }

   public static void dismissDialogs(String progressDialogTag,
       ProgressDialogFragment progressDialogFragment, FragmentManager fragmentManager) {

      if (fragmentManager == null) {
         throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
      }

      // dismiss the provided reference reference
      if (progressDialogFragment != null && !progressDialogFragment.dismissWasCalled()) {
         progressDialogFragment.dismiss();
      }

      // then try to find any dangling reference that has not been dismissed
      Fragment dialog = fragmentManager.findFragmentByTag(progressDialogTag);
      if (dialog != null && (dialog instanceof ProgressDialogFragment)) {
         ProgressDialogFragment tmp = (ProgressDialogFragment) dialog;
         if (!tmp.dismissWasCalled()) {
            tmp.dismiss();
         }
      }
   }
}
