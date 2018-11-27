/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import net.hobbitsoft.android.sailingbuddy.adapters.SearchFilterAdapter;
import net.hobbitsoft.android.sailingbuddy.adapters.StationsListRecyclerAdapter;
import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.data.StationList;
import net.hobbitsoft.android.sailingbuddy.database.Favorite;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.database.StationTable;
import net.hobbitsoft.android.sailingbuddy.utilities.AppExecutors;
import net.hobbitsoft.android.sailingbuddy.utilities.CoordinateUtils;
import net.hobbitsoft.android.sailingbuddy.utilities.IntentKeys;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();
    private MutableLiveData<List<StationList>> mStationLists = new MutableLiveData<>();
    SearchFilterAdapter mSearchFilterAdapter;

    @BindView(R.id.search_station_list)
    RecyclerView mStationListsRecyclerView;

    @BindView(R.id.search_string)
    AutoCompleteTextView mSearchString;

    private StationsListRecyclerAdapter mStationsListRecyclerAdapter;
    DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();

    private SailingBuddyDatabase sailingBuddyDatabase;
    private List<Favorite> mFavoiteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        Toolbar toolBar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolBar);
        toolBar.setTitle(getTitle());

        sailingBuddyDatabase = SailingBuddyDatabase.getInstance(this);
        setTitle("Station Search");

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle = intent.getBundleExtra(IntentKeys.CURRENT_LOCATION);
        mCurrentDecimalCoordinates = bundle.getParcelable(IntentKeys.CURRENT_LOCATION);

        getFavorites();
        getStations();
    }

    private void getFavorites() {
        new RetrieveFavorites().execute();

    }

    private class RetrieveFavorites extends AsyncTask<List<Favorite>, Void, List<Favorite>> {
        @Override
        protected List<Favorite> doInBackground(List<Favorite>... lists) {
            Log.d(TAG, "Retrieving Favorite Stations");
            List<Favorite> stationLists = new ArrayList<>();
            stationLists = sailingBuddyDatabase.favoritesDAO().getAllFavorites();
            return stationLists;
        }

        @Override
        protected void onPostExecute(List<Favorite> favoriteStations) {
            super.onPostExecute(favoriteStations);
            saveFavorites(favoriteStations);
        }
    }

    // We need the Favorite Stations before the Closest Sations in order to properly process if Favorite
    private void saveFavorites(List<Favorite> favoriteStations) {
        mFavoiteList = favoriteStations;
    }


    private void getStations() {
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<StationTable> localStationTableList = sailingBuddyDatabase.stationsDAO().getAllStations();
                final List<Favorite> localFavoriteList = mFavoiteList;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processStationTable(localStationTableList, localFavoriteList);
                    }
                });

            }
        });
    }

    private void processStationTable(List<StationTable> stationTableList, List<Favorite> favoriteList) {
        List<StationList> localStationLists = new ArrayList<>();
        for (StationTable stationTable : stationTableList) {
            Log.d(TAG, "Station: " + stationTable.getStationId() + " / " + stationTable.getName());
            StationList stationList = new StationList(stationTable);
            stationList.setFavorite(false);
            for (Favorite favorite : favoriteList) {
                if (favorite.getStationId().equals(stationTable.getStationId())) {
                    stationList.setFavorite(true);
                    Log.d(TAG, "Station is a Favorite");
                }
            }
            double distance = CoordinateUtils.getDistance(mCurrentDecimalCoordinates.getLatLng(), stationTable.getDecimalCoordinates().getLatLng());
            stationList.setDistance(distance);
            localStationLists.add(stationList);
        }
        mStationLists.postValue(localStationLists);
        setupSearchStationsRecycler(localStationLists);
    }

    private void setupSearchStationsRecycler(List<StationList> stationLists) {

        if (stationLists == null) {
            stationLists = new ArrayList<>();
        }

        mStationsListRecyclerAdapter = new StationsListRecyclerAdapter(this, stationLists);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mStationListsRecyclerView.setLayoutManager(linearLayoutManager);
        mStationListsRecyclerView.setAdapter(mStationsListRecyclerAdapter);
        mStationListsRecyclerView.setHasFixedSize(true);

        mSearchFilterAdapter = new SearchFilterAdapter(mStationsListRecyclerAdapter);
        mSearchFilterAdapter.setStationLists(stationLists);
        mSearchString.setAdapter(mSearchFilterAdapter);
    }
}