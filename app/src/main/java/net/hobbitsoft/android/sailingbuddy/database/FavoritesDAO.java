/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

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
public interface FavoritesDAO {

    @Query("SELECT  * FROM favorites")
    List<Favorite> getAllFavorites();

    @Query("SELECT * FROM favorites ORDER BY count DESC")
    LiveData<List<Favorite>> getAllFavoritesSortedByCount();

    @Query("SELECT * FROM favorites WHERE station_id = :stationId")
    Favorite getFavoriteByStation(String stationId);

    @Query("SELECT count FROM favorites WHERE station_id = :stationId")
    int getFavoriteCountByStation(String stationId);

    @Query("SELECT EXISTS  (SELECT 1 FROM favorites)")
    LiveData<Boolean> hasFavorites();

    @Query("SELECT EXISTS (SELECT 1 FROM favorites WHERE station_id = :stationId)")
    boolean isFavorite(String stationId);

    @Query("SELECT EXISTS (SELECT 1 FROM favorites WHERE station_id = :stationId)")
    LiveData<Boolean> isLiveFavorite(String stationId);

    @Query("UPDATE favorites SET count = count + 1 WHERE station_id = :stationId")
    void updateStationViewCount(String stationId);

    @Insert
    void AddStationToFavorite(Favorite favorite);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void UpdateStationUseCount(Favorite favorite);

    @Delete
    void deleteStationFromFavorites(Favorite favorite);

}
