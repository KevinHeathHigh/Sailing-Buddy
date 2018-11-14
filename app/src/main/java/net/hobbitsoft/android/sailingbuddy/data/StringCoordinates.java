package net.hobbitsoft.android.sailingbuddy.data;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

public class StringCoordinates implements Serializable {

    @ColumnInfo(name = "string_latitude")
    private String latitude;

    @ColumnInfo(name = "string_longitude")
    private String longitude;

    public StringCoordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public StringCoordinates() {
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /*
     * Format 48° 2.0' N / 87° 43.8' W
     */
    @NonNull
    @Override
    public String toString() {
        return getLatitude() + " / " + getLongitude();
    }

    public String getStringCoordinates() {
        return this.toString();
    }
}
