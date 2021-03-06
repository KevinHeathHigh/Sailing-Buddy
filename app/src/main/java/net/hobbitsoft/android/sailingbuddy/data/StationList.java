package net.hobbitsoft.android.sailingbuddy.data;

import android.os.Parcel;
import android.os.Parcelable;

import net.hobbitsoft.android.sailingbuddy.database.StationTable;
import net.hobbitsoft.android.sailingbuddy.utilities.ConversionUtils;

import java.io.Serializable;

public class StationList implements Comparable<StationList>, Serializable, Parcelable {

    private String mStationId;
    private StringCoordinates mStringCoordinates;
    private String mStationName;
    private double mDistance;
    private boolean mIsFavorite;

    public StationList(String stationId, StringCoordinates stringCoordinates, String stationName, double distance, boolean isFavorite) {
        this.mStationId = stationId;
        this.mStringCoordinates = stringCoordinates;
        this.mStationName = stationName;
        this.mDistance = distance;
        this.mIsFavorite = isFavorite;
    }

    public StationList(String stationId, double distance) {
        this.mStationId = stationId;
        this.mDistance = distance;
    }

    public StationList(String stationId) {
        this.mStationId = stationId;
    }

    public StationList(StationTable stationTable) {
        this.mStationId = stationTable.getStationId();
        this.mStationName = stationTable.getName();
        this.mStringCoordinates = new StringCoordinates(stationTable.getStringLatitude(), stationTable.getStringLongitude());
    }

    public StationList() {
    }

    protected StationList(Parcel in) {
        mStationId = in.readString();
        mStationName = in.readString();
        mDistance = in.readDouble();
        mIsFavorite = in.readByte() != 0;
        mStringCoordinates = (StringCoordinates) in.readSerializable();
    }

    public static final Creator<StationList> CREATOR = new Creator<StationList>() {
        @Override
        public StationList createFromParcel(Parcel in) {
            return new StationList(in);
        }

        @Override
        public StationList[] newArray(int size) {
            return new StationList[size];
        }
    };

    // this.mDistance as the first parameter causes the sorting to be ascending
    @Override
    public int compareTo(StationList distanceSort) {
        return Double.compare(this.getDistance(), distanceSort.getDistance());
    }

    public String getStationId() {
        return mStationId;
    }

    public void setStationId(String stationId) {
        this.mStationId = stationId;
    }

    public StringCoordinates getStringCoordinates() {
        return mStringCoordinates;
    }

    public void setStringCoordinates(StringCoordinates stringCoordinates) {
        this.mStringCoordinates = stringCoordinates;
    }

    public String getStationName() {
        return mStationName;
    }

    public void setStationName(String stationName) {
        this.mStationName = stationName;
    }

    public double getDistance() {
        return mDistance;
    }

    public String getDistanceString() {
        return ConversionUtils.getDoubleAsRoundedString(ConversionUtils.getMilesFromMeters(mDistance)) + " miles";
    }

    public void setDistance(double distance) {
        this.mDistance = distance;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean favorite) {
        mIsFavorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mStationId);
        dest.writeString(mStationName);
        dest.writeDouble(mDistance);
        dest.writeValue(mIsFavorite);
        dest.writeSerializable(mStringCoordinates);
    }
}
