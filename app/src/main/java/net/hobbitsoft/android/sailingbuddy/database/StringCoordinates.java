package net.hobbitsoft.android.sailingbuddy.database;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

public class StringCoordinates {

    @ColumnInfo(name = "string_longitude")
    private String longitude;

    @ColumnInfo(name = "string_latitude")
    private String latitude;

    @Ignore
    public StringCoordinates() {
    }

    public StringCoordinates(String longitude, String latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
}
