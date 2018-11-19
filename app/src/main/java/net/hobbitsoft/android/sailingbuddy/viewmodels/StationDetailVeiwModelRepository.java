/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy.viewmodels;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.database.StationDetails;
import net.hobbitsoft.android.sailingbuddy.noaadata.NDBCData;
import net.hobbitsoft.android.sailingbuddy.utilities.AppExecutors;

import java.io.IOException;

import androidx.lifecycle.LiveData;

public class StationDetailVeiwModelRepository {

    private static final String TAG = StationDetailVeiwModelRepository.class.getSimpleName();
    private static StationDetailVeiwModelRepository mInstance;

    private LiveData<StationDetails> mStationDetails;
    private String mStationId;
    private final SailingBuddyDatabase mSailingBuddyDatabase;
    private AppExecutors appExecutors = AppExecutors.getsInstance();
    private final Context context;

    public static StationDetailVeiwModelRepository getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StationDetailVeiwModelRepository(context);
        }
        return mInstance;
    }

    private StationDetailVeiwModelRepository(Context context) {
        mSailingBuddyDatabase = SailingBuddyDatabase.getInstance(context);
        this.context = context;
    }

    public void setStationId(String stationId) {
        mStationId = stationId;
        updateStationDetail(stationId);
    }

    public LiveData<StationDetails> getStationDetails(String stationId) {
        fetchStationDetail(stationId);
        return mSailingBuddyDatabase.stationCacheDAO().getLiveStaionFromCache(stationId);
    }

    private void setStationDetail(LiveData<StationDetails> stationDetails) {
        mStationDetails = stationDetails;

    }

    private void updateStationDetail(final String stationId) {
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mSailingBuddyDatabase.stationCacheDAO().isStaionCached(stationId)) {
                    LiveData<StationDetails> stationDetailsLiveData = mSailingBuddyDatabase.stationCacheDAO().getLiveStaionFromCache(stationId);
                    setStationDetail(stationDetailsLiveData);
                }
                fetchStationDetail(stationId);
            }
        });
    }

    private void fetchStationDetail(String stationId) {
        new RetrieveStaionDetail().execute(stationId);
    }


    class RetrieveStaionDetail extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                NDBCData.getLatestObservations(context, mSailingBuddyDatabase, strings[0]);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return strings[0];
        }

        @Override
        protected void onPostExecute(String stationId) {
            super.onPostExecute(stationId);
            setStationDetail(mSailingBuddyDatabase.stationCacheDAO().getLiveStaionFromCache(stationId));
        }
    }
}
