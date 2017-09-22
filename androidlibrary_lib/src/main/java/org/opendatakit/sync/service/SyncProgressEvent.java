package org.opendatakit.sync.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mitchellsundt@gmail.com
 */
public class SyncProgressEvent implements Parcelable {
  public static final Creator<SyncProgressEvent> CREATOR = new Creator<SyncProgressEvent>() {
    @Override
    public SyncProgressEvent createFromParcel(Parcel in) {
      return new SyncProgressEvent(in);
    }

    @Override
    public SyncProgressEvent[] newArray(int size) {
      return new SyncProgressEvent[size];
    }
  };
  /**
   * These four are used in SyncFragment, VerifyServerSettingsFragment, maybe more
   */
  @SuppressWarnings("WeakerAccess")
  public final String progressMessageText;
  @SuppressWarnings("WeakerAccess")
  public final SyncProgressState progressState;
  @SuppressWarnings("WeakerAccess")
  public final int curProgressBar;
  @SuppressWarnings("WeakerAccess")
  public final int maxProgressBar;

  public SyncProgressEvent(String progressMessageText, SyncProgressState progressState,
      int curProgressBar, int maxProgressBar) {
    this.progressMessageText = progressMessageText;
    this.progressState = progressState;
    this.curProgressBar = curProgressBar;
    this.maxProgressBar = maxProgressBar;
  }

  protected SyncProgressEvent(Parcel in) {
    progressMessageText = in.readString();
    progressState = in.readParcelable(SyncProgressState.class.getClassLoader());
    curProgressBar = in.readInt();
    maxProgressBar = in.readInt();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(progressMessageText);
    dest.writeParcelable(progressState, flags);
    dest.writeInt(curProgressBar);
    dest.writeInt(maxProgressBar);
  }

  @Override
  public int describeContents() {
    return 0;
  }
}
