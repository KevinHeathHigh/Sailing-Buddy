package net.hobbitsoft.android.sailingbuddy.utilities;

import android.os.Parcel;
import android.os.Parcelable;

public class DistanceSort implements Parcelable, Comparable<DistanceSort> {

    private String stationId;
    private double distance;

    public DistanceSort(String stationId, double distance) {
        this.stationId = stationId;
        this.distance = distance;
    }

    public DistanceSort() {
    }

    protected DistanceSort(Parcel in) {
        this.stationId = in.readString();
        this.distance = in.readDouble();
    }

    // this.distance as the first parameter causes the sorting to be ascending
    @Override
    public int compareTo(DistanceSort distanceSort) {
        return Double.compare(this.getDistance(), distanceSort.getDistance());
    }


    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stationId);
        dest.writeDouble(distance);
    }

    public static final Creator<DistanceSort> CREATOR = new Creator<DistanceSort>() {
        @Override
        public DistanceSort createFromParcel(Parcel source) {
            return new DistanceSort(source);
        }

        @Override
        public DistanceSort[] newArray(int size) {
            return new DistanceSort[size];
        }
    };
}
