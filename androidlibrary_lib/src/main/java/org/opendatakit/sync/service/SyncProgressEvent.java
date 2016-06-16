package org.opendatakit.sync.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mitchellsundt@gmail.com
 */
public class SyncProgressEvent implements Parcelable {
  public final int messageNum;
  public final String progressMessageText;
  public final SyncProgressState progressState;
  public final int curProgressBar;
  public final int maxProgressBar;

  protected SyncProgressEvent(int messageNum, String progressMessageText,
      SyncProgressState progressState, int curProgressBar, int maxProgressBar) {
    this.messageNum = messageNum;
    this.progressMessageText = progressMessageText;
    this.progressState = progressState;
    this.curProgressBar = curProgressBar;
    this.maxProgressBar = maxProgressBar;
  }

  protected SyncProgressEvent(Parcel in) {
    messageNum = in.readInt();
    progressMessageText = in.readString();
    progressState = in.readParcelable(SyncProgressState.class.getClassLoader());
    curProgressBar = in.readInt();
    maxProgressBar = in.readInt();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(messageNum);
    dest.writeString(progressMessageText);
    dest.writeParcelable(progressState, flags);
    dest.writeInt(curProgressBar);
    dest.writeInt(maxProgressBar);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<SyncProgressEvent> CREATOR = new Creator<SyncProgressEvent>() {
    @Override public SyncProgressEvent createFromParcel(Parcel in) {
      return new SyncProgressEvent(in);
    }

    @Override public SyncProgressEvent[] newArray(int size) {
      return new SyncProgressEvent[size];
    }
  };
}
