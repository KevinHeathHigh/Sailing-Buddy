package net.hobbitsoft.android.sailingbuddy.database;

import net.hobbitsoft.android.sailingbuddy.data.Moon;
import net.hobbitsoft.android.sailingbuddy.data.StringCoordinates;
import net.hobbitsoft.android.sailingbuddy.data.Sun;
import net.hobbitsoft.android.sailingbuddy.data.Tide;
import net.hobbitsoft.android.sailingbuddy.data.Wave;
import net.hobbitsoft.android.sailingbuddy.data.Wind;
import net.hobbitsoft.android.sailingbuddy.utilities.CoordinateUtils;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "station_cache", primaryKeys = "station_id")
public class StationDetails {

    @NonNull
    @ColumnInfo(name = "station_id")
    private String mStationId;
    @ColumnInfo(name = "station_name")
    private String mStationName;
    @ColumnInfo(name = "string_coordinates")
    private StringCoordinates stringCoordinates;
    @ColumnInfo(name = "last_update_time")
    private Date lastUpdateTime;
    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;
    @ColumnInfo(name = "wind")
    private Wind wind;
    @ColumnInfo(name = "current_temperature")
    private double currentTemperature;
    @ColumnInfo(name = "high_temperature")
    private double highTemperature;
    @ColumnInfo(name = "low_temperature")
    private double lowTemperature;
    @ColumnInfo(name = "first_tide")
    private Tide firstTide;
    @ColumnInfo(name = "second_tide")
    private Tide secondTide;
    @ColumnInfo(name = "third_tide")
    private Tide thirdTide;
    @ColumnInfo(name = "fourth_tide")
    private Tide fourthTide;
    @ColumnInfo(name = "sun_data")
    private Sun sun;
    @ColumnInfo(name = "moon_data")
    private Moon moon;
    @ColumnInfo(name = "air_pressure")
    private double airPressure;
    @ColumnInfo(name = "air_pressure_status")
    private String airPressureStatus;
    @ColumnInfo(name = "water_temperature")
    private double waterTemperature;
    @ColumnInfo(name = "dew_point")
    private double dewPoint;
    @ColumnInfo(name = "seas")
    private Wave seas;
    @ColumnInfo(name = "visibility")
    private double visibility;
    @ColumnInfo(name = "save_summary_last_update")
    private Date waveSummaryLastUpdate;
    @ColumnInfo(name = "swell")
    private Wave swell;
    @ColumnInfo(name = "wind_wave")
    private Wave windWave;
    @ColumnInfo(name = "owner")
    private String owner;
    @ColumnInfo(name = "owner_country")
    private String ownerCountry;
    @ColumnInfo(name = "time_zone")
    private String timeZone;
    @ColumnInfo(name = "forecast_station")
    private String mForecastStation;
    @ColumnInfo(name = "notes")
    private String notes;

    public StationDetails(@NonNull String stationId, String stationName, StringCoordinates stringCoordinates, Date lastUpdateTime, boolean isFavorite, Wind wind, double currentTemperature, double highTemperature, double lowTemperature, Tide firstTide, Tide secondTide, Tide thirdTide, Tide fourthTide, Sun sun, Moon moon, double airPressure, String airPressureStatus, double waterTemperature, double dewPoint, Wave seas, double visibility, Date waveSummaryLastUpdate, Wave swell, Wave windWave, String owner, String ownerCountry, String timeZone, String forecastStation, String notes) {
        this.mStationId = stationId;
        this.mStationName = stationName;
        this.stringCoordinates = stringCoordinates;
        this.lastUpdateTime = lastUpdateTime;
        this.isFavorite = isFavorite;
        this.wind = wind;
        this.currentTemperature = currentTemperature;
        this.highTemperature = highTemperature;
        this.lowTemperature = lowTemperature;
        this.firstTide = firstTide;
        this.secondTide = secondTide;
        this.thirdTide = thirdTide;
        this.fourthTide = fourthTide;
        this.sun = sun;
        this.moon = moon;
        this.airPressure = airPressure;
        this.airPressureStatus = airPressureStatus;
        this.waterTemperature = waterTemperature;
        this.dewPoint = dewPoint;
        this.seas = seas;
        this.visibility = visibility;
        this.waveSummaryLastUpdate = waveSummaryLastUpdate;
        this.swell = swell;
        this.windWave = windWave;
        this.owner = owner;
        this.ownerCountry = ownerCountry;
        this.timeZone = timeZone;
        this.mForecastStation = forecastStation;
        this.notes = notes;
    }

    @Ignore
    public StationDetails() {
    }

    @Ignore
    public StationDetails(StationTable stationTable) {
        this.mStationId = stationTable.getStationId();
        this.mStationName = stationTable.getName();
        this.notes = stationTable.getNote();
        this.mForecastStation = stationTable.getForecast();
        this.stringCoordinates = new StringCoordinates(stationTable.getStringLatitude(), stationTable.getStringLongitude());
        this.timeZone = stationTable.getTimezone();
    }

    public void updateStationOwner(StationOwner stationOwner) {
        this.owner = stationOwner.getOwnerName();
        this.ownerCountry = stationOwner.getCountryCode();
    }

    @NonNull
    public String getStationId() {
        return mStationId;
    }

    public void setStationId(@NonNull String stationId) {
        this.mStationId = stationId;
    }

    public String getStationName() {
        return mStationName;
    }

    public void setStationName(String stationName) {
        this.mStationName = stationName;
    }

    public StringCoordinates getStringCoordinates() {
        return stringCoordinates;
    }

    public void setStringCoordinates(StringCoordinates stringCoordinates) {
        this.stringCoordinates = stringCoordinates;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }


    public String getCurrentTemperatureString() {
        return String.valueOf(getCurrentTemperature()) + CoordinateUtils.DEGREE_SYMBOL + " F";
    }

    public void setCurrentTemperature(double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public double getHighTemperature() {
        return highTemperature;
    }

    public String getHighTemperatureString() {
        return String.valueOf(getHighTemperature()) + CoordinateUtils.DEGREE_SYMBOL + " F";
    }

    public void setHighTemperature(double highTemperature) {
        this.highTemperature = highTemperature;
    }

    public double getLowTemperature() {
        return lowTemperature;
    }

    public String getLowTemperatureString() {
        return String.valueOf(getLowTemperature()) + CoordinateUtils.DEGREE_SYMBOL + " F";
    }

    public void setLowTemperature(double lowTemperature) {
        this.lowTemperature = lowTemperature;
    }

    public Tide getFirstTide() {
        return firstTide;
    }

    public void setFirstTide(Tide firstTide) {
        this.firstTide = firstTide;
    }

    public Tide getSecondTide() {
        return secondTide;
    }

    public void setSecondTide(Tide secondTide) {
        this.secondTide = secondTide;
    }

    public Tide getThirdTide() {
        return thirdTide;
    }

    public void setThirdTide(Tide thirdTide) {
        this.thirdTide = thirdTide;
    }

    public Tide getFourthTide() {
        return fourthTide;
    }

    public void setFourthTide(Tide fourthTide) {
        this.fourthTide = fourthTide;
    }

    public Sun getSun() {
        return sun;
    }

    public void setSun(Sun sun) {
        this.sun = sun;
    }

    public Moon getMoon() {
        return moon;
    }

    public void setMoon(Moon moon) {
        this.moon = moon;
    }

    public double getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(double airPressure) {
        this.airPressure = airPressure;
    }

    public String getAirPressureStatus() {
        return airPressureStatus;
    }

    public void setAirPressureStatus(String airPressureStatus) {
        this.airPressureStatus = airPressureStatus;
    }

    public double getWaterTemperature() {
        return waterTemperature;
    }

    public void setWaterTemperature(double waterTemperature) {
        this.waterTemperature = waterTemperature;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public Wave getSeas() {
        return seas;
    }

    public void setSeas(Wave seas) {
        this.seas = seas;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public Date getWaveSummaryLastUpdate() {
        return waveSummaryLastUpdate;
    }

    public void setWaveSummaryLastUpdate(Date waveSummaryLastUpdate) {
        this.waveSummaryLastUpdate = waveSummaryLastUpdate;
    }

    public Wave getSwell() {
        return swell;
    }

    public void setSwell(Wave swell) {
        this.swell = swell;
    }

    public Wave getWindWave() {
        return windWave;
    }

    public void setWindWave(Wave windWave) {
        this.windWave = windWave;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerCountry() {
        return ownerCountry;
    }

    public void setOwnerCountry(String ownerCountry) {
        this.ownerCountry = ownerCountry;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getForecastStation() {
        return mForecastStation;
    }

    public void setForecastStation(String forecastStation) {
        this.mForecastStation = getForecastStation();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
