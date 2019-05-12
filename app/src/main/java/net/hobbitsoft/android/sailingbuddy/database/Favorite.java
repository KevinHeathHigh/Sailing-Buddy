package net.hobbitsoft.android.sailingbuddy.database;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import static android.os.Build.VERSION_CODES;

@Entity(tableName = "favorites", primaryKeys = "station_id")
public class Favorite implements Parcelable, Comparable<Favorite> {

    @NonNull
    @ColumnInfo(name = "station_id")
    private String mStationId;
    @ColumnInfo(name = "count")
    private int mCount;

    protected Favorite(Parcel in) {
        mStationId = in.readString();
        mCount = in.readInt();
    }

    public static final Creator<Favorite> CREATOR = new Creator<Favorite>() {
        @Override
        public Favorite createFromParcel(Parcel in) {
            return new Favorite(in);
        }

        @Override
        public Favorite[] newArray(int size) {
            return new Favorite[size];
        }
    };

    @Override
    public int compareTo(Favorite favorite) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            return Integer.compare(favorite.getCount(), this.getCount());
        } else {
            //Assumption here --
            return this.getCount() >= favorite.getCount() ? -1 : 0;
        }
    }

    public Favorite(String stationId, int count) {
        this.mStationId = stationId;
        this.mCount = count;
    }

    @Ignore
    public Favorite() {
    }

    @Ignore
    public Favorite(String stationId) {
        this.mStationId = stationId;
        // If this contructor is being used, this this is a new station being added to favorites,
        // So no current visits,
        this.mCount = 0;
    }

    public String getStationId() {
        return mStationId;
    }

    public void setStationId(String stationId) {
        this.mStationId = stationId;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mStationId);
        dest.writeDouble(mCount);
    }
}

