package net.hobbitsoft.android.sailingbuddy;

import android.content.Intent;
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

    @BindView(R.id.search_station_list)
    RecyclerView mStationListsRecyclerView;

    @BindView(R.id.search_string)
    AutoCompleteTextView mSearchString;

    private StationsListRecyclerAdapter mStationsListRecyclerAdapter;
    DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();

    private SailingBuddyDatabase sailingBuddyDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        Toolbar toolBar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolBar);
        toolBar.setTitle(getTitle());

        sailingBuddyDatabase = SailingBuddyDatabase.getInstance(this);
        setTitle("Station Search");

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle = intent.getBundleExtra(IntentKeys.CURRENT_LOCATION);
        mCurrentDecimalCoordinates = bundle.getParcelable(IntentKeys.CURRENT_LOCATION);

        getStations();
    }

    private void getStations() {
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<StationTable> localStationTableList = (List<StationTable>) sailingBuddyDatabase.stationsDAO().getAllStations();
                final List<Favorite> localFavoriteList = (List<Favorite>) sailingBuddyDatabase.favoritesDAO().getAllFavorites();
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

        //mStationLists.postValue(localStationLists);
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

        SearchFilterAdapter searchFilterAdapter = new SearchFilterAdapter(mStationsListRecyclerAdapter);
        searchFilterAdapter.setStationLists(stationLists);
        mSearchString.setAdapter(searchFilterAdapter);
    }
}