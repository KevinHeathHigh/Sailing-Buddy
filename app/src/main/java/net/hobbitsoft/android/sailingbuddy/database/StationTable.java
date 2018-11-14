package net.hobbitsoft.android.sailingbuddy.database;

import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.utilities.CoordinateUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

/**
 *
 */
@Entity(tableName = "station_table", primaryKeys = "station_id")
public class StationTable implements Comparable<StationTable> {

    @NonNull
    @ColumnInfo(name = "station_id")
    private String stationId;
    @ColumnInfo(name = "owner")
    private String owner;
    @ColumnInfo(name = "ttype")
    private String ttype;
    @ColumnInfo(name = "hull")
    private String hull;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "payload")
    private String payload;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "string_longitude")
    private String stringLongitude;
    @ColumnInfo(name = "string_latitude")
    private String stringLatitude;
    @ColumnInfo(name = "decimal_longitude")
    private double decimalLongitude;
    @ColumnInfo(name = "decimal_latitude")
    private double decimalLatitude;
    @ColumnInfo(name = "time_zone")
    private String timezone;
    @ColumnInfo(name = "forecast")
    private String forecast;
    @ColumnInfo(name = "note")
    private String note;
    @Ignore
    private DecimalCoordinates mCurrentCoordinates;

    public StationTable(@NonNull String stationId, String owner, String ttype, String hull, String name,
                        String payload, String location, String stringLongitude, String stringLatitude,
                        double decimalLongitude, double decimalLatitude, String timezone,
                        String forecast, String note) {
        this.stationId = stationId;
        this.owner = owner;
        this.ttype = ttype;
        this.hull = hull;
        this.name = name;
        this.payload = payload;
        this.location = location;
        this.stringLatitude = stringLatitude;
        this.stringLongitude = stringLongitude;
        this.decimalLatitude = decimalLatitude;
        this.decimalLongitude = decimalLongitude;
        this.timezone = timezone;
        this.forecast = forecast;
        this.note = note;
    }

    /*
     * @param stationRow as '|' deliminated string
     *  example: 41017|N|3-meter discus buoy|||DACT payload|35.400 N 75.100 W (35&#176;23'60" N 75&#176;5'60" W)|E| |
     */
    @Ignore
    public StationTable(String stationRow) {
        String[] stationItems = stationRow.trim().split("\\|");
        this.stationId = stationItems[0].trim();
        this.owner = stationItems[1].trim();
        this.ttype = stationItems[2].trim();
        this.hull = stationItems[3].trim();
        this.name = stationItems[4].trim();
        this.payload = stationItems[5].trim();
        this.location = stationItems[6].trim();
        StringCoordinates stringCoordinates = CoordinateUtils.resolveStringCoordinates(this.location);
        this.stringLatitude = stringCoordinates.getLatitude();
        this.stringLongitude = stringCoordinates.getLongitude();
        DecimalCoordinates decimalCoordinates = CoordinateUtils.resolveDecimalCoordinates(this.location);
        this.decimalLatitude = decimalCoordinates.getLatitude();
        this.decimalLongitude = decimalCoordinates.getLongitude();
        this.timezone = stationItems[7].trim();
        this.forecast = stationItems[8].trim();
        if (stationItems.length == 10) {
            this.note = stationItems[9].trim();
        }
    }

    @Ignore
    public StationTable() {
    }

    // this.distance as the first parameter causes the sorting to be ascending
    @Override
    public int compareTo(StationTable stationTable) {
        return Double.compare(this.getDistance(), stationTable.getDistance());
    }

    @NonNull
    public String getStationId() {
        return stationId;
    }

    public void setStationId(@NonNull String stationId) {
        this.stationId = stationId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTtype() {
        return ttype;
    }

    public void setTtype(String ttpye) {
        this.ttype = ttype;
    }

    public String getHull() {
        return hull;
    }

    public void setHull(String hull) {
        this.hull = hull;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStringLongitude() {
        return stringLongitude;
    }

    public void setStringLongitude(String stringLongitude) {
        this.stringLongitude = stringLongitude;
    }

    public String getStringLatitude() {
        return stringLatitude;
    }

    public void setStringLatitude(String stringLatitude) {
        this.stringLatitude = stringLatitude;
    }

    public double getDecimalLongitude() {
        return decimalLongitude;
    }

    public DecimalCoordinates getDecimalCoordinates() {
        return new DecimalCoordinates(this.decimalLatitude, this.decimalLongitude);
    }

    public void setDecimalLongitude(double decimalLongitude) {
        this.decimalLongitude = decimalLongitude;
    }

    public double getDecimalLatitude() {
        return decimalLatitude;
    }

    public void setDecimalLatitude(double decimalLatitude) {
        this.decimalLatitude = decimalLatitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getForecast() {
        return forecast;
    }

    public void setForecast(String forecast) {
        this.forecast = forecast;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getDistance() {
        return CoordinateUtils.getDistance(this.mCurrentCoordinates.getLatLng(),
                this.getDecimalCoordinates().getLatLng());
    }

    public void setCurrentCoordinates(DecimalCoordinates currentCoordinates) {
        this.mCurrentCoordinates = currentCoordinates;
    }
}


