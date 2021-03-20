package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.RowOutcome;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ParcelableRowOutcomeList extends RowOutcomeList implements Parcelable {
  public ParcelableRowOutcomeList() {
    super();
  }

  public ParcelableRowOutcomeList(RowOutcomeList rowOutcomeList) {
    setRows(rowOutcomeList.getRows());
    setDataETag(rowOutcomeList.getDataETag());
    setTableUri(rowOutcomeList.getTableUri());
  }

  protected ParcelableRowOutcomeList(Parcel in) {
    super();

    setRows(readRows(in));
    setDataETag(in.readString());
    setTableUri(in.readString());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    writeRows(dest, flags);
    dest.writeString(getDataETag());
    dest.writeString(getTableUri());
  }

  private void writeRows(Parcel dest, int flags) {
    if (getRows() == null) {
      dest.writeInt(-1);
      return;
    }

    dest.writeInt(getRows().size());
    for (RowOutcome rowOutcome : getRows()) {
      ParcelableRowOutcome.writeToParcel(rowOutcome, dest, flags);
    }
  }

  private ArrayList<RowOutcome> readRows(Parcel in) {
    int size = in.readInt();

    if (size < 0) {
      return null;
    }

    ArrayList<RowOutcome> rows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      rows.add(ParcelableRowOutcome.CREATOR.createFromParcel(in));
    }

    return rows;
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
