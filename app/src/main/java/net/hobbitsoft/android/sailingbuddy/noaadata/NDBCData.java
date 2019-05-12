package net.hobbitsoft.android.sailingbuddy.noaadata;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import net.hobbitsoft.android.sailingbuddy.R;
import net.hobbitsoft.android.sailingbuddy.data.Wave;
import net.hobbitsoft.android.sailingbuddy.data.Wind;
import net.hobbitsoft.android.sailingbuddy.database.Forecast;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.database.StationDetails;
import net.hobbitsoft.android.sailingbuddy.database.StationOwner;
import net.hobbitsoft.android.sailingbuddy.database.StationTable;
import net.hobbitsoft.android.sailingbuddy.utilities.PreferencesKeys;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NDBCData {
    /**
     * Created by Kevin Heath High on 3/24/2018.
     * Class used to accessed resources from NDBC
     * National Data Buoy Center - http://www.ndbc.noaa.gov
     */

    private static Context savedContext;

    private static final String TAG = NDBCData.class.getSimpleName();

    //Root folder for and NDBC Data
    private final static String NDBC_DATA_DIR_URL = "https://www.ndbc.noaa.gov/data";
    private final static String STATIONS_DIR = "stations";
    private final static String STATIONS_TABLE = "station_table.txt";
    private final static String STATIONS_OWNERS_TABLE = "station_owners.txt";
    private final static String LATEST_OBSERVATIONS_DIR = "latest_obs";
    private final static String FORECASTS_DIR = "Forecasts";


    private static URL buildStationsURL() {
        Uri stationsURI = Uri.parse(NDBC_DATA_DIR_URL).buildUpon()
                .appendPath(STATIONS_DIR)
                .appendPath(STATIONS_TABLE)
                .build();

        URL url = null;
        try {
            url = new URL(stationsURI.toString());
            Log.i(TAG, "StationTable URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static URL buildStationOwnerURL() {
        Uri stationsURI = Uri.parse(NDBC_DATA_DIR_URL).buildUpon()
                .appendPath(STATIONS_DIR)
                .appendPath(STATIONS_OWNERS_TABLE)
                .build();

        URL url = null;
        try {
            url = new URL(stationsURI.toString());
            Log.i(TAG, "StationTable Owner URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static URL buildLatestObservationsURL(String stationId) {
        Uri stationsURI = Uri.parse(NDBC_DATA_DIR_URL).buildUpon()
                .appendPath(LATEST_OBSERVATIONS_DIR)
                .appendPath(stationId + ".txt")
                .build();

        URL url = null;
        try {
            url = new URL(stationsURI.toString());
            Log.i(TAG, "Latest Observations URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private static URL buildForecastURL(String forecastStation) {
        Uri stationsURI = Uri.parse(NDBC_DATA_DIR_URL).buildUpon()
                .appendPath(FORECASTS_DIR)
                .appendPath(forecastStation + ".html")
                .build();

        URL url = null;
        try {
            url = new URL(stationsURI.toString());
            Log.i(TAG, "Forecasts URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static void getStationsTable(Context context, SailingBuddyDatabase sailingBuddyDatabase)
            throws IOException {
        URL url = buildStationsURL();
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        int count = 0;
        long lastModifiedDate = urlConnection.getLastModified();

        Log.d(TAG, "Connected to Stations Table");
        Log.d(TAG, "Last Modified: " + String.valueOf(lastModifiedDate));

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PreferencesKeys.LAST_MODIFIED_PREFERNCE_FILE, Context.MODE_PRIVATE);

        try {
            // Checking to see if the file is out of date
            if (lastModifiedDate !=
                    sharedPreferences.getLong(PreferencesKeys.STATIONS_TABLE_LAST_MODIFIED_KEY, 0)) {

                Log.d(TAG, "Retrieving lastest stations data");
                String rowData = null;
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);

                while (scanner.hasNextLine()) {
                    rowData = scanner.nextLine();

                    // Rows that start with # are headers or comments and don't contain usable data
                    if (!rowData.startsWith("#")) {
                        StationTable stationTable = new StationTable(rowData);
                        // Update existing rows, or add new rows
                        if (sailingBuddyDatabase.stationsDAO().stationInDatabase(stationTable.getStationId())) {
                            sailingBuddyDatabase.stationsDAO().updateStation(stationTable);
                        } else {
                            sailingBuddyDatabase.stationsDAO().insertStation(stationTable);
                        }
                        count += 1;
                    }
                }

                Log.d(TAG, "Downloaded " + String.valueOf(count) + " records");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(PreferencesKeys.STATIONS_TABLE_LAST_MODIFIED_KEY, lastModifiedDate);
                editor.commit();
                scanner.close();
                in.close();
            } else {
                Log.d(TAG, "Latest station already downloaded");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
    }

    public static void getStationOwnerTable(Context context, SailingBuddyDatabase sailingBuddyDatabase)
            throws IOException {
        URL url = buildStationOwnerURL();
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        int count = 0;
        long lastModifiedDate = urlConnection.getLastModified();

        Log.d(TAG, "Connected to Station Owners Table");
        Log.d(TAG, "Last Modified: " + String.valueOf(lastModifiedDate));

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PreferencesKeys.LAST_MODIFIED_PREFERNCE_FILE, Context.MODE_PRIVATE);

        try {
            // Checking to see if the file is out of date
            if (lastModifiedDate !=
                    sharedPreferences.getLong(PreferencesKeys.STATION_OWNERS_TABLE_LAST_MODIFIED_KEY, 0)) {

                Log.d(TAG, "Retrieving latest station owners data");
                String rowData = null;
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);

                while (scanner.hasNextLine()) {
                    rowData = scanner.nextLine();
                    if (!rowData.startsWith("#")) {
                        StationOwner stationOwner = new StationOwner(rowData);
                        if (sailingBuddyDatabase.stationOwnersDAO().ownerInDatabase(stationOwner.getOwnerCode())) {
                            sailingBuddyDatabase.stationOwnersDAO().updateStationOwner(stationOwner);
                        } else {
                            sailingBuddyDatabase.stationOwnersDAO().insertStationOwner(stationOwner);
                        }
                        count += 1;
                    }
                }

                Log.d(TAG, "Downloaded " + String.valueOf(count) + " records");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(PreferencesKeys.STATION_OWNERS_TABLE_LAST_MODIFIED_KEY, lastModifiedDate);
                editor.commit();
                scanner.close();
                in.close();
            } else {
                Log.d(TAG, "Latest station owners already downloaded");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
    }

    //TODO: This method will need to be updated to call other  methods that get more information
    public static void getLatestObservations(Context context, SailingBuddyDatabase sailingBuddyDatabase, String stationId)
            throws IOException {
        URL url = buildLatestObservationsURL(stationId);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        int responseCode = urlConnection.getResponseCode();

        long lastModifiedDate = urlConnection.getLastModified();

        Log.d(TAG, "Connected to Latest Observation:");
        Log.d(TAG, "Last Modified: " + String.valueOf(lastModifiedDate));

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PreferencesKeys.LAST_MODIFIED_PREFERNCE_FILE, Context.MODE_PRIVATE);


        try {
            // Checking to see if the file is out of date
            if (lastModifiedDate !=
                    sharedPreferences.getLong(PreferencesKeys.LATEST_OBSERVATIONS_LAST_MODIFIED_KEY + "_" + stationId, -1)) {

                //Update Station Details object with data from Station Table and Station Owners Table
                StationTable stationTable = sailingBuddyDatabase.stationsDAO().getStationByID(stationId);
                StationDetails stationDetails = new StationDetails(stationTable);
                stationDetails.setLastUpdateTime(new Date(lastModifiedDate));
                StationOwner stationOwner = sailingBuddyDatabase.stationOwnersDAO().getStationOwnerByOwnerID(stationTable.getOwner());
                stationDetails.updateStationOwner(stationOwner);
                //Test for 404
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Retrieving lastest observation data");
                    String rowData = null;
                    InputStream in = urlConnection.getInputStream();
                    Scanner scanner = new Scanner(in);

                    boolean inWaveSummary = false;
                    boolean inSwell = false;
                    boolean inWindWave = false;

                    while (scanner.hasNextLine()) {
                        rowData = scanner.nextLine();
                        //Parse each row
                        if (rowData.startsWith("Station")) {
                            String[] rowWords = rowData.split(" ");
                            if (!rowWords[1].toUpperCase().equals(stationId.toUpperCase())) {
                                Log.d(TAG, rowWords[1] + " does not equal " + stationId + " exiting route");
                                break;
                            }
                        } else if (rowData.contains("GMT")) {
                            Date date = new SimpleDateFormat("HHmm z MM/dd/yy", Locale.ENGLISH).parse(rowData);
                            if (inWaveSummary) {
                                stationDetails.setWaveSummaryLastUpdate(date);
                            } else {
                                stationDetails.setLastUpdateTime(date);
                            }
                            stationDetails.setLastUpdateTime(date);
                        } else if (rowData.startsWith("Wind")) {
                            Wind wind = new Wind();
                            Log.d(TAG, "Wind String: " + rowData);
                            String[] rowWords = rowData.split(" ");
                            for (String word : rowWords) {
                                if (word.matches("[NEWS]*$")) {
                                    wind.setDirection(word);
                                } else if (word.matches("\\(\\d*.\\)\\,")) {
                                    Pattern pattern = Pattern.compile("\\((\\d*).\\)");
                                    Matcher matcher = pattern.matcher(word);
                                    if (matcher.find()) {
                                        wind.setDirectionDegrees(matcher.group(1));
                                    } else {
                                        wind.setMeasurement(word);
                                    }
                                } else if (word.matches("\\d*\\.\\d*")) {
                                    wind.setSpeed(Double.valueOf(word));
                                } else if (word.matches("kt")) {
                                    wind.setMeasurement(word);
                                }
                            }
                            if (stationDetails.getWind() != null) {
                                stationDetails.getWind().setDirection(wind.getDirection());
                                stationDetails.getWind().setDirectionDegrees(wind.getDirectionDegrees());
                                stationDetails.getWind().setSpeed(wind.getSpeed());
                                stationDetails.getWind().setMeasurement(wind.getMeasurement());
                            } else {
                                stationDetails.setWind(wind);
                            }
                        } else if (rowData.startsWith("Gust")) {
                            String[] rowWords = rowData.split(" ");
                            if (stationDetails.getWind() != null) {
                                stationDetails.getWind().setGust(Double.valueOf(rowWords[1]));
                            } else {
                                Wind wind = new Wind();
                                wind.setGust(Double.valueOf(rowWords[1]));
                                stationDetails.setWind(wind);
                            }
                        } else if (rowData.startsWith("Seas")) {
                            String[] rowWords = rowData.split(" ");
                            if (stationDetails.getSeas() == null) {
                                Wave wave = new Wave();
                                wave.setHeight(Double.valueOf(rowWords[1]));
                                stationDetails.setSeas(wave);
                            } else {
                                stationDetails.getSeas().setHeight(Double.valueOf(rowWords[1]));
                            }
                        } else if (rowData.startsWith("Peak Period")) {
                            String[] rowWords = rowData.split(" ");
                            //Keeping in mind that 'Period' becomesthe second element
                            if (stationDetails.getSeas() == null) {
                                Wave wave = new Wave();
                                //Keeping in mind that 'Period' becomesthe second element
                                wave.setPeriod(Double.valueOf(rowWords[2]));
                                stationDetails.setSeas(wave);
                            } else {
                                stationDetails.getSeas().setPeriod(Double.valueOf(rowWords[2]));
                            }
                        } else if (rowData.startsWith("Pres")) {
                            String[] rowWords = rowData.split(" ");
                            stationDetails.setAirPressure(Double.valueOf(rowWords[1]));
                            if (rowWords.length > 2 && !rowWords[2].isEmpty()) {
                                stationDetails.setAirPressureStatus(rowWords[2]);
                            }
                        } else if (rowData.startsWith("Air Temp")) {
                            String[] rowWords = rowData.split(" ");
                            stationDetails.setCurrentTemperature(Double.valueOf(rowWords[2]));
                        } else if (rowData.startsWith("Water Temp")) {
                            String[] rowWords = rowData.split(" ");
                            stationDetails.setWaterTemperature(Double.valueOf(rowWords[2]));
                        } else if (rowData.startsWith("Dew Point")) {
                            String[] rowWords = rowData.split(" ");
                            stationDetails.setDewPoint(Double.valueOf(rowWords[2]));
                        } else if (rowData.startsWith("Visibility")) {
                            String[] rowWords = rowData.split(" ");
                            stationDetails.setVisibility(Double.valueOf(rowWords[1]));
                        } else if (rowData.startsWith("Wave Summary")) {
                            inWaveSummary = true;
                        } else if (rowData.startsWith("Swell")) {
                            inSwell = true;
                            inWindWave = false;
                            Wave wave = new Wave();
                            String[] rowWords = rowData.split(" ");
                            wave.setHeight(Double.valueOf(rowWords[1]));
                            stationDetails.setSwell(wave);
                        } else if (rowData.startsWith("Wind Wave")) {
                            inWindWave = true;
                            inSwell = false;
                            Wave wave = new Wave();
                            String[] rowWords = rowData.split(" ");
                            wave.setHeight(Double.valueOf(rowWords[1]));
                            stationDetails.setWindWave(wave);
                        } else if (rowData.startsWith("Period")) {
                            String[] rowWords = rowData.split(" ");
                            if (inSwell) {
                                if (stationDetails.getSwell() == null) {
                                    Wave wave = new Wave();
                                    wave.setPeriod(Double.valueOf(rowWords[1]));
                                    stationDetails.setSwell(wave);
                                } else {
                                    stationDetails.getSwell().setPeriod(Double.valueOf(rowWords[1]));
                                }
                            } else if (inWaveSummary) {
                                if (stationDetails.getWindWave() == null) {
                                    Wave wave = new Wave();
                                    wave.setPeriod(Double.valueOf(rowWords[1]));
                                    stationDetails.setWindWave(wave);
                                } else {
                                    stationDetails.getWindWave().setPeriod(Double.valueOf(rowWords[1]));
                                }
                            }
                        } else if (rowData.startsWith("Direction")) {
                            String[] rowWords = rowData.split(" ");
                            if (inSwell) {
                                if (stationDetails.getWindWave() == null) {
                                    Wave wave = new Wave();
                                    wave.setDirection(rowWords[1]);
                                    stationDetails.setSwell(wave);
                                } else {
                                    stationDetails.getSwell().setDirection(rowWords[1]);
                                }
                            } else if (inWaveSummary) {
                                if (stationDetails.getWindWave() == null) {
                                    Wave wave = new Wave();
                                    wave.setDirection(rowWords[1]);
                                    stationDetails.setWindWave(wave);
                                } else {
                                    stationDetails.getWindWave().setDirection(rowWords[1]);
                                }
                            }
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(PreferencesKeys.STATION_OWNERS_TABLE_LAST_MODIFIED_KEY + "_" + stationId, lastModifiedDate);
                    editor.commit();
                    scanner.close();
                    in.close();
                } else {
                    Log.d(TAG, "URL Returned: " + responseCode);
                }
                if (sailingBuddyDatabase.stationCacheDAO().isStationCached(stationId)) {
                    sailingBuddyDatabase.stationCacheDAO().updateStionInCache(stationDetails);
                } else {
                    sailingBuddyDatabase.stationCacheDAO().addStationToCache(stationDetails);
                }
            } else {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Latest observation already downloaded");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
    }

    public static void getForecast(Context context, String forecastStation,
                                     SailingBuddyDatabase sailingBuddyDatabase) throws IOException {
        String forecastString = context.getResources().getString(R.string.no_forecast_found);
        URL url = buildForecastURL(forecastStation);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        int responseCode = urlConnection.getResponseCode();
        long lastModifiedDate = urlConnection.getLastModified();
        boolean isInForecast = false;

        Log.d(TAG, "Connected to Latest Forecast:");
        Log.d(TAG, "Last Modified: " + String.valueOf(lastModifiedDate));

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PreferencesKeys.LAST_MODIFIED_PREFERNCE_FILE, Context.MODE_PRIVATE);

        // Checking to see if the file is out of date
        if (lastModifiedDate !=
                sharedPreferences.getLong(PreferencesKeys.LATEST_FORECAST_LAST_MODIFIED_KEY + "_" + forecastStation, -1)) {
            Log.d(TAG, "Retrieving lastest forecast");
            String rowData = null;
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);

            while (scanner.hasNextLine()) {
                rowData = scanner.nextLine();

                // Rows that start with # are headers or comments and don't contain usable data
                // Per https://stackoverflow.com/questions/46008925/how-to-display-formatted-text-in-textview
                //   wrap in a CDATA object
                if (rowData.equals(context.getResources().getString(R.string.start_of_forecast_section))) {
                    isInForecast = true;
                    forecastString = "";
                    continue;
                }
                if (rowData.equals(context.getResources().getString(R.string.end_of_forecast_section))) {
                    isInForecast = false;
                    continue;
                }
                if (isInForecast) {
                    forecastString +=  rowData + "<br>";
                }
            }

            Forecast forecast = new Forecast(forecastStation, forecastString);
            //Figure out why the database obbject is sometimes null
            if (sailingBuddyDatabase != null) {
                if (!sailingBuddyDatabase.forecastCacheDAO().forecastCached(forecastStation)) {
                    sailingBuddyDatabase.forecastCacheDAO().addForecastToCache(forecast);
                } else {
                    sailingBuddyDatabase.forecastCacheDAO().updateForecastInCache(forecast);
                }
            }

        } else {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Latest forecast already downloaded");
            }
        }
    }
}



