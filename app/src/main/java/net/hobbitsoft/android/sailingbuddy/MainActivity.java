package net.hobbitsoft.android.sailingbuddy;

import android.content.Intent;
import android.os.AsyncTask;
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

    @BindView(R.id.station_list_tabs)
    TabLayout mTabLayout;

    @BindView(R.id.station_list_view_pagers)
    ViewPager mViewPager;

    @BindView(R.id.main_toolbar)
    Toolbar mMainToolBar;
    private List<Favorite> mFavoiteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mMainToolBar);
        mMainToolBar.setTitle(getTitle());

        sailingBuddyDatabase = SailingBuddyDatabase.getInstance(getApplicationContext());
        getFavorites();
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
        }

        getSelectedStationCoordinatesAndName(mSelectedStationId);
        setTitleText();

        mViewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), mHasLocation, labels, mListOfClosestStations, mFavoiteList, mCurrentDecimalCoordinates, this));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void setTitleText() {
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

    public StationsListRecyclerAdapter getmFavoriteListRecyclerAdapter() {
        return mFavoriteListRecyclerAdapter;
    }

    public StationsListRecyclerAdapter getmClosestListRecyclerAdapter() {
        return mClosestListRecyclerAdapter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        if (mClosestListRecyclerAdapter != null) {
            mClosestListRecyclerAdapter.notifyDataSetChanged();
        }
        if (mFavoriteListRecyclerAdapter != null) {
            mFavoriteListRecyclerAdapter.notifyDataSetChanged();
        }
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

    public void getSelectedStationCoordinatesAndName(String stationId) {
        if (!stationId.isEmpty()) {
            mSelectedStationId = stationId;
            AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                String stationName;
                DecimalCoordinates stationCoordinates = new DecimalCoordinates();

                @Override
                public void run() {
                    stationCoordinates = sailingBuddyDatabase.stationsDAO().getDecimalCoordinatesFromStation(stationId);
                    stationName = sailingBuddyDatabase.stationsDAO().getStationNameByStationId(stationId);
                    updateSelectedCoordinatesAndName(stationCoordinates, stationName);
                }
            });
        }
    }

    private void updateSelectedCoordinatesAndName(DecimalCoordinates stationCoordinates, String stationName) {
        mSelectedCoordinates = stationCoordinates;
        mSelectedStationName = stationName;
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

    public class PagerAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 2;
        private boolean mHasLocation;
        private String[] mLabels;
        private List<DistanceSort> mListOfClosestStations = new ArrayList<>();
        private DecimalCoordinates mCurrentDecimalCoordinates;
        private MainActivity mMainActivity;
        private List<Favorite> mFavoriteList = new ArrayList<>();

        public PagerAdapter(FragmentManager fragmentManager, boolean hasLocation, String[] labels,
                            List<DistanceSort> listOfClosestStations, List<Favorite> favoriteList, DecimalCoordinates currentDecimalCoordinates, MainActivity mainActivity) {
            super(fragmentManager);
            this.mHasLocation = hasLocation;
            this.mLabels = labels;
            this.mListOfClosestStations = listOfClosestStations;
            this.mCurrentDecimalCoordinates = currentDecimalCoordinates;
            this.mFavoriteList = favoriteList;
            this.mMainActivity = mainActivity;
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position + 1, mHasLocation, mLabels,
                    mListOfClosestStations, mFavoriteList, mCurrentDecimalCoordinates, mMainActivity);
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
        }
    }

    public static class PageFragment extends Fragment {
        private int mPage;
        private boolean mHasLocation;
        private String[] mLabels;
        private List<DistanceSort> mListOfClosestStations;
        private DecimalCoordinates mCurrentDecimalCoordinates = new DecimalCoordinates();
        private List<Favorite> mFavoriteStations = new ArrayList<>();
        private List<StationList> mFavoriteStationsList = new ArrayList<>();
        private List<StationList> mClosestStationsList = new ArrayList<>();
        private static MainActivity mMainActivity;
        private StationsListRecyclerAdapter mFavoritesListRecyclerAdapter;
        private RecyclerView mFavoritesRecyclerView;
        private StationsListRecyclerAdapter mClosestListRecyclerAdapter;
        private RecyclerView mClosestRecyclerView;

        public static PageFragment newInstance(int page, boolean hasLocation, String[] labels,
                                               List<DistanceSort> listOfClosestStations, List<Favorite> favoriteList,
                                               DecimalCoordinates currentDecimalCoordinates, MainActivity mainActivity) {
            mMainActivity = mainActivity;
            Bundle bundle = new Bundle();
            bundle.putInt(IntentKeys.PAGE, page);
            bundle.putBoolean(IntentKeys.HAS_LOCATION, hasLocation);
            bundle.putStringArray(IntentKeys.LABELS, labels);
            bundle.putParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST, (ArrayList<DistanceSort>) listOfClosestStations);
            bundle.putParcelable(IntentKeys.CURRENT_LOCATION, currentDecimalCoordinates);
            bundle.putParcelableArrayList(IntentKeys.FAVORITE_STATIONS, (ArrayList<Favorite>) favoriteList);
            PageFragment pageFragment = new PageFragment();
            pageFragment.setArguments(bundle);

            return pageFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPage = getArguments().getInt(IntentKeys.PAGE);
            mHasLocation = getArguments().getBoolean(IntentKeys.HAS_LOCATION);
            mLabels = getArguments().getStringArray(IntentKeys.LABELS);
            mListOfClosestStations = getArguments().getParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST);
            mFavoriteStations = getArguments().getParcelableArrayList(IntentKeys.FAVORITE_STATIONS);
            mCurrentDecimalCoordinates = getArguments().getParcelable(IntentKeys.CURRENT_LOCATION);

            if (mPage == 2) {
                assert mFavoriteStationsList != null;
                if (mFavoriteStationsList.size() != 0) {
                    getFavoriteStationsList();
                }
            } else {
                assert mClosestStationsList != null;
                if (mHasLocation && (mListOfClosestStations.size() != 0)) {
                    getClostestStations();
                }
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.viewpager_recycler_list, container, false);
            mFavoritesRecyclerView = (RecyclerView) view.findViewById(R.id.view_pager_recycler_list);
            mClosestRecyclerView = (RecyclerView) view.findViewById(R.id.view_pager_recycler_list);
            return view;
        }

        private void getFavoriteStationsList() {
            new RetrieveFavoriteStationsList().execute();
        }

        private class RetrieveFavoriteStationsList extends AsyncTask<Void, Void, List<StationList>> {

            @Override
            protected List<StationList> doInBackground(Void... voids) {
                List<StationList> stationLists = new ArrayList<>();
                Log.d(TAG, "Retrieving Favorite Stations");
                if (sailingBuddyDatabase.favoritesDAO().hasFavorites()) {

                    List<Favorite> favoriteList = (List<Favorite>) sailingBuddyDatabase.favoritesDAO().getAllFavoritesSortedByCount();

                    for (Favorite favorite : favoriteList) {
                        StationList stationList = new StationList(favorite.getStationId());
                        stationList.setStationName(sailingBuddyDatabase.stationsDAO().getStationNameByStationId(favorite.getStationId()));
                        stationList.setFavorite(true);
                        if (mHasLocation) {
                            DecimalCoordinates stationCoordinates = sailingBuddyDatabase.stationsDAO().getDecimalCoordinatesFromStation(favorite.getStationId());
                            double distance = CoordinateUtils.getDistance(mCurrentDecimalCoordinates.getLatLng(), stationCoordinates.getLatLng());
                            stationList.setDistance(distance);
                        }
                        stationList.setStringCoordinates(sailingBuddyDatabase.stationsDAO().getStringCoordinatesFromStation(favorite.getStationId()));
                        stationLists.add(stationList);
                    }
                }
                return stationLists;
            }

            @Override
            protected void onPostExecute(List<StationList> stationLists) {
                super.onPostExecute(stationLists);
                processFavoriteStationList(stationLists);
            }
        }

        private void processFavoriteStationList(List<StationList> stationList) {
            if (stationList != null) {
                for (StationList station : stationList) {
                    station.setFavorite(false);
                    if (mFavoriteStations != null) {
                        for (Favorite favorite : mFavoriteStations) {
                            if (favorite.getStationId().equals(station.getStationId())) {
                                station.setFavorite(true);
                            }
                        }
                    }
                    setupFavoriteStationsRecycler(stationList);
                }
            }
        }

        private void setupFavoriteStationsRecycler(List<StationList> stationLists) {
            if (stationLists == null) {
                stationLists = new ArrayList<>();
            }

            mFavoriteStationsList = stationLists;
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
                    mMainActivity.getSelectedStationCoordinatesAndName(stationId);
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

        private void getClostestStations() {
            new RetrieveClosesList().execute();
        }

        private class RetrieveClosesList extends AsyncTask<List<DistanceSort>, Void, List<StationList>> {

            @Override
            protected List<StationList> doInBackground(List<DistanceSort>... lists) {
                Log.d(TAG, "Retrieving Closest Stations");
                List<StationList> stationLists = new ArrayList<>();

                for (DistanceSort station : mListOfClosestStations) {
                    StationList stationList = new StationList(station.getStationId());
                    stationList.setStationName(sailingBuddyDatabase.stationsDAO().getStationNameByStationId(station.getStationId()));
                    stationList.setFavorite(true);
                    if (mHasLocation) {
                        DecimalCoordinates stationCoordinates = sailingBuddyDatabase.stationsDAO().getDecimalCoordinatesFromStation(station.getStationId());
                        double distance = CoordinateUtils.getDistance(mCurrentDecimalCoordinates.getLatLng(), stationCoordinates.getLatLng());
                        stationList.setDistance(distance);
                    }
                    stationList.setStringCoordinates(sailingBuddyDatabase.stationsDAO().getStringCoordinatesFromStation(station.getStationId()));
                    stationLists.add(stationList);
                }

                return stationLists;
            }

            @Override
            protected void onPostExecute(List<StationList> stationLists) {
                super.onPostExecute(stationLists);
                processClosetStationList(stationLists);
            }
        }

        private void processClosetStationList(List<StationList> stationList) {
            for (StationList station : stationList) {
                station.setFavorite(false);
                if (mFavoriteStations != null) {
                    for (Favorite favorite : mFavoriteStations) {
                        if (favorite.getStationId().equals(station.getStationId())) {
                            station.setFavorite(true);
                        }
                    }
                }
            }
            setupClosestStationsRecycler(stationList);
        }

        private void setupClosestStationsRecycler(List<StationList> stationLists) {
            mClosestStationsList = stationLists;
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
                    mMainActivity.getSelectedStationCoordinatesAndName(stationId);
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

