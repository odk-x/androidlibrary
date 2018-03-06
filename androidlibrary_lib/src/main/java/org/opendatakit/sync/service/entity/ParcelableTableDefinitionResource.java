package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTableDefinitionResource extends TableDefinitionResource implements Parcelable {
  protected ParcelableTableDefinitionResource(Parcel in) {
    super(ParcelableTableDefinition.CREATOR.createFromParcel(in));

    setSelfUri(in.readString());
    setTableUri(in.readString());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(((ParcelableTableDefinition) ((TableDefinition) this)), flags);

    dest.writeString(getSelfUri());
    dest.writeString(getTableId());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableTableDefinitionResource> CREATOR = new Creator<ParcelableTableDefinitionResource>() {
    @Override
    public ParcelableTableDefinitionResource createFromParcel(Parcel in) {
      return new ParcelableTableDefinitionResource(in);
    }

    @Override
    public ParcelableTableDefinitionResource[] newArray(int size) {
      return new ParcelableTableDefinitionResource[size];
    }
  };
}
