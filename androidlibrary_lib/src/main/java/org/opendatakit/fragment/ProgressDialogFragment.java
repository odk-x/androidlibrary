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

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.opendatakit.androidlibrary.R;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.utilities.AppNameUtil;

/**
 * Fragment-version of Progress dialog, used all over the place
 *
 * @author mitchellsundt@gmail.com
 */
public class ProgressDialogFragment extends DialogFragment
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
      void onProgressDialogPositiveButtonClick(ProgressDialogFragment dialog);

      void onProgressDialogNegativeButtonClick(ProgressDialogFragment dialog);

      void onProgressDialogNeutralButtonClick(ProgressDialogFragment dialog);
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
    * @param positiveButtonText if non-null, will create a button with that name
    * @param negativeButtonText if non-null, will create a button with that name
    * @param neutralButtonText  if non-null, will create a button with that name
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
   private boolean createDialogCalled;

   // Use this instance of the interface to deliver action events
   private ProgressDialogListener progressDialogListener;

   public boolean dismissWasCalled() {
      return dismissCalled;
   }

   public boolean createDialogCalled() { return createDialogCalled; }

   @Override public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      dismissCalled = false;
      createDialogCalled = false;

      // first set internal state based on arugments
      initState(getArguments());

      // second update internal state based on any saved date
      initState(savedInstanceState);
   }

   @Override public void onActivityCreated (Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      Activity activity = getActivity();

      if(appName == null) {
         appName = AppNameUtil.getAppNameFromActivity(activity);
      }

      // Verify that the host activity implements the listener callback interface
      if (activity instanceof ProgressDialogListener) {
         // Instantiate the the listener so we can send events to the host
         progressDialogListener = (ProgressDialogListener) activity;
         WebLogger.getLogger(appName)
             .i(t, t + "progressDialogListener established reference to an activity");
      }
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
      dismissCalled = true;
      outState.putString(BUNDLE_KEY_TITLE, title);
      outState.putString(BUNDLE_KEY_MESSAGE, message);
      outState.putBoolean(BUNDLE_KEY_DISMISSABLE, canDismissDialog);
      outState.putString(BUNDLE_KEY_POSI_BUTTON_TXT, positiveButtonText);
      outState.putString(BUNDLE_KEY_NEGA_BUTTON_TXT, negativeButtonText);
      outState.putString(BUNDLE_KEY_NEUT_BUTTON_TXT, neutralButtonText);
      super.onSaveInstanceState(outState);
   }

   private AlertDialog getProgressDialog() {
      Dialog dialog = getDialog();
      if (dialog instanceof AlertDialog) {
         return (AlertDialog) dialog;
      } else {
         throw new IllegalStateException("Somehow an ProgressDialogFrament does not have an "
             + "ProgressDialog");
      }
   }

   /**
    * Updates the message on the dialog
    *
    * @param newMessage the new message
    */
   public void setMessage(String newMessage) {
      message = newMessage;
      tvMessage.setText(message);
   }

   /**
    * Updates the title of the dialog
    *
    * @param newTitle the new title
    */
   private void setTitle(String newTitle) {
      title = newTitle;
      getProgressDialog().setTitle(title);
   }

   public void setMessage(String message, int progress, int max) {
      this.message=message;
      tvMessage.setText(message);
      if(progress<=0)
         progressIndicator.setIndeterminate(true);
      else {
         progressIndicator.setIndeterminate(false);
         progressIndicator.setMax(max);
         progressIndicator.setProgressCompat(progress,true);
      }
   }

   TextView tvMessage;
   LinearProgressIndicator progressIndicator;

   /**
    * Called from onCreate when a dialog object needs to be created
    *
    * @param savedInstanceState unused
    * @return a Dialog object with the correct message and title
    */
   @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

      LayoutInflater inflater=getLayoutInflater();
      View view=inflater.inflate(R.layout.progress_indicator_layout,null);

      AlertDialog dialog=new MaterialAlertDialogBuilder(getActivity(),R.style.Theme_MaterialComponents_Light_Dialog_Alert)
              .setTitle(title)
              .setCancelable(false)
              .setView(view)
              .create();

      tvMessage=view.findViewById(R.id.tvMessage);
      progressIndicator=view.findViewById(R.id.progress_indicator);

      tvMessage.setText(message);
      progressIndicator.setIndeterminate(true);

      dialog.setCanceledOnTouchOutside(false);

      if (positiveButtonText != null) {
         dialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText, this);
      }

      if (negativeButtonText != null) {
         dialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText, this);
      }

      if (neutralButtonText != null) {
         dialog.setButton(DialogInterface.BUTTON_NEUTRAL, neutralButtonText, this);
      }

      createDialogCalled = true;

      return dialog;
   }

   @Override public void onClick(DialogInterface dialog, int whichButton) {
      if (progressDialogListener == null) {
         WebLogger.getLogger(appName)
             .i(t, t + "progressDialogListener is null so cannot " + "dispatch an onClickEvent");
         return;
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
       ProgressDialogFragment inputProgressDialogFragment, FragmentManager fragmentManager, String
       title, String message, boolean canDismissDialog,
       String positiveButtonText, String negativeButtonText, String neutralButtonText) {

      if (fragmentManager == null) {
         throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
      }

      ProgressDialogFragment outputProgressDialogFragment = null;

      Fragment dialog = fragmentManager.findFragmentByTag(progressDialogTag);
      // attempt to get the pre-existing fragment that has not already been dismissed
      if (dialog != null && (dialog instanceof ProgressDialogFragment)) {
         outputProgressDialogFragment = (ProgressDialogFragment) dialog;
         if (outputProgressDialogFragment.dismissWasCalled()) {
            outputProgressDialogFragment = null;
         }
      } else {
         // failed to find the progress dialog, dismiss the dangling reference if dismiss
         // wasn't previously called, probably unnecessary
         if (inputProgressDialogFragment != null && !inputProgressDialogFragment.dismissWasCalled()) {
            inputProgressDialogFragment.dismiss();
         }
         outputProgressDialogFragment = null;
      }

      // if no pre-existing fragment create one, else update the message
      if (outputProgressDialogFragment == null) {
         outputProgressDialogFragment = ProgressDialogFragment
             .newInstance(title, message, canDismissDialog, positiveButtonText, negativeButtonText,
                 neutralButtonText);
         if(!outputProgressDialogFragment.isAdded()) {
            outputProgressDialogFragment.show(fragmentManager, progressDialogTag);
         }
      } else {
         outputProgressDialogFragment.setTitle(title);
         outputProgressDialogFragment.setMessage(message);
      }

      return outputProgressDialogFragment;
   }

   public static void dismissDialogs(String progressDialogTag,
       ProgressDialogFragment inputProgressDialogFragment, FragmentManager fragmentManager) {

      if (fragmentManager == null) {
         throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
      }

      // dismiss the provided reference reference
      if (inputProgressDialogFragment != null && !inputProgressDialogFragment.dismissWasCalled()) {
         inputProgressDialogFragment.dismiss();
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
