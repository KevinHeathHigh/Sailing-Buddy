package net.hobbitsoft.android.sailingbuddy.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface StationOwnersDAO {

    @Query("SELECT * FROM station_owners")
    List<StationOwner> getAllStationOwners();

    @Query("SELECT * FROM station_owners WHERE owner_code = :ownerId")
    StationOwner getStationOwnerByOwnerID(String ownerId);

    @Query("SELECT owner_name FROM station_owners WHERE owner_code = :ownerId")
    String getStationOwnerNameByOwnerID(String ownerId);

    @Query("SELECT EXISTS (SELECT 1 FROM station_owners WHERE owner_code = :ownerId)")
    boolean ownerInDatabase(String ownerId);

    @Insert
    void insertStationOwner(StationOwner stationOwner);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateStationOwner(StationOwner stationOwner);

    @Delete
    void deleteStationOwner(StationOwner stationOwner);

}
