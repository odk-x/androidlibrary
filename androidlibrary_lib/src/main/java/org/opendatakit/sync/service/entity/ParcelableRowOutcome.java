package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;

public class ParcelableRowOutcome extends RowOutcome implements Parcelable {
  public ParcelableRowOutcome(ParcelableRow row, OutcomeType outcomeType) {
    super(row);

    setOutcome(outcomeType);
  }

  protected ParcelableRowOutcome(Parcel in) {
    super(ParcelableRow.CREATOR.createFromParcel(in));

    setSelfUri(in.readString());
    setOutcome(OutcomeType.valueOf(in.readString()));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    writeToParcel(this, dest, flags);
  }

  public static void writeToParcel(RowOutcome rowOutcome, Parcel dest, int flags) {
    ParcelableRow.writeToParcel(rowOutcome, dest, flags);
    dest.writeString(rowOutcome.getSelfUri());
    dest.writeString(rowOutcome.getOutcome().toString());

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
