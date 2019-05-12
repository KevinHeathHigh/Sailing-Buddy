package net.hobbitsoft.android.sailingbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.hobbitsoft.android.sailingbuddy.data.DecimalCoordinates;
import net.hobbitsoft.android.sailingbuddy.utilities.IntentKeys;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //TODO: Have icon for showing current location
    //mMap.setMyLocationEnabled(); ?
    //TODO: Have icon to show nearby stations

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int ZOOM_LEVEL = 12;
    private String mStationId;
    private String mStationName;
    private DecimalCoordinates mDecimalCoordinates = new DecimalCoordinates();
    Marker marker;
    LatLng buoyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolBar = (Toolbar) findViewById(R.id.maps_toolbar);
        toolBar.setTitle(getTitle());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        mStationId = intent.getStringExtra(IntentKeys.SELECTED_STATION_ID);
        mStationName = intent.getStringExtra(IntentKeys.SELECTED_STATION_NAME);
        Bundle bundle = new Bundle();
        bundle = intent.getBundleExtra(IntentKeys.SELECTED_LOCATION);
        mDecimalCoordinates = bundle.getParcelable(IntentKeys.SELECTED_LOCATION);
        buoyLocation = mDecimalCoordinates.getLatLng();
        toolBar.setTitle(mStationId + " - " + mStationName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            marker.remove();
            mMap.clear();
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN | GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(buoyLocation, ZOOM_LEVEL));
        marker = mMap.addMarker(new MarkerOptions().position((buoyLocation)));
        marker.setVisible(false);
        marker.setTitle(mStationId);
        marker.setSnippet(mStationName);
        marker.showInfoWindow();
        marker.setVisible(true);
    }


}

