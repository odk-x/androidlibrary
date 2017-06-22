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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import org.opendatakit.androidlibrary.R;

/**
 * Fragment-version of Progress dialog, used all over the place
 *
 * @author mitchellsundt@gmail.com
 */
@SuppressWarnings("unused")
public class ProgressDialogFragment extends DialogFragment {

  /**
   * Returns a new ProgressDialogFragment with the given title and message
   *
   * @param title   the title to be displayed in the dialog
   * @param message the message to be displayed in the dialog
   * @return a ProgressDialogFragment with the cirrect arguments set
   */
  public static ProgressDialogFragment newInstance(String title, String message) {
    ProgressDialogFragment frag = new ProgressDialogFragment();
    Bundle args = new Bundle();
    args.putString("title", title);
    args.putString("message", message);
    frag.setArguments(args);
    return frag;
  }

  /**
   * Updates the message on the dialog
   * @param message the new mssage
   */
  public void setMessage(String message) {
    ((ProgressDialog) this.getDialog()).setMessage(message);
  }

  /**
   * Called from onCreate when a dialog object needs to be created
   * @param savedInstanceState unused
   * @return a Dialog object with the correct message and title
   */
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String title = getArguments().getString("title");
    String message = getArguments().getString("message");

    DialogInterface.OnClickListener loadingButtonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Fragment f = ProgressDialogFragment.this;

        if (f instanceof CancelProgressDialog) {
          // user code should dismiss the dialog
          // since this is a cancellation action...
          // dialog.dismiss();
          ((CancelProgressDialog) f).cancelProgressDialog();
        }
      }
    };
    DialogInterface.OnShowListener showButtonListener = new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface dialog) {
        Fragment f = ProgressDialogFragment.this;

        // If the client code wants to get an event when the user dismisses the dialog, allow the
        // user to dismiss the dialog by showing them the button. Otherwise, the client code will
        // dismiss the dialog, so don't let the user do it and hide the button
        if (f instanceof CancelProgressDialog) {
          ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
              .setVisibility(View.VISIBLE);
        } else {
          ((ProgressDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
              .setVisibility(View.GONE);
        }

      }
    };

    ProgressDialog mProgressDialog = new ProgressDialog(getActivity(), getTheme());
    mProgressDialog.setTitle(title);
    mProgressDialog.setMessage(message);
    mProgressDialog.setIcon(R.drawable.ic_info_outline_black_24dp);
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setCancelable(false);
    mProgressDialog.setCanceledOnTouchOutside(false);
    mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.cancel),
        loadingButtonListener);
    mProgressDialog.setOnShowListener(showButtonListener);

    return mProgressDialog;
  }

  /**
   * Implemented by InitializationFragment in survey, scan and tables
   */
  @SuppressWarnings("WeakerAccess")
  public interface CancelProgressDialog {
    void cancelProgressDialog();
  }
}
