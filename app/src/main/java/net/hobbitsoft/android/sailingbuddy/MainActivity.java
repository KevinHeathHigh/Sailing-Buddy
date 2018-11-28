/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import net.hobbitsoft.android.sailingbuddy.adapters.ClosestStationsListRecyclerAdapter;
import net.hobbitsoft.android.sailingbuddy.adapters.FavoritesListRecyclerAdapter;
import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.data.StationList;
import net.hobbitsoft.android.sailingbuddy.data.StringCoordinates;
import net.hobbitsoft.android.sailingbuddy.database.Favorite;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.utilities.AppExecutors;
import net.hobbitsoft.android.sailingbuddy.utilities.CoordinateUtils;
import net.hobbitsoft.android.sailingbuddy.utilities.DistanceSort;
import net.hobbitsoft.android.sailingbuddy.utilities.InstanceStateKeys;
import net.hobbitsoft.android.sailingbuddy.utilities.IntentKeys;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a list of StationTable. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private List<DistanceSort> mListOfClosestStations;
    private String mSelectedStationId;
    private String mSelectedStationName;
    private DecimalCoordinates mSelectedCoordinates = new DecimalCoordinates();
    private DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();

    private boolean mHasLocation = false;
    private boolean mHasForecast = false;

    private static SailingBuddyDatabase sailingBuddyDatabase;
    private static ClosestStationsListRecyclerAdapter mClosestListRecyclerAdapter;
    private static FavoritesListRecyclerAdapter mFavoriteListRecyclerAdapter;

    private PagerAdapter mPagerAdapter;

    @BindView(R.id.station_list_tabs)
    TabLayout mTabLayout;

    @BindView(R.id.station_list_view_pagers)
    ViewPager mViewPager;

    @BindView(R.id.main_toolbar)
    Toolbar mMainToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mMainToolBar);
        mMainToolBar.setTitle(getTitle());

        sailingBuddyDatabase = SailingBuddyDatabase.getInstance(getApplicationContext());

        String closestString = this.getString(R.string.label_closest_stations);
        String favoritesString = this.getString(R.string.label_favorite_stations);
        String labels[] = new String[]{closestString, favoritesString};

        if (savedInstanceState == null) {
            mSelectedStationId = getIntent().getStringExtra(IntentKeys.CLOSEST_STATION_ID);
            mHasLocation = getIntent().getBooleanExtra(IntentKeys.HAS_LOCATION, false);
            Bundle bundle = getIntent().getBundleExtra(IntentKeys.CLOSEST_STATION_LIST);
            mListOfClosestStations = bundle.getParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST);
            if (mHasLocation) {
                mCurrentDecimalCoordinates = bundle.getParcelable(IntentKeys.CURRENT_LOCATION);
            }

            Bundle arguments = new Bundle();
            arguments.putString(IntentKeys.OVERVIEW_STATION_ID,
                    getIntent().getStringExtra(IntentKeys.CLOSEST_STATION_ID));
            StationOverviewFragment stationOverviewFragment = new StationOverviewFragment();
            stationOverviewFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.station_overview, stationOverviewFragment)
                    .commit();
        } else {
            if (savedInstanceState.containsKey(InstanceStateKeys.CURRENT_COORDINATES)) {
                mCurrentDecimalCoordinates = savedInstanceState.getParcelable(InstanceStateKeys.CURRENT_COORDINATES);
            }
            if (savedInstanceState.containsKey(InstanceStateKeys.HAS_LOCATION)) {
                mHasLocation = savedInstanceState.getBoolean(InstanceStateKeys.HAS_LOCATION);
            }
            if (savedInstanceState.containsKey(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS)) {
                mListOfClosestStations = savedInstanceState.getParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS);
            }
            if (savedInstanceState.containsKey(InstanceStateKeys.SELECTED_STATION_ID)) {
                mSelectedStationId = savedInstanceState.getString(InstanceStateKeys.SELECTED_STATION_ID);
            }
            if (savedInstanceState.containsKey(InstanceStateKeys.SELECTED_STATION_NAME)) {
                mSelectedStationName = savedInstanceState.getString(InstanceStateKeys.SELECTED_STATION_NAME);
            }
            if (savedInstanceState.containsKey(InstanceStateKeys.SELECTED_COORDINATES)) {
                mSelectedCoordinates = savedInstanceState.getParcelable(InstanceStateKeys.SELECTED_COORDINATES);
            }
            setTitleText();
        }

        ClosestTabFragment closestTabFragment = ClosestTabFragment.newInstance(mHasLocation,
                mListOfClosestStations, mCurrentDecimalCoordinates);
        FavoriteTabFragment favoriteTabFragment = FavoriteTabFragment.newInstance(mHasLocation,
                mListOfClosestStations, mCurrentDecimalCoordinates);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPagerAdapter.setFragmentList(closestTabFragment);
        mPagerAdapter.setFragmentList(favoriteTabFragment);
        mPagerAdapter.setLabels(labels);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "On Tab Selected: " + tab.getText());
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void updateSelectedStation(String stationId, String stationName) {
        this.mSelectedStationId = stationId;
        this.mSelectedStationName = stationName;
        getSelectedCoordinates(stationId);
        setTitleText();
    }

    private void setTitleText() {
        String makeTitle = new String();
        if (mSelectedStationId != null && mSelectedStationName != null) {
            makeTitle = mSelectedStationId + " - " + mSelectedStationName;
        } else if (mSelectedStationId != null) {
            makeTitle = mSelectedStationId;
        } else if (mSelectedStationName != null) {
            makeTitle = mSelectedStationName;
        }
        mMainToolBar.setTitle(makeTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.ov_mn_map:
                intent = new Intent(this, MapsActivity.class);
                intent.putExtra(IntentKeys.SELECTED_STATION_ID, mSelectedStationId);
                intent.putExtra(IntentKeys.SELECTED_STATION_NAME, mSelectedStationName);
                bundle.putParcelable(IntentKeys.SELECTED_LOCATION, mSelectedCoordinates);
                intent.putExtra(IntentKeys.SELECTED_LOCATION, bundle);
                startActivity(intent);
                return true;
            case R.id.ov_mn_search:
                intent = new Intent(this, SearchActivity.class);
                bundle.putParcelable(IntentKeys.CURRENT_LOCATION, mCurrentDecimalCoordinates);
                intent.putExtra(IntentKeys.CURRENT_LOCATION, bundle);
                startActivity(intent);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static FavoritesListRecyclerAdapter getFavoriteListRecyclerAdapter() {
        return mFavoriteListRecyclerAdapter;
    }

    public static ClosestStationsListRecyclerAdapter getClosestListRecyclerAdapter() {
        return mClosestListRecyclerAdapter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS, (ArrayList<? extends Parcelable>) mListOfClosestStations);
        outState.putString(InstanceStateKeys.SELECTED_STATION_ID, mSelectedStationId);
        outState.putString(InstanceStateKeys.SELECTED_STATION_NAME, mSelectedStationName);
        outState.putParcelable(InstanceStateKeys.SELECTED_COORDINATES, mSelectedCoordinates);
        outState.putBoolean(InstanceStateKeys.HAS_LOCATION, mHasLocation);
        outState.putParcelable(InstanceStateKeys.CURRENT_COORDINATES, mCurrentDecimalCoordinates);
    }

    private void getSelectedCoordinates(String stationId) {
        if (!stationId.isEmpty()) {
            mSelectedStationId = stationId;
            LiveData<DecimalCoordinates> stationCoordinates = sailingBuddyDatabase.stationsDAO().getLiveDecimalCoordinatesFromStation(stationId);
            stationCoordinates.observe(this, new Observer<DecimalCoordinates>() {
                @Override
                public void onChanged(DecimalCoordinates decimalCoordinates) {
                    stationCoordinates.removeObserver(this);
                    updateSelectedCoordinates(decimalCoordinates);
                }
            });
        }
    }

    private void updateSelectedCoordinates(DecimalCoordinates stationCoordinates) {
        mSelectedCoordinates = stationCoordinates;
    }


    public class PagerAdapter extends FragmentStatePagerAdapter {

        private String[] mLabels;
        private List<Fragment> mFragmentList = new ArrayList<>();

        public void setFragmentList(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        public void setLabels(String[] mLabels) {
            this.mLabels = mLabels;
        }

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "Get Item: " + String.valueOf(position));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mLabels[position];
        }

    }

    public static class ClosestTabFragment extends Fragment {

        private boolean mHasLocation;
        private List<DistanceSort> mListOfClosestStations;
        private DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();
        private static List<StationList> mClosestStationsList = new ArrayList<>();
        private static RecyclerView mClosestRecyclerView;

        public static ClosestTabFragment newInstance(boolean hasLocation,
                                                     List<DistanceSort> listOfClosestStations,
                                                     DecimalCoordinates currentDecimalCoordinates) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(IntentKeys.HAS_LOCATION, hasLocation);
            bundle.putParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST, (ArrayList<DistanceSort>) listOfClosestStations);
            bundle.putParcelable(IntentKeys.CURRENT_LOCATION, currentDecimalCoordinates);
            ClosestTabFragment closestTabFragment = new ClosestTabFragment();
            closestTabFragment.setArguments(bundle);
            return closestTabFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "On Create Closest Tab Fragment");
            if (savedInstanceState == null) {
                this.mHasLocation = getArguments().getBoolean(IntentKeys.HAS_LOCATION);
                this.mListOfClosestStations = getArguments().getParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST);
                this.mCurrentDecimalCoordinates = getArguments().getParcelable(IntentKeys.CURRENT_LOCATION);
            } else {
                this.mHasLocation = savedInstanceState.getBoolean(InstanceStateKeys.HAS_LOCATION);
                this.mListOfClosestStations = savedInstanceState.getParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS);
                this.mCurrentDecimalCoordinates = savedInstanceState.getParcelable(InstanceStateKeys.CURRENT_COORDINATES);
                if (savedInstanceState.containsKey(InstanceStateKeys.CLOSESTS_STATIONS_LIST)) {
                    mClosestStationsList = savedInstanceState.getParcelableArrayList(InstanceStateKeys.CLOSESTS_STATIONS_LIST);
                }
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View view = inflater.inflate(R.layout.viewpager_closest_recycler_list, container, false);
            mClosestRecyclerView = view.findViewById(R.id.view_pager_closest_recycler_list);
            setupClosestStationsRecycler();
            return view;
        }


        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(InstanceStateKeys.HAS_LOCATION, mHasLocation);
            outState.putParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS, (ArrayList<? extends Parcelable>) mListOfClosestStations);
            outState.putParcelable(InstanceStateKeys.CURRENT_COORDINATES, mCurrentDecimalCoordinates);
            outState.putParcelableArrayList(InstanceStateKeys.CLOSESTS_STATIONS_LIST, (ArrayList<? extends Parcelable>) mClosestStationsList);
        }

        private void getClosestStations() {
            Log.d(TAG, "Getting Closest Stations");
            AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mClosestStationsList.clear();
                    for (DistanceSort station : mListOfClosestStations) {
                        StationList stationList = new StationList(station.getStationId());
                        stationList.setStationName(sailingBuddyDatabase.stationsDAO().getStationNameByStationId(station.getStationId()));
                        if (mHasLocation) {
                            stationList.setDistance(CoordinateUtils.
                                    getDistance(mCurrentDecimalCoordinates.getLatLng(),
                                            sailingBuddyDatabase.stationsDAO().
                                                    getDecimalCoordinatesFromStation(station.getStationId()).getLatLng()));
                        }
                        stationList.setFavorite(sailingBuddyDatabase.favoritesDAO().
                                isFavorite(station.getStationId()));
                        stationList.setStringCoordinates(sailingBuddyDatabase.stationsDAO()
                                .getStringCoordinatesFromStation(station.getStationId()));
                        mClosestStationsList.add(stationList);
                    }
                }
            });
            mClosestListRecyclerAdapter.notifyDataSetChanged();
        }

        private void setupClosestStationsRecycler() {
            Log.d(TAG, "Setup Closest Stations Recycler");
            mClosestListRecyclerAdapter = new ClosestStationsListRecyclerAdapter(getActivity(), mClosestStationsList);
            mClosestRecyclerView.setHasFixedSize(true);
            mClosestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mClosestRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mClosestRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
            mClosestRecyclerView.setAdapter(mClosestListRecyclerAdapter);
            mClosestListRecyclerAdapter.setClickListener(new ClosestStationsListRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemCLick(View view, String stationId) {
                    Log.d(TAG, "onItemClick - Station ID: " + stationId);
                    Bundle arguments = new Bundle();
                    arguments.putString(IntentKeys.OVERVIEW_STATION_ID,
                            stationId);
                    StationOverviewFragment stationOverviewFragment = new StationOverviewFragment();
                    stationOverviewFragment.setArguments(arguments);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.station_overview, stationOverviewFragment)
                            .commit();
                    AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (sailingBuddyDatabase.favoritesDAO().isFavorite(stationId)) {
                                sailingBuddyDatabase.favoritesDAO().updateStationViewCount(stationId);
                            }
                        }
                    });
                }
            });
            getClosestStations();
        }
    }

    public static class FavoriteTabFragment extends Fragment {

        private boolean mHasLocation;
        private List<DistanceSort> mListOfClosestStations;
        private DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();
        private static List<StationList> mFavoritesStationsList = new ArrayList<>();
        private static RecyclerView mFavoritesRecyclerView;

        public static FavoriteTabFragment newInstance(boolean hasLocation,
                                                      List<DistanceSort> listOfClosestStations,
                                                      DecimalCoordinates currentDecimalCoordinates) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(IntentKeys.HAS_LOCATION, hasLocation);
            bundle.putParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST, (ArrayList<DistanceSort>) listOfClosestStations);
            bundle.putParcelable(IntentKeys.CURRENT_LOCATION, currentDecimalCoordinates);
            FavoriteTabFragment favoriteTabFragment = new FavoriteTabFragment();
            favoriteTabFragment.setArguments(bundle);
            return favoriteTabFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "On Create Favorites Tab Fragment");
            if (savedInstanceState == null) {
                this.mHasLocation = getArguments().getBoolean(IntentKeys.HAS_LOCATION);
                this.mListOfClosestStations = getArguments().getParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST);
                this.mCurrentDecimalCoordinates = getArguments().getParcelable(IntentKeys.CURRENT_LOCATION);
            } else {
                this.mHasLocation = savedInstanceState.getBoolean(InstanceStateKeys.HAS_LOCATION);
                this.mListOfClosestStations = savedInstanceState.getParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS);
                this.mCurrentDecimalCoordinates = savedInstanceState.getParcelable(InstanceStateKeys.CURRENT_COORDINATES);
                if (savedInstanceState.containsKey(InstanceStateKeys.FAVORITE_STATIONS_LIST)) {
                    mFavoritesStationsList = savedInstanceState.getParcelableArrayList(InstanceStateKeys.FAVORITE_STATIONS_LIST);
                }
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.viewpager_favorites_recycler_list, container, false);
            mFavoritesRecyclerView = view.findViewById(R.id.view_pager_favorites_recycler_list);
            setupFavoriteStationsRecycler();
            return view;
        }


        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(InstanceStateKeys.HAS_LOCATION, mHasLocation);
            outState.putParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS, (ArrayList<? extends Parcelable>) mListOfClosestStations);
            outState.putParcelable(InstanceStateKeys.CURRENT_COORDINATES, mCurrentDecimalCoordinates);
            outState.putParcelableArrayList(InstanceStateKeys.FAVORITE_STATIONS_LIST, (ArrayList<? extends Parcelable>) mFavoritesStationsList);
        }

        private void getFavoriteStationsList() {
            Log.d(TAG, "Getting Favorite Stations List");
            AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    List<Favorite> favoriteList = sailingBuddyDatabase.favoritesDAO().getAllFavoritesSortedByCount();
                    mFavoritesStationsList.clear();
                    for (Favorite favorite : favoriteList) {
                        StationList stationList = new StationList(favorite.getStationId());
                        String stationName = sailingBuddyDatabase.stationsDAO().getStationNameByStationId(favorite.getStationId());
                        stationList.setStationName(stationName);
                        stationList.setFavorite(true);
                        if (mHasLocation) {
                            DecimalCoordinates decimalCoordinates = sailingBuddyDatabase.stationsDAO().getDecimalCoordinatesFromStation(favorite.getStationId());
                            double distance = CoordinateUtils.getDistance(mCurrentDecimalCoordinates.getLatLng(), decimalCoordinates.getLatLng());
                            stationList.setDistance(distance);
                        }
                        StringCoordinates stringCoordinates = sailingBuddyDatabase.stationsDAO().getStringCoordinatesFromStation(favorite.getStationId());
                        stationList.setStringCoordinates(stringCoordinates);
                        mFavoritesStationsList.add(stationList);
                    }
                }
            });
            mFavoriteListRecyclerAdapter.notifyDataSetChanged();
        }

        private void setupFavoriteStationsRecycler() {
            Log.d(TAG, "Setup Favorite Stations Recycler");
            mFavoriteListRecyclerAdapter = new FavoritesListRecyclerAdapter(getActivity(), mFavoritesStationsList);
            mFavoritesRecyclerView.setHasFixedSize(true);
            mFavoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mFavoritesRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mFavoritesRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
            mFavoritesRecyclerView.setAdapter(mFavoriteListRecyclerAdapter);
            mFavoriteListRecyclerAdapter.setClickListener(new FavoritesListRecyclerAdapter.ItemClickListener() {
                @Override
                public void onItemCLick(View view, String stationId) {
                    Log.d(TAG, "onItemClick - Station ID: " + stationId);
                    Bundle arguments = new Bundle();
                    arguments.putString(IntentKeys.OVERVIEW_STATION_ID,
                            stationId);
                    StationOverviewFragment stationOverviewFragment = new StationOverviewFragment();
                    stationOverviewFragment.setArguments(arguments);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.station_overview, stationOverviewFragment)
                            .commit();
                    AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (sailingBuddyDatabase.favoritesDAO().isFavorite(stationId)) {
                                sailingBuddyDatabase.favoritesDAO().updateStationViewCount(stationId);
                            }
                        }
                    });
                    getClosestListRecyclerAdapter().notifyDataSetChanged();
                    getFavoriteListRecyclerAdapter().notifyDataSetChanged();
                }
            });
            getFavoriteStationsList();
        }
    }
}


