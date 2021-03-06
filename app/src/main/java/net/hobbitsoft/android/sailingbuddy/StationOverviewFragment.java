/*
 * Copyright (c) 2018.  HobbitSoft - Kevin Heath High
 */

package net.hobbitsoft.android.sailingbuddy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.hobbitsoft.android.sailingbuddy.database.StationDetails;
import net.hobbitsoft.android.sailingbuddy.databinding.StationOverviewFragmentBinding;
import net.hobbitsoft.android.sailingbuddy.utilities.InstanceStateKeys;
import net.hobbitsoft.android.sailingbuddy.utilities.IntentKeys;
import net.hobbitsoft.android.sailingbuddy.viewmodels.StationOverviewViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * A fragment representing a single StationData detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link StationDetailActivity}
 * on handsets.
 */
public class StationOverviewFragment extends Fragment {

    private static final String TAG = StationOverviewFragment.class.getSimpleName();

    private String mStationId;
    private StationOverviewViewModel mStationOverviewViewModel;
    private LiveData<StationDetails> mStationDetails;
    private StationOverviewFragmentBinding mStationOverviewFragmentBinding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StationOverviewFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "Activity Created");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(InstanceStateKeys.STATION_ID)) {
            mStationId = savedInstanceState.getString(InstanceStateKeys.STATION_ID);
        } else {
            mStationId = getArguments().getString(IntentKeys.OVERVIEW_STATION_ID);
        }
        Log.d(TAG, "Station ID: " + mStationId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "CreateView");
        mStationOverviewFragmentBinding =
                DataBindingUtil.inflate(inflater, R.layout.station_overview_fragment, container, false);

        mStationOverviewViewModel = new StationOverviewViewModel(getActivity().getApplication());
        //mStationOverviewViewModel.setStationId(mStationId);
        mStationOverviewFragmentBinding.setLifecycleOwner(this);
        mStationOverviewFragmentBinding.setStationDetailsModel(mStationOverviewViewModel);
        mStationOverviewViewModel.getStationDetails(mStationId).observe(this, new Observer<StationDetails>() {
                @Override
                public void onChanged(StationDetails stationDetails) {
                    Log.d(TAG, "Observer Changed");
                    if (stationDetails != null) {
                        populateOverview(stationDetails);
                    }
                }
            });
        return mStationOverviewFragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(InstanceStateKeys.STATION_ID, mStationId);
    }

    private void populateOverview(StationDetails stationDetails) {

        ((MainActivity) getActivity()).updateSelectedStation(stationDetails.getStationId(), stationDetails.getStationName(), stationDetails.getForecastStation());
        mStationOverviewFragmentBinding.coordinates.setText(stationDetails.getStringCoordinates().toString());
        mStationOverviewFragmentBinding.lastRefreshTime.setText(stationDetails.getLastUpdateTime().toString());
        mStationOverviewFragmentBinding.currentTemperature.setText(stationDetails.getCurrentTemperatureString());
        mStationOverviewFragmentBinding.highTemperature.setText(stationDetails.getHighTemperatureString());
        mStationOverviewFragmentBinding.lowTemperature.setText(stationDetails.getLowTemperatureString());
        if (stationDetails.getWind() != null) {
            if (mStationOverviewFragmentBinding.currentWindSpeed.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.currentWindSpeed.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.currentWindSpeed.setText(stationDetails.getWind().getSpeedString());
            if (mStationOverviewFragmentBinding.currentGustSpeed.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.currentGustSpeed.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.currentGustSpeed.setText(stationDetails.getWind().getGustString());
            if (mStationOverviewFragmentBinding.currentWindDirection.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.currentWindDirection.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.currentWindDirection.setText(stationDetails.getWind().getDirectionString());
        } else {
            mStationOverviewFragmentBinding.currentWindSpeed.setVisibility(View.INVISIBLE);
            mStationOverviewFragmentBinding.currentGustSpeed.setVisibility(View.INVISIBLE);
            mStationOverviewFragmentBinding.currentWindDirection.setVisibility(View.INVISIBLE);
        }
        mStationOverviewFragmentBinding.currentTemperature.setText(stationDetails.getCurrentTemperatureString());
        mStationOverviewFragmentBinding.highTemperature.setText(stationDetails.getHighTemperatureString());
        mStationOverviewFragmentBinding.lowTemperature.setText(stationDetails.getLowTemperatureString());
        if (stationDetails.getFirstTide() != null) {
            if (mStationOverviewFragmentBinding.firstTide.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.firstTide.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.firstTide.setText(stationDetails.getFirstTide().toString());
        } else {
            mStationOverviewFragmentBinding.firstTide.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getSecondTide() != null) {
            if (mStationOverviewFragmentBinding.secondTide.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.secondTide.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.secondTide.setText(stationDetails.getSecondTide().toString());
        } else {
            mStationOverviewFragmentBinding.secondTide.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getThirdTide() != null) {
            if (mStationOverviewFragmentBinding.thirdTide.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.thirdTide.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.thirdTide.setText(stationDetails.getFirstTide().toString());
        } else {
            mStationOverviewFragmentBinding.thirdTide.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getFourthTide() != null) {
            if (mStationOverviewFragmentBinding.fourthTide.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.fourthTide.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.fourthTide.setText(stationDetails.getFirstTide().toString());
        } else {
            mStationOverviewFragmentBinding.fourthTide.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getSun().getRiseString() != null) {
            if (mStationOverviewFragmentBinding.sunRise.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.sunRise.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.sunRise.setText(stationDetails.getSun().getRiseString());
        } else {
            mStationOverviewFragmentBinding.sunRise.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getSun().getSetString() != null) {
            if (mStationOverviewFragmentBinding.sunSet.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.sunSet.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.sunSet.setText(stationDetails.getSun().getSetString());
        } else {
            mStationOverviewFragmentBinding.sunSet.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getSun().getTotalHoursString() != null) {
            if (mStationOverviewFragmentBinding.totalSunHours.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.totalSunHours.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.totalSunHours.setText(stationDetails.getSun().getTotalHoursString());
        } else {
            mStationOverviewFragmentBinding.totalSunHours.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getSun().getHoursLeftString() != null) {
            if (mStationOverviewFragmentBinding.hoursTilSunSet.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.hoursTilSunSet.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.hoursTilSunSet.setText(stationDetails.getSun().getHoursLeftString());
        } else {
            mStationOverviewFragmentBinding.hoursTilSunSet.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getMoon().getRiseString() != null) {
            if (mStationOverviewFragmentBinding.moonRise.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.moonRise.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.moonRise.setText(stationDetails.getMoon().getRiseString());
        } else {
            mStationOverviewFragmentBinding.moonRise.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getMoon().getSetString() != null) {
            if (mStationOverviewFragmentBinding.moonSet.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.moonSet.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.moonSet.setText(stationDetails.getMoon().getSetString());
        } else {
            mStationOverviewFragmentBinding.moonSet.setVisibility(View.INVISIBLE);
        }
        if (stationDetails.getMoon().getPercentString() != null) {
            if (mStationOverviewFragmentBinding.moonPhase.getVisibility() == View.INVISIBLE) {
                mStationOverviewFragmentBinding.moonPhase.setVisibility(View.VISIBLE);
            }
            mStationOverviewFragmentBinding.moonPhase.setText(stationDetails.getMoon().getPercentString());
        } else {
            mStationOverviewFragmentBinding.moonPhase.setVisibility(View.INVISIBLE);
        }
    }
}
