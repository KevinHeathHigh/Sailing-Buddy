package net.hobbitsoft.android.sailingbuddy.database;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "forecast", primaryKeys = "forecast_station")
public class Forecast implements Serializable {

    @NonNull
    @ColumnInfo(name = "forecast_station")
    private String mForecastStation;

    @ColumnInfo(name = "forecast")
    private String mForecast;

    public Forecast(String forecastStation, String forecast) {
        this.mForecastStation = forecastStation;
        this.mForecast = forecast;
    }

    @Ignore
    public Forecast() {
    }

    @Ignore
    public Forecast(String forecastStation) {
        this.mForecastStation = forecastStation;
    }

    public String getForecastStation() {
        return mForecastStation;
    }

    public void setForecastStation(String forecastStation) {
        this.mForecastStation = forecastStation;
    }

    public String getForecast() {
        return mForecast;
    }

    public void setForecast(String forecast) {
        this.mForecast = forecast;
    }
}
