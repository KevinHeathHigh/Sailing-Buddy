package net.hobbitsoft.android.sailingbuddy.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface StationCacheDAO {

    @Query("SELECT * FROM station_cache WHERE station_id = :stationId")
    LiveData<StationDetails> getStaionFromCache(String stationId);

    @Query("SELECT * FROM station_cache")
    List<StationDetails> getAllCachedStaions();

    @Query("SELECT EXISTS (SELECT 1 FROM station_cache WHERE station_id = :stationId)")
    boolean isStaionCached(String stationId);

    @Insert
    void addStationToCache(StationDetails stationDetails);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateStionInCache(StationDetails stationDetails);

    @Delete
    void delectStaionFromCache(StationDetails stationDetails);


}
