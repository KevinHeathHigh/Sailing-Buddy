package net.hobbitsoft.android.sailingbuddy;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;

import android.widget.TextView;

import net.hobbitsoft.android.sailingbuddy.database.Forecast;
import net.hobbitsoft.android.sailingbuddy.database.SailingBuddyDatabase;
import net.hobbitsoft.android.sailingbuddy.noaadata.NDBCData;
import net.hobbitsoft.android.sailingbuddy.utilities.InstanceStateKeys;
import net.hobbitsoft.android.sailingbuddy.utilities.IntentKeys;

import java.io.IOException;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Color.BLACK;

public class ForecastActivity extends AppCompatActivity {

    private static Forecast mForecast;
    private String mStationId;
    private String mStationName;

    @BindView(R.id.forecast)
    TextView forecastView;

    @BindView(R.id.forecast_toolbar)
    Toolbar mForecastToolbar;

    @BindView(R.id.forecast_refresh)
    SwipeRefreshLayout mForecastRefresh;

    private static SailingBuddyDatabase sailingBuddyDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        ButterKnife.bind(this);

        sailingBuddyDatabase = SailingBuddyDatabase.getInstance(getApplicationContext());

        setSupportActionBar(mForecastToolbar);
        mForecastToolbar.setTitle(getTitle());
        Objects.requireNonNull(mForecastToolbar.getOverflowIcon()).setColorFilter(BLACK, PorterDuff.Mode.SRC_ATOP);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mForecast = new Forecast(intent.getStringExtra(IntentKeys.FORECAST_STATION));
            mStationId = intent.getStringExtra(IntentKeys.SELECTED_STATION_ID);
            mStationName = intent.getStringExtra(IntentKeys.SELECTED_STATION_NAME);
            fetchForecast();
        } else {
            mStationId = savedInstanceState.getString(InstanceStateKeys.SELECTED_STATION_ID);
            mStationName = savedInstanceState.getString(InstanceStateKeys.SELECTED_STATION_NAME);
            mForecast = (Forecast) savedInstanceState.getSerializable(InstanceStateKeys.FORECAST);
            updateForecast();
        }

        setTitleText();

        forecastView.setMovementMethod(new ScrollingMovementMethod());
        mForecastRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchForecast();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(InstanceStateKeys.SELECTED_STATION_ID, mStationId);
        outState.putString(InstanceStateKeys.SELECTED_STATION_NAME, mStationName);
        outState.putSerializable(InstanceStateKeys.FORECAST, mForecast);
    }

    private void setTitleText() {
        String title = "";
        if (mStationId != null && mStationName != null) {
            title = mStationId + " - " + mStationName;
        } else if (mStationId != null) {
            title = mStationId;
        } else if (mStationName != null) {
            title = mStationName;
        }
        mForecastToolbar.setTitle(title);
    }

    private void fetchForecast() {
        new RetrieveForecast().execute();
    }

    private class RetrieveForecast extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mForecastRefresh.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                NDBCData.getForecast(getApplicationContext(), mForecast.getForecastStation(), sailingBuddyDatabase);
                if (sailingBuddyDatabase.forecastCacheDAO().forecastCached(mForecast.getForecastStation()))
                    mForecast.setForecast(sailingBuddyDatabase.forecastCacheDAO().getForecastByForecastStation(mForecast.getForecastStation()).getForecast());
                else {
                    mForecast.setForecast("No Forecast Found for: " + mStationId + " - " + mStationName + " with Forecast Station: " + mForecast.getForecastStation());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateForecast();
        }
    }

    private void updateForecast() {
        if (mForecast.getForecast() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                forecastView.setText(Html.fromHtml(mForecast.getForecast(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                forecastView.setText(Html.fromHtml(mForecast.getForecast()));
            }
        } else {
            this.finish();
        }
        mForecastRefresh.setRefreshing(false);
        setTitleText();
    }
}

