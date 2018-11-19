/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.database.StationDetails;
import net.hobbitsoft.android.sailingbuddy.utilities.AppExecutors;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class StationOverviewWidgetService extends IntentService {

    public static final String ACTION_GET_STATION_OVERVIEW = "net.hobbitsoft.android.sailingbuddy.action.ACTION_GET_STATION_OVERVIEW";

    public static final String EXTRA_STATION_ID = "net.hobbitsoft.android.sailingbuddy.extra.STATION_ID";
    public static final String EXTRA_APP_WIDGET_ID = "net.hobbitsoft.android.sailingbuddy.extra.APP_WIDGET_ID";

    SailingBuddyDatabase mSailingBuddyDatabase;

    public StationOverviewWidgetService() {
        super("StationOverviewWidgetService");
        mSailingBuddyDatabase = SailingBuddyDatabase.getInstance(this);
    }

    /**
     * Starts this service to perform action Get Station Overview with the given Station ID. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetStationOverview(Context context, String stationId, int appWidgetId) {
        Intent intent = new Intent(context, StationOverviewWidgetService.class);
        intent.setAction(ACTION_GET_STATION_OVERVIEW);
        intent.putExtra(EXTRA_STATION_ID, stationId);
        intent.putExtra(EXTRA_APP_WIDGET_ID, appWidgetId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_STATION_OVERVIEW.equals(action)) {
                final String stationId = intent.getStringExtra(EXTRA_STATION_ID);
                final int appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, 0);
                handleActionGetStationOverview(stationId, appWidgetId);
            }
        }
    }

    /**
     * Handle action Get Station Overview in the provided background thread with the provided
     * parameters.
     *
     * @param stationId
     */
    private void handleActionGetStationOverview(final String stationId, final int appWidgetId) {
        AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                StationDetails stationDetails = mSailingBuddyDatabase.stationCacheDAO().getStaionFromCache(stationId);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                StationOverviewWidget.notifyAppWidget(getApplicationContext(), appWidgetManager, stationDetails, appWidgetId);
            }
        });
    }
}
