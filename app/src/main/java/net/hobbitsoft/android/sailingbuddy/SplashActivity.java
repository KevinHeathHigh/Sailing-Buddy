/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.database.StationTable;
import net.hobbitsoft.android.sailingbuddy.noaadata.NDBCData;
import net.hobbitsoft.android.sailingbuddy.utilities.CoordinateUtils;
import net.hobbitsoft.android.sailingbuddy.utilities.DistanceSort;
import net.hobbitsoft.android.sailingbuddy.utilities.IntentKeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int PERMISSION_ACCESS_LOCATION = 181;
    private static final int CLOSEST_BUOY_LIST_SIZE = 10;


    private static boolean hasLocation = false;
    private static DecimalCoordinates currentDecimalCoordinates = new DecimalCoordinates();

    private static List<StationTable> stationTableList;
    private static String closestStationId;
    private static String mClosestStationName;
    private static Boolean mHasForecast;
    private static String mForecastSStation;
    private static List<DistanceSort> closestStationList = new ArrayList<>();

    private static SailingBuddyDatabase sailingBuddyDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            //https://stackoverflow.com/questions/22422691/android-app-restarts-from-splash-screen-when-opened-by-clicking-app-icon-only-fi
            finish();
            return;
        }
        sailingBuddyDatabase = SailingBuddyDatabase.getInstance(getApplicationContext());
        getStations();
    }

    private void launchStationOverviewActivity() {

        Log.d(TAG, "Getting ready to Launch StationDetailActivity");
        Log.d(TAG, "Has Location: " + String.valueOf(hasLocation));
        Log.d(TAG, "Closest Station: " + closestStationId);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(IntentKeys.CLOSEST_STATION_ID, closestStationId);
        intent.putExtra(IntentKeys.HAS_LOCATION, hasLocation);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IntentKeys.CLOSEST_STATION_LIST, (ArrayList<DistanceSort>) closestStationList);
        if (hasLocation) {
            bundle.putParcelable(IntentKeys.CURRENT_LOCATION, currentDecimalCoordinates);
        }
        intent.putExtra(IntentKeys.CLOSEST_STATION_LIST, bundle);
        startActivity(intent);
        this.finish();

    }

    private void getStations() {
        Log.d(TAG, "Retrieving Stations Table and Owners in background");
        new RetrieveStations().execute();

    }

    private void getLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_LOCATION);
        } else {
            getLocation();
        }
    }

    private class RetrieveStations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "Start downloading Stations and Owners");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                NDBCData.getStationsTable(getApplicationContext(), sailingBuddyDatabase);
                NDBCData.getStationOwnerTable(getApplicationContext(), sailingBuddyDatabase);
                stationTableList = sailingBuddyDatabase.stationsDAO().getAllStations();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "Stations and Owners Dowloaded");
            getLocationPermissions();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void findClosestBuoy() {

        /*
         * We need to get the all the coordinates from the buoys, compare them to our current
         * coordinates, create a list of distances and then sort by closest
         *
         *  TODO: This is a very long processes, need to research how to speed this up
         *  maybe a map/apply type function on the List, something else do the calculations
         *  as a property of the pojo and then sort
         *
         *  TODO: Make the Buoy Icon "tick-tock" while loading - gives the sense that something is happening
         *  though this could be done with a progress bar in the short run
         *
         *  TODO: Add foghorn at startup :)  Toot Toot
         */

        Log.d(TAG, "Starting to sort the Station table by distance.");
        long startTime = System.currentTimeMillis();

        List<DistanceSort> currentDistanceSortList = new ArrayList<>();

        for (StationTable station : stationTableList) {
            String stationId = station.getStationId();
            Double distance = CoordinateUtils.getDistance(currentDecimalCoordinates.getLatLng(),
                    station.getDecimalCoordinates().getLatLng());
            DistanceSort distanceSort = new DistanceSort(stationId, distance);
            currentDistanceSortList.add(distanceSort);
        }
        Log.d(TAG, "Processed all distances. Now sotring by closest");
        long processTime = System.currentTimeMillis();
        long lapDuration = (processTime - startTime);
        Log.d(TAG, "Processing all distances took " + String.valueOf(lapDuration));
        // Sort buoys by distance (ascending)
        Collections.sort(currentDistanceSortList);


        Log.d(TAG, "Sorted.");
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        Log.d(TAG, "Calculating closest bouys took " + String.valueOf(duration)
        );

        // Save off a limited number of Stations
        for (int i = 0; i < CLOSEST_BUOY_LIST_SIZE; i++) {
            closestStationList.add(currentDistanceSortList.get(i));
        }
        closestStationId = closestStationList.get(0).getStationId();

        Log.d(TAG, "Closest Station: " + closestStationId);

        launchStationOverviewActivity();
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG, "Getting Location");
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Recieved new Location data.");
                hasLocation = true;

                currentDecimalCoordinates.setLatitude(location.getLatitude());
                currentDecimalCoordinates.setLongitude(location.getLongitude());

                Log.d(TAG, "Current Latitude: " + String.valueOf(currentDecimalCoordinates.getLatitude()));
                Log.d(TAG, "Current Longitude: " + String.valueOf(currentDecimalCoordinates.getLongitude()));
                //This can be problematic - need to make sure we just keep going after exception
                // We just need to get this once
                locationManager.removeUpdates(this);
                findClosestBuoy();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Location Status Changed");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "Location Provider Enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "Location Provider Disabled: " + provider);
                hasLocation = false;
                launchStationOverviewActivity();
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 10, locationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_ACCESS_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasLocation = true;
                    getLocation();
                } else {
                    hasLocation = false;
                }
                break;
        }


    }
}
