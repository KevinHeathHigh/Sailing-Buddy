/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy.database;

import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.data.StringCoordinates;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface StationTableDAO {

    @Query("SELECT * FROM station_table")
    List<StationTable> getAllStations();

    @Query("SELECT * FROM station_table WHERE station_id = :stationId")
    StationTable getStationByID(String stationId);

    @Query("SELECT * FROM station_table WHERE station_id LIKE :station_id_fragment")
    List<StationTable> getStationsLike(String station_id_fragment);

    @Query("SELECT EXISTS (SELECT 1 FROM station_table WHERE station_id = :stationId)")
    boolean stationInDatabase(String stationId);

    @Query("SELECT decimal_longitude, decimal_latitude FROM station_table where station_id = :stationId")
    LiveData<DecimalCoordinates> getLiveDecimalCoordinatesFromStation(String stationId);

    @Query("SELECT decimal_longitude, decimal_latitude FROM station_table where station_id = :stationId")
    DecimalCoordinates getDecimalCoordinatesFromStation(String stationId);

    @Query("SELECT string_longitude, string_latitude FROM station_table where station_id = :stationId")
    LiveData<StringCoordinates> getLiveStringCoordinatesFromStation(String stationId);

    @Query("SELECT string_longitude, string_latitude FROM station_table where station_id = :stationId")
    StringCoordinates getStringCoordinatesFromStation(String stationId);

    @Query("SELECT station_id, decimal_longitude, decimal_latitude FROM station_table")
    List<StationDecimalCoordinates> getAllCoordinates();

    @Query("SELECT name FROM station_table WHERE station_id = :stationId")
    LiveData<String> getLiveStationNameByStationId(String stationId);

    @Query("SELECT name FROM station_table WHERE station_id = :stationId")
    String getStationNameByStationId(String stationId);

    @Insert
    void insertStation(StationTable stationTable);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateStation(StationTable stationTable);

    @Delete
    void deleteStation(StationTable stationTable);

}
