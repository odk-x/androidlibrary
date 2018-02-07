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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import org.opendatakit.properties.RequestCodes;

/**
 * Alert dialog implemented as a fragment for notifying user of a problem.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class ConfirmationDialogFragment extends DialogFragment {

  private static final String t = ConfirmationDialogFragment.class.getSimpleName();

  private static final String TITLE_KEY = "CDF1_title";
  private static final String MESSAGE_KEY = "CDF1_message";
  private static final String OK_TXT_KEY = "CDF1_OK_TXT";
  private static final String CANCEL_TXT_KEY = "CDF1_CANCEL_TXT";
  private static final String FRAGMENT_ID_KEY = "CDF1_fragmentId";

  public interface ConfirmConfirmationDialog {
    void okConfirmationDialog();

    void cancelConfirmationDialog();
  }

  public static ConfirmationDialogFragment newInstance(int fragmentId, String title,
      String message, String okButton, String cancelButton) {
    ConfirmationDialogFragment frag = new ConfirmationDialogFragment();
    Bundle args = new Bundle();
    args.putInt(FRAGMENT_ID_KEY, fragmentId);
    args.putString(TITLE_KEY, title);
    args.putString(MESSAGE_KEY, message);
    args.putString(OK_TXT_KEY, okButton);
    args.putString(CANCEL_TXT_KEY, cancelButton);
    frag.setArguments(args);
    return frag;
  }

  public void setMessage(String message) {
    ((AlertDialog) this.getDialog()).setMessage(message);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String title = getArguments().getString(TITLE_KEY);
    String message = getArguments().getString(MESSAGE_KEY);
    String okButton = getArguments().getString(OK_TXT_KEY);
    String cancelButton = getArguments().getString(CANCEL_TXT_KEY);

    final Integer fragmentId = getArguments().getInt(FRAGMENT_ID_KEY);
    FragmentManager mgr = getFragmentManager();
    Fragment f = mgr.findFragmentById(fragmentId);
    setTargetFragment(f, RequestCodes.CONFIRMATION_DIALOG.ordinal());

    DialogInterface.OnClickListener dialogYesNoListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int i) {
        FragmentManager mgr = getFragmentManager();
        Fragment f = mgr.findFragmentById(fragmentId);
        switch (i) {
        case DialogInterface.BUTTON_POSITIVE: // delete
          ((ConfirmConfirmationDialog) f).okConfirmationDialog();
          dialog.dismiss();
          break;
        case DialogInterface.BUTTON_NEGATIVE: // do nothing
          ((ConfirmConfirmationDialog) f).cancelConfirmationDialog();
          dialog.dismiss();
          break;
        }
      }
    };

    AlertDialog dlg = new AlertDialog.Builder(getActivity())
        .setIcon(android.R.drawable.ic_dialog_info).setTitle(title).setMessage(message)
        .setCancelable(false).setPositiveButton(okButton, dialogYesNoListener)
        .setNegativeButton(cancelButton, dialogYesNoListener).create();
    dlg.setCanceledOnTouchOutside(false);
    return dlg;
  }
}
