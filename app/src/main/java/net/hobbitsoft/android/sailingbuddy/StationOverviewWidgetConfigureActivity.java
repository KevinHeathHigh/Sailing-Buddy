/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.hobbitsoft.android.sailingbuddy.adapters.StationsListRecyclerAdapter;
import net.hobbitsoft.android.sailingbuddy.data.StationList;
import net.hobbitsoft.android.sailingbuddy.database.Favorite;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The configuration screen for the {@link StationOverviewWidget StationOverviewWidget} AppWidget.
 */
public class StationOverviewWidgetConfigureActivity extends Activity implements StationsListRecyclerAdapter.ItemClickListener {

    private static final String TAG = StationOverviewWidgetConfigureActivity.class.getSimpleName();
    private static final String PREFS_NAME = StationOverviewWidgetConfigureActivity.class.getSimpleName();
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private SailingBuddyDatabase mSailingBuddyDatabase;

    @BindView(R.id.widget_station_list)
    RecyclerView mFavoritesRecycler;

    @BindView(R.id.widget_no_favorites_display)
    TextView mNoFavoritesMessage;

    private static List<StationList> mFavoriteStationList = new ArrayList<>();
    private StationsListRecyclerAdapter mStationsListRecyclerAdapter;

    public StationOverviewWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }


    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        return titleValue;
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.station_overview_widget_configure);
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        ButterKnife.bind(this);
        mSailingBuddyDatabase = SailingBuddyDatabase.getInstance(this);


        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        getFavorites();
    }


    private void getFavorites() {
        new RetrieveFavorites().execute();
    }

    @Override
    public void onItemCLick(View view, String stationId) {
        final Context context = StationOverviewWidgetConfigureActivity.this;

        // When the view is clicked, store the string locally
        //String widgetText = mAppWidgetText.getText().toString();
        saveTitlePref(context, mAppWidgetId, stationId);

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        StationOverviewWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private class RetrieveFavorites extends AsyncTask<Void, Void, List<StationList>> {
        @Override
        protected List<StationList> doInBackground(Void... voids) {
            Log.d(TAG, "Retrieving Favorite Stations");
            List<StationList> stationLists = new ArrayList<>();
            List<Favorite> favoriteList = new ArrayList<>();
            favoriteList = mSailingBuddyDatabase.favoritesDAO().getAllFavorites();
            for (Favorite favorite : favoriteList) {
                StationList stationList = new StationList(mSailingBuddyDatabase.stationsDAO().getStationByID(favorite.getStationId()));
                stationList.setFavorite(true);
                stationLists.add(stationList);
            }
            return stationLists;
        }

        @Override
        protected void onPostExecute(List<StationList> favoriteStations) {
            mFavoriteStationList = favoriteStations;
            setupSearchStationsRecycler();
        }
    }

    private void setupSearchStationsRecycler() {
        if (mFavoriteStationList != null && mFavoriteStationList.size() != 0 && !mFavoriteStationList.isEmpty()) {
            mStationsListRecyclerAdapter = new StationsListRecyclerAdapter(this, mFavoriteStationList);
            mStationsListRecyclerAdapter.setClickListener(this);
            mFavoritesRecycler.setLayoutManager(new LinearLayoutManager(this));
            mFavoritesRecycler.setAdapter(mStationsListRecyclerAdapter);
            mFavoritesRecycler.setHasFixedSize(true);
            mStationsListRecyclerAdapter.notifyDataSetChanged();
        } else {
            mFavoritesRecycler.setVisibility(View.GONE);
            mNoFavoritesMessage.setVisibility(View.VISIBLE);
            Toast.makeText(this, getResources().getText(R.string.wiget_no_favorites_message), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

