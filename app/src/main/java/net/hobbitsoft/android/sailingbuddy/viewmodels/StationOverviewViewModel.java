package net.hobbitsoft.android.sailingbuddy.viewmodels;

import android.app.Application;

import net.hobbitsoft.android.sailingbuddy.data.Moon;
import net.hobbitsoft.android.sailingbuddy.data.Sun;
import net.hobbitsoft.android.sailingbuddy.data.Tide;
import net.hobbitsoft.android.sailingbuddy.data.Wind;
import net.hobbitsoft.android.sailingbuddy.database.StationDetails;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class StationOverviewViewModel extends AndroidViewModel implements Observable {

    private PropertyChangeRegistry propertyChangeRegistry = new PropertyChangeRegistry();

    private StationDetailVeiwModelRepository mStationDetailVeiwModelRepository;
    private String mStationId;
    private LiveData<StationDetails> mStationDetails;

    public Observer<LiveData<StationDetails>> stationDetailsObserver = new Observer<LiveData<StationDetails>>() {
        @Override
        public void onChanged(LiveData<StationDetails> stationDetailsLiveData) {
            mStationDetails = stationDetailsLiveData;
            notifyChange();
        }
    };

    void notifyChange() {
        propertyChangeRegistry.notifyCallbacks(this, 0, null);
    }

    public void setStationId(String stationId) {
        this.mStationId = mStationId;
        mStationDetailVeiwModelRepository.setStationId(stationId);
    }

    public StationOverviewViewModel(Application application) {
        super(application);
        mStationDetailVeiwModelRepository = StationDetailVeiwModelRepository.getInstance(application.getApplicationContext());
    }

    public LiveData<StationDetails> getStationDetails() {
        return mStationDetailVeiwModelRepository.getStationDetails();
    }

    public void setStationDetails(LiveData<StationDetails> stationDetails) {
        mStationDetails = stationDetails;
        stationDetailsObserver.onChanged(stationDetails);
        notifyChange();
    }

    @Bindable
    public String getCurrentCoordinatesString() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getCurrentTemperatureString();
        } else {
            return "";
        }
    }

    @Bindable
    public String getLastUpdateTime() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getLastUpdateTime().toString();
        } else {
            return "";
        }
    }

    @Bindable
    public Moon getMoon() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getMoon();
        } else {
            return new Moon();
        }
    }

    @Bindable
    public Sun getSun() {
        if (mStationDetails != null) {

            return mStationDetails.getValue().getSun();
        } else {
            return new Sun();
        }
    }

    @Bindable
    public Tide getFirstTide() {
        if (mStationDetails != null) {

            return mStationDetails.getValue().getFirstTide();
        } else {
            return new Tide();
        }
    }

    @Bindable
    public Tide getSecondTide() {
        if (mStationDetails != null) {

            return mStationDetails.getValue().getSecondTide();
        } else {
            return new Tide();
        }
    }

    @Bindable
    public Tide getThirdTideDetails() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getThirdTide();
        } else {
            return new Tide();
        }
    }

    @Bindable
    public Tide getFourthideDetails() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getFourthTide();
        } else {
            return new Tide();
        }
    }

    @Bindable
    public String getCurrentTemperatureString() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getCurrentTemperatureString();
        } else {
            return " ";
        }
    }

    @Bindable
    public String getHighTemperatureString() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getHighTemperatureString();
        } else {
            return " ";
        }
    }

    @Bindable
    public String getLowTemperatureString() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getLowTemperatureString();
        } else {
            return " ";
        }
    }

    @Bindable
    public Wind getWind() {
        if (mStationDetails != null) {
            return mStationDetails.getValue().getWind();
        } else {
            return new Wind();
        }
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

    }
}
