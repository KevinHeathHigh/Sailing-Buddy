package net.hobbitsoft.android.sailingbuddy.data;

import net.hobbitsoft.android.sailingbuddy.utilities.ConversionUtils;
import net.hobbitsoft.android.sailingbuddy.utilities.DateUtils;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;
import org.shredzone.commons.suncalc.MoonTimes;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Always calculate Moon times in real time so that it is always most up to date.
 * https://shredzone.org/maven/commons-suncalc/
 */
public class Moon {

    private Date rise;
    private Date set;
    private int percent;
    private String phase;
    private double latitude;
    private double longitude;

    public Moon(Date rise, Date set, int percent, String phase, DecimalCoordinates decimalCoordinates) {
        this.rise = rise;
        this.set = set;
        this.percent = percent;
        this.phase = phase;
        this.latitude = decimalCoordinates.getLatitude();
        this.longitude = decimalCoordinates.getLongitude();
    }

    public Moon() {
    }

    public Moon(DecimalCoordinates decimalCoordinates) {
        this.latitude = decimalCoordinates.getLatitude();
        this.longitude = decimalCoordinates.getLongitude();
    }

    public Date getRise() {
        MoonTimes moonTimes = MoonTimes.compute().on(ConversionUtils.returnTodayMidnight()).at(latitude, longitude).execute();
        this.rise = moonTimes.getRise();
        return this.rise;
    }

    public String getRiseString() {
        if (getRise() != null) {
            return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(getRise());
        } else {
            return null;
        }
    }

    /*
    public void setRise(Date rise) {
        this.rise = rise;
    }
    */
    public Date getSet() {
        MoonTimes moonTimes = MoonTimes.compute().on(ConversionUtils.returnTodayMidnight()).at(latitude, longitude).execute();
        this.set = moonTimes.getSet();
        return this.set;
    }


    public String getSetString() {
        return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(getSet());
    }

    /*
    public void setSet(Date set) {
        this.set = set;
    }
    */

    public int getPercent() {
        MoonIllumination moonIllumination = MoonIllumination.compute().on(ConversionUtils.returnTodayMidnight()).execute(); // at(latitude, longitude).execute();
        this.percent = (int) (moonIllumination.getFraction() * 100);
        return this.percent;
    }

    public String getPercentString() {
        return getPercent() + "%";
    }

    /*
    public void setPercent(int percent) {
        this.percent = percent;
    }
    */

    public String getPhase() {
        MoonPhase moonPhase = MoonPhase.compute().on(new Date(System.currentTimeMillis())).execute();
        this.phase = moonPhase.toString();
        return phase;
    }

    /*
    public void setPhase(String phase) {
        this.phase = phase;
    }
    */

}
