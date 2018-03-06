package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ParcelableRowOutcomeList extends RowOutcomeList implements Parcelable {
  protected ParcelableRowOutcomeList(Parcel in) {
    super(
        in.readArrayList(ParcelableRowOutcome.class.getClassLoader()),
        in.readString()
    );

    setTableUri(in.readString());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    List<ParcelableRowOutcome> pRowOutcomeList = new ArrayList<>();
    for (RowOutcome rowOutcome : getRows()) {
      pRowOutcomeList.add(((ParcelableRowOutcome) rowOutcome));
    }
    dest.writeTypedList(pRowOutcomeList);

    dest.writeString(getDataETag());
    dest.writeString(getTableUri());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableRowOutcomeList> CREATOR = new Creator<ParcelableRowOutcomeList>() {
    @Override
    public ParcelableRowOutcomeList createFromParcel(Parcel in) {
      return new ParcelableRowOutcomeList(in);
    }

    @Override
    public ParcelableRowOutcomeList[] newArray(int size) {
      return new ParcelableRowOutcomeList[size];
    }
  };
}
