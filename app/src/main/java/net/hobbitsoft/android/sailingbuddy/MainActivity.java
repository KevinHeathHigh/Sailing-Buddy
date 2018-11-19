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

import net.hobbitsoft.android.sailingbuddy.adapters.StationsListRecyclerAdapter;
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
import androidx.fragment.app.FragmentPagerAdapter;
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

    private boolean mHasLocation = false;
    private DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();

    private StationsListRecyclerAdapter mFavoriteListRecyclerAdapter;
    private StationsListRecyclerAdapter mClosestListRecyclerAdapter;

    private static SailingBuddyDatabase sailingBuddyDatabase;

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


        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mHasLocation, labels, mListOfClosestStations, mCurrentDecimalCoordinates);
        mViewPager.setAdapter(null);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
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

    public StationsListRecyclerAdapter getFavoriteListRecyclerAdapter() {
        return mFavoriteListRecyclerAdapter;
    }

    public StationsListRecyclerAdapter getClosestListRecyclerAdapter() {
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

    public class PagerAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 2;
        private boolean mHasLocation;
        private String[] mLabels;
        private List<DistanceSort> mListOfClosestStations = new ArrayList<>();
        private DecimalCoordinates mCurrentDecimalCoordinates;

        public PagerAdapter(FragmentManager fragmentManager, boolean hasLocation, String[] labels,
                            List<DistanceSort> listOfClosestStations, DecimalCoordinates currentDecimalCoordinates) {
            super(fragmentManager);
            this.mHasLocation = hasLocation;
            this.mLabels = labels;
            this.mListOfClosestStations = listOfClosestStations;
            this.mCurrentDecimalCoordinates = currentDecimalCoordinates;
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position + 1, mHasLocation, mLabels,
                    mListOfClosestStations, mCurrentDecimalCoordinates);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mLabels[position];
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
            container.removeAllViews();
        }
    }

    public static class PageFragment extends Fragment {
        private int mPage;
        private boolean mHasLocation;
        private String[] mLabels;
        private List<DistanceSort> mListOfClosestStations;
        private DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();

        private StationsListRecyclerAdapter mFavoritesListRecyclerAdapter;
        private RecyclerView mFavoritesRecyclerView;
        private StationsListRecyclerAdapter mClosestListRecyclerAdapter;
        private RecyclerView mClosestRecyclerView;


        public static PageFragment newInstance(int page, boolean hasLocation, String[] labels,
                                               List<DistanceSort> listOfClosestStations,
                                               DecimalCoordinates currentDecimalCoordinates) {
            Bundle bundle = new Bundle();
            bundle.putInt(IntentKeys.PAGE, page);
            bundle.putBoolean(IntentKeys.HAS_LOCATION, hasLocation);
            bundle.putStringArray(IntentKeys.LABELS, labels);
            bundle.putParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST, (ArrayList<DistanceSort>) listOfClosestStations);
            bundle.putParcelable(IntentKeys.CURRENT_LOCATION, currentDecimalCoordinates);
            PageFragment pageFragment = new PageFragment();
            pageFragment.setArguments(bundle);
            return pageFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "On Create Page Fragement");
            if (savedInstanceState == null) {
                this.mPage = getArguments().getInt(IntentKeys.PAGE);
                this.mHasLocation = getArguments().getBoolean(IntentKeys.HAS_LOCATION);
                this.mLabels = getArguments().getStringArray(IntentKeys.LABELS);
                this.mListOfClosestStations = getArguments().getParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST);
                this.mCurrentDecimalCoordinates = getArguments().getParcelable(IntentKeys.CURRENT_LOCATION);
            } else {
                this.mPage = savedInstanceState.getInt(InstanceStateKeys.PAGE);
                this.mHasLocation = savedInstanceState.getBoolean(InstanceStateKeys.HAS_LOCATION);
                this.mLabels = savedInstanceState.getStringArray(InstanceStateKeys.LABELS);
                this.mListOfClosestStations = savedInstanceState.getParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS);
                this.mCurrentDecimalCoordinates = savedInstanceState.getParcelable(InstanceStateKeys.CURRENT_COORDINATES);
            }

        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            Log.d(TAG, "On Create View Page Fragment");
            View view = inflater.inflate(R.layout.viewpager_recycler_list, container, false);
            this.mFavoritesRecyclerView = view.findViewById(R.id.view_pager_recycler_list);
            this.mClosestRecyclerView = view.findViewById(R.id.view_pager_recycler_list);

            if (mPage == 2) {
                getFavoriteStationsList();
            } else {
                if (mHasLocation) {
                    getClosestStations();
                }
            }
            return view;
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(InstanceStateKeys.PAGE, mPage);
            outState.putBoolean(InstanceStateKeys.HAS_LOCATION, mHasLocation);
            outState.putStringArray(InstanceStateKeys.LABELS, mLabels);
            outState.putParcelableArrayList(InstanceStateKeys.LIST_OF_CLOSEST_STATIONS, (ArrayList<? extends Parcelable>) mListOfClosestStations);
            outState.putParcelable(InstanceStateKeys.CURRENT_COORDINATES, mCurrentDecimalCoordinates);
        }

        private void getFavoriteStationsList() {
            Log.d(TAG, "Getting Favorite Stations List");
            LiveData<List<Favorite>> favoritesList = sailingBuddyDatabase.favoritesDAO().getAllFavoritesSortedByCount();
            favoritesList.observe(this, new Observer<List<Favorite>>() {
                @Override
                public void onChanged(List<Favorite> favoriteList) {
                    favoritesList.removeObserver(this);
                    List<StationList> stationLists = new ArrayList<>();
                    for (Favorite favorite : favoriteList) {
                        StationList stationList = new StationList(favorite.getStationId());
                        LiveData<String> stationName = sailingBuddyDatabase.stationsDAO().getLiveStationNameByStationId(favorite.getStationId());
                        stationName.observe(getActivity(), new Observer<String>() {
                            @Override
                            public void onChanged(String name) {
                                stationName.removeObserver(this);
                                stationList.setStationName(name);
                                stationList.setFavorite(true);
                                if (mHasLocation) {
                                    LiveData<DecimalCoordinates> stationCoordinates = sailingBuddyDatabase.stationsDAO().getLiveDecimalCoordinatesFromStation(favorite.getStationId());
                                    stationCoordinates.observe(getActivity(), new Observer<DecimalCoordinates>() {
                                        @Override
                                        public void onChanged(DecimalCoordinates decimalCoordinates) {
                                            stationCoordinates.removeObserver(this);
                                            double distance = CoordinateUtils.getDistance(mCurrentDecimalCoordinates.getLatLng(), decimalCoordinates.getLatLng());
                                            stationList.setDistance(distance);
                                        }
                                    });
                                }
                                LiveData<StringCoordinates> stringCoordinates = sailingBuddyDatabase.stationsDAO().getLiveStringCoordinatesFromStation(favorite.getStationId());
                                stringCoordinates.observe(getActivity(), new Observer<StringCoordinates>() {
                                    @Override
                                    public void onChanged(StringCoordinates coordinates) {
                                        stringCoordinates.removeObserver(this);
                                        stationList.setStringCoordinates(coordinates);
                                    }
                                });
                                stationLists.add(stationList);
                            }
                        });
                    }
                    updateFavoriteStationsRecycler(stationLists);
                }
            });
        }

        private void updateFavoriteStationsRecycler(List<StationList> stationLists) {
            if (mFavoritesListRecyclerAdapter == null) {
                setupFavoriteStationsRecycler(stationLists);
            } else {
                mFavoritesListRecyclerAdapter.notifyDataSetChanged();
            }
        }

        private void setupFavoriteStationsRecycler(List<StationList> stationLists) {
            Log.d(TAG, "Setup Favorite Stations Recycler");
            if (stationLists == null) {
                stationLists = new ArrayList<>();
            }
            mFavoritesListRecyclerAdapter = new StationsListRecyclerAdapter(getContext(), stationLists);
            mFavoritesRecyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mFavoritesRecyclerView.setLayoutManager(linearLayoutManager);
            mFavoritesRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mFavoritesRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
            mFavoritesRecyclerView.setAdapter(mFavoritesListRecyclerAdapter);
            mFavoritesListRecyclerAdapter.setClickListener(new StationsListRecyclerAdapter.ItemClickListener() {
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
        }

        private void getClosestStations() {
            Log.d(TAG, "Getting Closest Stations");
            List<StationList> stationLists = new ArrayList<>();

            for (DistanceSort station : mListOfClosestStations) {
                StationList stationList = new StationList(station.getStationId());
                LiveData<String> stationName = sailingBuddyDatabase.stationsDAO().getLiveStationNameByStationId(station.getStationId());
                stationName.observe(getActivity(), new Observer<String>() {
                    @Override
                    public void onChanged(String name) {
                        stationName.removeObserver(this);
                        stationList.setStationName(name);
                    }
                });
                if (mHasLocation) {
                    LiveData<DecimalCoordinates> stationCoordinates = sailingBuddyDatabase.stationsDAO().getLiveDecimalCoordinatesFromStation(station.getStationId());
                    stationCoordinates.observe(getActivity(), new Observer<DecimalCoordinates>() {
                        @Override
                        public void onChanged(DecimalCoordinates decimalCoordinates) {
                            stationCoordinates.removeObserver(this);
                            double distance = CoordinateUtils.getDistance(mCurrentDecimalCoordinates.getLatLng(), decimalCoordinates.getLatLng());
                            stationList.setDistance(distance);
                        }
                    });
                }
                LiveData<Boolean> isFavorite = sailingBuddyDatabase.favoritesDAO().isLiveFavorite(station.getStationId());
                isFavorite.observe(getActivity(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        isFavorite.removeObserver(this);
                        stationList.setFavorite(aBoolean.booleanValue());
                    }
                });
                LiveData<StringCoordinates> stringCoordinates = sailingBuddyDatabase.stationsDAO().getLiveStringCoordinatesFromStation(station.getStationId());
                stringCoordinates.observe(getActivity(), new Observer<StringCoordinates>() {
                    @Override
                    public void onChanged(StringCoordinates coordinates) {
                        stringCoordinates.removeObserver(this);
                        stationList.setStringCoordinates(coordinates);
                    }
                });
                stationLists.add(stationList);
            }

            updateClosestStationsRecycler(stationLists);
        }

        private void updateClosestStationsRecycler(List<StationList> stationLists) {
            if (mClosestListRecyclerAdapter == null) {
                setupClosestStationsRecycler(stationLists);
            } else {
                mClosestListRecyclerAdapter.notifyDataSetChanged();
            }
        }

        private void setupClosestStationsRecycler(List<StationList> stationLists) {
            Log.d(TAG, "Setup Closest Stations Recycler");
            mClosestListRecyclerAdapter = new StationsListRecyclerAdapter(getContext(), stationLists);
            mClosestRecyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            mClosestRecyclerView.setLayoutManager(linearLayoutManager);
            mClosestRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mClosestRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
            mClosestRecyclerView.setAdapter(mClosestListRecyclerAdapter);
            mClosestListRecyclerAdapter.setClickListener(new StationsListRecyclerAdapter.ItemClickListener() {
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

        }
    }
}

