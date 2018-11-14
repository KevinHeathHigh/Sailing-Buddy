package net.hobbitsoft.android.sailingbuddy.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {StationTable.class, StationOwner.class, Favorite.class, StationDetails.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class SailingBuddyDatabase extends RoomDatabase {

    private static final String TAG = SailingBuddyDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "sailingbuddydatabase";
    private static SailingBuddyDatabase instance;

    public static SailingBuddyDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new database instance");
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        SailingBuddyDatabase.class, SailingBuddyDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.d(TAG, "Getting the database instance");
        return instance;
    }

    abstract public StationTableDAO stationsDAO();

    abstract public StationOwnersDAO stationOwnersDAO();

    abstract public FavoritesDAO favoritesDAO();

    abstract public StationCacheDAO stationCacheDAO();

}
