package org.opendatakit.builder.csvparser.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RowCsvMixin extends Row {
  @Override
  @JsonProperty(TableConstants.ID)
  public abstract String getRowId();

  @Override
  @JsonProperty(TableConstants.ROW_ETAG)
  public abstract String getRowETag();

  @Override
  @JsonIgnore
  public abstract String getDataETagAtModification();

  @Override
  @JsonIgnore
  public abstract boolean isDeleted();

  @Override
  @JsonIgnore
  public abstract String getCreateUser();

  @Override
  @JsonIgnore
  public abstract String getLastUpdateUser();

  @Override
  @JsonUnwrapped
  public abstract RowFilterScope getRowFilterScope();

  @Override
  @JsonProperty(TableConstants.SAVEPOINT_CREATOR)
  public abstract String getSavepointCreator();

  @Override
  @JsonProperty(TableConstants.FORM_ID)
  public abstract String getFormId();

  @Override
  @JsonProperty(TableConstants.LOCALE)
  public abstract String getLocale();

  @Override
  @JsonProperty(TableConstants.SAVEPOINT_TYPE)
  public abstract String getSavepointType();

  @Override
  @JsonProperty(TableConstants.SAVEPOINT_TIMESTAMP)
  public abstract String getSavepointTimestamp();

  @Override
  @JsonProperty(TableConstants.ID)
  public abstract void setRowId(String rowId);

  @Override
  @JsonProperty(TableConstants.ROW_ETAG)
  public abstract void setRowETag(String rowETag);

  @Override
  @JsonIgnore
  public abstract void setDataETagAtModification(String dataETagAtModification);

  @Override
  @JsonIgnore
  public abstract void setDeleted(boolean deleted);

  @Override
  @JsonIgnore
  public abstract void setCreateUser(String createUser);

  @Override
  @JsonIgnore
  public abstract void setLastUpdateUser(String lastUpdateUser);

  @Override
  @JsonUnwrapped
  public abstract void setRowFilterScope(RowFilterScope filterScope);

  @Override
  @JsonProperty(TableConstants.SAVEPOINT_CREATOR)
  public abstract void setSavepointCreator(String savepointCreator);

  @Override
  @JsonProperty(TableConstants.FORM_ID)
  public abstract void setFormId(String formId);

  @Override
  @JsonProperty(TableConstants.LOCALE)
  public abstract void setLocale(String locale);

  @Override
  @JsonProperty(TableConstants.SAVEPOINT_TYPE)
  public abstract void setSavepointType(String savepointType);

  @Override
  @JsonProperty(TableConstants.SAVEPOINT_TIMESTAMP)
  public abstract void setSavepointTimestamp(String savepointTimestamp);
}
