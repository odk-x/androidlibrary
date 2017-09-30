package org.opendatakit.database.queries;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jbeorse on 9/26/17.
 */

public class SingleRowQuery extends SimpleQuery {

    /**
     * The ID of the single row
     */
    private final String mRowId;

    /**
     * Construct the query
     *
     * @param tableId The table to query
     * @param rowId The row to choose
     * @param bindArgs The sql selection args
     * @param whereClause The sql where clause
     * @param groupByArgs The sql group by arguments
     * @param havingClause The sql having clause
     * @param orderByColNames The columns to order by
     * @param orderByDirections The directions to order by
     * @param limit The maximum number of rows to return
     * @param offset The offset to start counting the limit from
     */
    public SingleRowQuery(String tableId, String rowId, BindArgs bindArgs, String whereClause,
                       String[] groupByArgs, String havingClause, String[] orderByColNames,
                       String[] orderByDirections, Integer limit, Integer offset) {
        super(tableId, bindArgs, whereClause, groupByArgs, havingClause, orderByColNames,
                orderByDirections, limit, offset);
        this.mRowId = rowId;
    }

    public SingleRowQuery(String tableId, String rowId, BindArgs bindArgs, String whereClause,
                          String[] groupByArgs, String havingClause, String[] orderByColNames,
                          String[] orderByDirections, QueryBounds bounds) {
        super(tableId, bindArgs, whereClause, groupByArgs, havingClause, orderByColNames,
                orderByDirections, bounds);
        this.mRowId = rowId;
    }

    public SingleRowQuery(Parcel in) {
        super(in);

        this.mRowId = readStringFromParcel(in);
    }

    /**
     * Get id of the single row
     *
     * @return the id of the desired row
     */
    public String getRowId() {
        return mRowId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        writeStringToParcel(dest, mRowId);
    }

    public static final Parcelable.Creator<SingleRowQuery> CREATOR =
            new Parcelable.Creator<SingleRowQuery>() {
                public SingleRowQuery createFromParcel(Parcel in) {
                    return new SingleRowQuery(in);
                }

                public SingleRowQuery[] newArray(int size) {
                    return new SingleRowQuery[size];
                }
            };
}
