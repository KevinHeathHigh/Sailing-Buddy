/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import net.hobbitsoft.android.sailingbuddy.database.StationDetails;

/**
 * Implementation of App example_appwidget_preview functionality.
 * App example_appwidget_preview Configuration implemented in {@link StationOverviewWidgetConfigureActivity StationOverviewWidgetConfigureActivity}
 */
public class StationOverviewWidget extends AppWidgetProvider {

    private static final String TAG = StationOverviewWidget.class.getSimpleName();
    private static Context mContext;

    static void notifyAppWidget(Context context, AppWidgetManager appWidgetManager, StationDetails stationDetails, int appWidgetId) {

        if (stationDetails != null) {
            Log.d(TAG, "Notify Widget " + String.valueOf(appWidgetId) + " with Station Details for: " + stationDetails.getStationId());
            RemoteViews views = getRemoteViews(context);
            populateOverview(views, stationDetails);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            Log.d(TAG, "Notify Widget " + String.valueOf(appWidgetId) + " with no Station Details");
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        mContext = context;
        Log.d(TAG, "Update App Widget " + String.valueOf(appWidgetId));
        String stationId = StationOverviewWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = getRemoteViews(context);
        Intent intent = new Intent(context, StationOverviewWidgetService.class);
        intent.putExtra(StationOverviewWidgetService.EXTRA_STATION_ID, stationId);
        intent.putExtra(StationOverviewWidgetService.EXTRA_APP_WIDGET_ID, appWidgetId);
        intent.setAction(StationOverviewWidgetService.ACTION_GET_STATION_OVERVIEW);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static RemoteViews getRemoteViews(Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.station_overview_widget);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "On Update Widget " + String.valueOf(appWidgetId));
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            StationOverviewWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static void populateOverview(RemoteViews views, StationDetails stationDetails) {
        views.setTextViewText(R.id.widget_station_title, stationDetails.getStationId() + " - " + stationDetails.getStationName());
        views.setTextViewText(R.id.coordinates, stationDetails.getStringCoordinates().toString());
        views.setTextViewText(R.id.last_refresh_time, stationDetails.getLastUpdateTime().toString());
        views.setTextViewText(R.id.current_temperature, stationDetails.getCurrentTemperatureString());
        views.setTextViewText(R.id.high_temperature, stationDetails.getHighTemperatureString());
        views.setTextViewText(R.id.low_temperature, stationDetails.getLowTemperatureString());
        if (stationDetails.getWind() != null) {
            views.setTextViewText(R.id.current_wind_speed, stationDetails.getWind().getSpeedString());
            views.setTextViewText(R.id.current_gust_speed, stationDetails.getWind().getGustString());
            views.setTextViewText(R.id.current_wind_direction, stationDetails.getWind().getDirectionString());
        }
        if (stationDetails.getFirstTide() != null) {
            views.setTextViewText(R.id.first_tide, stationDetails.getFirstTide().toString());
        }
        if (stationDetails.getSecondTide() != null) {
            views.setTextViewText(R.id.second_tide, stationDetails.getSecondTide().toString());
        }
        if (stationDetails.getThirdTide() != null) {
            views.setTextViewText(R.id.third_tide, stationDetails.getFirstTide().toString());
        }
        if (stationDetails.getFourthTide() != null) {
            views.setTextViewText(R.id.fourth_tide, stationDetails.getFirstTide().toString());
        }
    }
}

