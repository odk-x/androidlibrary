/*
 * Copyright (C) 2016 University of Washington
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.androidlibrary.R;

/**
 * Fragment-version of AlertDialog
 *
 * @author mitchellsundt@gmail.com
 */
public class DismissableOutcomeDialogFragment extends DialogFragment {

  private static final String t = DismissableOutcomeDialogFragment.class.getSimpleName();
  private static final String FRAGMENT_MANAGER_NULL_ERROR = "FragmentManager cannot be null";

  private static final String TITLE_KEY = "DODF_title";
  private static final String MESSAGE_KEY = "DODF_message";
  private static final String IS_OK_KEY = "DODF_isOkay";
  private static final String HANDLER_TAG_KEY = "DODF_handlerTag";

  private static final String OK_INVOKED_KEY = "DODF_ok_invoked";


  public interface ISyncOutcomeHandler {
    void onSyncCompleted();
  }

  public static DismissableOutcomeDialogFragment newInstance(String title, String message, boolean isOk, String handlerTag) {
    DismissableOutcomeDialogFragment frag = new DismissableOutcomeDialogFragment();
    Bundle args = new Bundle();
    args.putString(TITLE_KEY, title);
    args.putString(MESSAGE_KEY, message);
    args.putBoolean(IS_OK_KEY, isOk);
    args.putString(HANDLER_TAG_KEY, handlerTag);
    frag.setArguments(args);
    return frag;
  }


  private String appName;
  private boolean ok_invoked;
  private boolean dismissCalled;

  /**
   * Override the Fragment.onAttach() method to get appName and initailize variables
   *
   * @param context
   */
  @Override public void onAttach(Context context) {
    super.onAttach(context);

    dismissCalled = false;
    ok_invoked = false;

    Activity activity = getActivity();

    // get the appName from the ODK app aware infrastructure
    if (activity instanceof IAppAwareActivity) {
      appName = ((IAppAwareActivity) activity).getAppName();
    } else {
      throw new RuntimeException("The activity that ProgressDialogListener is attaching to is "
          + "NOT an IAppAwareActivity");
    }
  }

  public boolean dismissWasCalled() {
    return dismissCalled;
  }


  public void setMessage(String message) {
    ((AlertDialog) this.getDialog()).setMessage(message);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String title = getArguments().getString(TITLE_KEY);
    String message = getArguments().getString(MESSAGE_KEY);

    ok_invoked = savedInstanceState != null && savedInstanceState.getBoolean(OK_INVOKED_KEY);

    DialogInterface.OnClickListener okButtonListener = new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        ok_invoked = true;
        cancelOutcomeDialog();
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(title).setMessage(message).setIcon(R.drawable.ic_info_outline_black_24dp)
        .setCancelable(false).setPositiveButton(R.string.ok, okButtonListener);

    AlertDialog dialog = builder.create();
    dialog.setCanceledOnTouchOutside(false);

    return dialog;
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(OK_INVOKED_KEY, ok_invoked);
  }

  @Override public void onDismiss(DialogInterface dialog) {
    dismissCalled = true;
    super.onDismiss(dialog);
    if ( !ok_invoked ) {
      Activity a = getActivity();
      if ( a != null ) {
        a.setResult(Activity.RESULT_CANCELED);
        a.finish();
      }
    }
  }


  private void cancelOutcomeDialog() {
    ok_invoked = true;
    this.getDialog().dismiss();
    if ( this.getArguments().getBoolean(IS_OK_KEY) ) {
      this.getActivity().setResult(Activity.RESULT_OK);
    } else {
      this.getActivity().setResult(Activity.RESULT_CANCELED);
    }

    String handlerTag = getArguments().getString(HANDLER_TAG_KEY);
    // Notify the syncFragment that this sync has completed
    // so as to release the details of the sync within the service.
    Fragment fragment = getFragmentManager().findFragmentByTag(handlerTag);
    if (fragment != null) {
      ISyncOutcomeHandler syncHandler = (ISyncOutcomeHandler) fragment;
      syncHandler.onSyncCompleted();
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////
  /////////////////   STATIC HELPER FUNCTIONS TO AVOID DUPLICATE CODE   /////////////////
  ///////////////////////////////////////////////////////////////////////////////////////

  public static DismissableOutcomeDialogFragment eitherReuseOrCreateNew(String dialogTag,
      DismissableOutcomeDialogFragment dialogFragment, FragmentManager fragmentManager, String title, String message, boolean isOk, String handlerTag) {

    Log.e(t, "WRB: in eitherReuseOrCreateNew");

    if (fragmentManager == null) {
      throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
    }

    Fragment dialog = fragmentManager.findFragmentByTag(dialogTag);

    if (dialog != null && (dialog instanceof AlertDialogFragment)) {
      dialogFragment = (DismissableOutcomeDialogFragment) dialog;
      if (dialogFragment.dismissWasCalled()) {
        dialogFragment = null;
      }
    } else {

      // failed to find the alert dialog, dimiss the dangling reference if dismiss
      // wasn't previously called
      if (dialogFragment != null && !dialogFragment.dismissWasCalled()) {
        dialogFragment.dismiss();
      }
      dialogFragment = null;
    }

    // if no-prexising fragment create one, update the message
    if (dialogFragment == null) {
      dialogFragment = DismissableOutcomeDialogFragment.newInstance(title, message, isOk,
          handlerTag);
    } else {
      dialogFragment.getDialog().setTitle(title);
      dialogFragment.setMessage(message);
    }

    return dialogFragment;
  }

  public static void dismissDialogs(String dialogTag,
      DismissableOutcomeDialogFragment dialogFragment, FragmentManager fragmentManager) {

    if (fragmentManager == null) {
      throw new IllegalArgumentException(FRAGMENT_MANAGER_NULL_ERROR);
    }

    // dismiss the provided reference reference
    if (dialogFragment != null && !dialogFragment.dismissWasCalled()) {
      dialogFragment.dismiss();
    }

    // then try to find any dangling reference that has not been dismissed
    Fragment dialog = fragmentManager.findFragmentByTag(dialogTag);
    if (dialog != null && (dialog instanceof DismissableOutcomeDialogFragment)) {
      DismissableOutcomeDialogFragment tmp = (DismissableOutcomeDialogFragment) dialog;
      if(!tmp.dismissWasCalled()) {
        tmp.dismiss();
      }
    }
  }
}

