package net.hobbitsoft.android.sailingbuddy.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

public class DecimalCoordinates implements Parcelable {

    @ColumnInfo(name = "decimal_latitude")
    private double latitude;

    @ColumnInfo(name = "decimal_longitude")
    private double longitude;

    @Ignore
    public DecimalCoordinates() {
    }

    public DecimalCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    protected DecimalCoordinates(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public static final Creator<DecimalCoordinates> CREATOR = new Creator<DecimalCoordinates>() {
        @Override
        public DecimalCoordinates createFromParcel(Parcel in) {
            return new DecimalCoordinates(in);
        }

        @Override
        public DecimalCoordinates[] newArray(int size) {
            return new DecimalCoordinates[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);

    }
}
