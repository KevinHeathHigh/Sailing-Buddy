package net.hobbitsoft.android.sailingbuddy.database;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ForecastDAO {
    @Query("SELECT * FROM forecast WHERE forecast_station = :forecastStation")
    Forecast getForecastByForecastStation(String forecastStation);

    @Query("SELECT * FROM forecast WHERE forecast_station = :forecastStation")
    LiveData<Forecast> getLiveForecastByForecastStation(String forecastStation);

    @Query("SELECT EXISTS(SELECT 1 FROM forecast WHERE forecast_station = :forecastStation)")
    boolean forecastCached(String forecastStation);

    @Insert()
    void addForecastToCache(Forecast forecast);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateForecastInCache(Forecast forecast);

    @Delete
    void deleteForecastFromCache(StationDetails stationDetails);
}
