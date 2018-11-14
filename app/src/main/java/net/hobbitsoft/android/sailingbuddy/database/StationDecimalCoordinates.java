package net.hobbitsoft.android.sailingbuddy.database;

import androidx.room.ColumnInfo;

public class StationDecimalCoordinates {

    @ColumnInfo(name = "station_id")
    String stationId;

    @ColumnInfo(name = "decimal_longitude")
    double longitude;

    @ColumnInfo(name = "decimal_latitude")
    double latitude;

}
