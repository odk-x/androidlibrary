package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableRowOutcome extends RowOutcome implements Parcelable {
  protected ParcelableRowOutcome(Parcel in) {
    super(ParcelableRow.CREATOR.createFromParcel(in));

    setSelfUri(in.readString());
    setOutcome(((OutcomeType) in.readSerializable()));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    ((ParcelableRow) ((Row) this)).writeToParcel(dest, flags);

    dest.writeString(getSelfUri());
    dest.writeSerializable(getOutcome());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableRowOutcome> CREATOR = new Creator<ParcelableRowOutcome>() {
    @Override
    public ParcelableRowOutcome createFromParcel(Parcel in) {
      return new ParcelableRowOutcome(in);
    }

    @Override
    public ParcelableRowOutcome[] newArray(int size) {
      return new ParcelableRowOutcome[size];
    }
  };
}
