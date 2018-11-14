package net.hobbitsoft.android.sailingbuddy.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "station_owners", primaryKeys = "owner_code")
public class StationOwner {

    @NonNull
    @ColumnInfo(name = "owner_code")
    private String ownerCode;
    @ColumnInfo(name = "owner_name")
    private String ownerName;
    @ColumnInfo(name = "country_code")
    private String countryCode;

    public StationOwner(@NonNull String ownerCode, String ownerName, String countryCode) {
        this.ownerCode = ownerCode;
        this.ownerName = ownerName;
        this.countryCode = countryCode;
    }

    /**
     * @param stationOwnerRow as '|' deliminated string
     *                        AC |U.S. Army Corps of Engineers|US
     */
    @Ignore
    public StationOwner(String stationOwnerRow) {
        String[] stationOwnerItems = stationOwnerRow.trim().split("\\|");
        this.ownerCode = stationOwnerItems[0].trim();
        this.ownerName = stationOwnerItems[1].trim();
        this.countryCode = stationOwnerItems[2].trim();
    }

    @NonNull
    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(@NonNull String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
