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
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.opendatakit.androidlibrary.R;
import org.opendatakit.properties.RequestCodes;
import org.opendatakit.utilities.AppNameUtil;

/**
 * Alert dialog implemented as a fragment for notifying user of a problem.
 *
 * @author mitchellsundt@gmail.com
 */
public class AlertDialogFragment extends DialogFragment implements DialogInterface
    .OnClickListener {

   private static final String t = AlertDialogFragment.class.getSimpleName();
   private static final String FRAGMENT_MANAGER_NULL_ERROR = "FragmentManager cannot be null";

   private static final String FRAGMENT_ID_KEY = "ADF_fragmentId";
   private static final String TITLE_KEY = "ADF_title";
   private static final String MESSAGE_KEY = "ADF_message";
   private static final String DISMISS_ACTIVITY_KEY = "ADF_dismiss_activity";
   private static final String OK_INVOKED_KEY = "ADF_okInvoked";

   public interface ConfirmAlertDialog {
      void okAlertDialog();
   }

   public static AlertDialogFragment newInstance(int fragmentId, boolean dismissActivity, String title, String message) {
      AlertDialogFragment frag = new AlertDialogFragment();
      Bundle args = new Bundle();
      args.putInt(FRAGMENT_ID_KEY, fragmentId);
      args.putBoolean(DISMISS_ACTIVITY_KEY, dismissActivity);
      args.putString(TITLE_KEY, title);
      args.putString(MESSAGE_KEY, message);
      frag.setArguments(args);
      return frag;
   }

   private String appName;

   private boolean dismissCalled;
   private boolean createDialogCalled;
   private boolean ok_invoked;

   private int fragmentId;
   private boolean dismissActivity;
   private String title;
   private String message;
   
   @Override public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      dismissCalled = false;
      createDialogCalled = false;
      ok_invoked = false;

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

   }

   private void initState(Bundle bundle) {
      if (bundle != null) {
         if (bundle.containsKey(FRAGMENT_ID_KEY)) {
            fragmentId = bundle.getInt(FRAGMENT_ID_KEY);
         }
         if (bundle.containsKey(DISMISS_ACTIVITY_KEY)) {
            dismissActivity = bundle.getBoolean(DISMISS_ACTIVITY_KEY);
         }
         if (bundle.containsKey(TITLE_KEY)) {
            title = bundle.getString(TITLE_KEY);
         }
         if (bundle.containsKey(MESSAGE_KEY)) {
            message = bundle.getString(MESSAGE_KEY);
         }
         if (bundle.containsKey(OK_INVOKED_KEY)) {
            ok_invoked = bundle.getBoolean(OK_INVOKED_KEY);
         }
      }
   }

   @Override public void onSaveInstanceState(Bundle outState) {
      dismissCalled = true;
      outState.putInt(FRAGMENT_ID_KEY, fragmentId);
      outState.putBoolean(DISMISS_ACTIVITY_KEY, dismissActivity);
      outState.putString(TITLE_KEY, title);
      outState.putString(MESSAGE_KEY, message);
      outState.putBoolean(OK_INVOKED_KEY, ok_invoked);
      super.onSaveInstanceState(outState);
   }

   public boolean dismissWasCalled() {
      return dismissCalled;
   }

   public boolean createDialogCalled() { return createDialogCalled; }

   private AlertDialog getAlertDialog() {
      Dialog dialog = getDialog();
      if (dialog instanceof AlertDialog) {
         return (AlertDialog) dialog;
      } else {
         throw new IllegalStateException("Somehow an AlertDialogFrament does not have an "
             + "AlertDialog");
      }
   }

   /**
    * Updates the message on the dialog
    *
    * @param newMessage the new mssage
    */
   public void setMessage(String newMessage) {
      message = newMessage;
      getAlertDialog().setMessage(message);
   }

   private void setTitle(String newTitle) {
      title = newTitle;
      getAlertDialog().setMessage(title);
   }

   @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
      String title = getArguments().getString(TITLE_KEY);
      String message = getArguments().getString(MESSAGE_KEY);

      final Integer fragmentId = getArguments().getInt(FRAGMENT_ID_KEY);

      FragmentManager mgr = getParentFragmentManager();
      Fragment f = mgr.findFragmentById(fragmentId);
      setTargetFragment(f, RequestCodes.ALERT_DIALOG.ordinal());

      AlertDialog alertDialog=new MaterialAlertDialogBuilder(getActivity(), R.style.Theme_MaterialComponents_Light_Dialog_Alert)
              .setTitle(title)
              .setMessage(message)
              .setCancelable(false)
              .setPositiveButton(getString(R.string.ok), this)
              .create();
      alertDialog.setCanceledOnTouchOutside(false);

      createDialogCalled = true;

      return alertDialog;
   }

   @Override public void onClick(DialogInterface dialog, int i) {
      ok_invoked = true;
      switch (i) {
      case DialogInterface.BUTTON_POSITIVE: // ok
         FragmentManager mgr = getParentFragmentManager();
         Fragment f = mgr.findFragmentById(fragmentId);

         if (f instanceof ConfirmAlertDialog) {
            ((ConfirmAlertDialog) f).okAlertDialog();
         }
         dialog.dismiss();
         break;
      }
   }

   @Override public void onDismiss(DialogInterface dialog) {
      dismissCalled = true;
      super.onDismiss(dialog);
      if ( !ok_invoked  && dismissActivity) {
         Activity a = getActivity();
         if ( a != null ) {
            a.setResult(Activity.RESULT_CANCELED);
            a.finish();
         }
      }
   }

   ///////////////////////////////////////////////////////////////////////////////////////
   /////////////////   STATIC HELPER FUNCTIONS TO AVOID DUPLICATE CODE   /////////////////
   ///////////////////////////////////////////////////////////////////////////////////////

   public static AlertDialogFragment eitherReuseOrCreateNew(String alertDialogTag,
       AlertDialogFragment inputAlertDialogFragment, FragmentManager fragmentManager,
       boolean dismissActivity, int fragmentId, String title, String message) {

      if (fragmentManager == null) {
         throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
      }

      AlertDialogFragment outputAlertDialogFragment = null;

      Fragment dialog = fragmentManager.findFragmentByTag(alertDialogTag);

      if (dialog != null && (dialog instanceof AlertDialogFragment)) {
         outputAlertDialogFragment = (AlertDialogFragment) dialog;
         if (outputAlertDialogFragment.dismissWasCalled()) {
            outputAlertDialogFragment = null;
         }
      } else {

         // failed to find the alert dialog, dimiss the dangling reference if dismiss
         // wasn't previously called
         if (inputAlertDialogFragment != null && !inputAlertDialogFragment.dismissWasCalled()) {
            inputAlertDialogFragment.dismiss();
         }
         outputAlertDialogFragment = null;
      }

      // if no pre-existing fragment create one, else update the message
      if (outputAlertDialogFragment == null) {
         outputAlertDialogFragment = AlertDialogFragment.newInstance(fragmentId, dismissActivity, title, message);
         if(!outputAlertDialogFragment.isAdded()) {
            outputAlertDialogFragment.show(fragmentManager, alertDialogTag);
         }
      } else {
         outputAlertDialogFragment.setTitle(title);
         outputAlertDialogFragment.setMessage(message);
      }

      return outputAlertDialogFragment;
   }

   public static void dismissDialogs(String alertDialogTag,
       AlertDialogFragment inputAlertDialogFragment, FragmentManager fragmentManager) {

      if (fragmentManager == null) {
         throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
      }

      // dismiss the provided reference reference
      if (inputAlertDialogFragment != null && !inputAlertDialogFragment.dismissWasCalled()) {
         inputAlertDialogFragment.dismiss();
      }

      // then try to find any dangling reference that has not been dismissed
      Fragment dialog = fragmentManager.findFragmentByTag(alertDialogTag);
      if (dialog != null && (dialog instanceof AlertDialogFragment)) {
         AlertDialogFragment tmp = (AlertDialogFragment) dialog;
         if(!tmp.dismissWasCalled()) {
            tmp.dismiss();
         }
      }
   }
}
