package net.hobbitsoft.android.sailingbuddy.data;


import net.hobbitsoft.android.sailingbuddy.utilities.ConversionUtils;
import net.hobbitsoft.android.sailingbuddy.utilities.DateUtils;

import org.shredzone.commons.suncalc.SunTimes;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Uses https://shredzone.org/maven/commons-suncalc/
 */
public class Sun {

    private Date rise;
    private Date set;
    private double latitude;
    private double longitude;

    public Sun(Date rise, Date set, DecimalCoordinates decimalCoordinates) {
        this.rise = rise;
        this.set = set;
        this.latitude = decimalCoordinates.getLatitude();
        this.longitude = decimalCoordinates.getLongitude();
    }

    public Sun() {
    }

    public Sun(DecimalCoordinates decimalCoordinates) {
        this.latitude = decimalCoordinates.getLatitude();
        this.longitude = decimalCoordinates.getLongitude();
    }

    public Date getRise() {
        SunTimes sunTimes = SunTimes
                .compute()
                .on(ConversionUtils.returnTodayMidnight())
                .oneDay()
                .at(latitude, longitude)
                .execute();
        this.rise = sunTimes.getRise();
        return this.rise;
    }

    public String getRiseString() {
        return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(getRise());
    }

    /*
    public void setRise(Date rise) {
        this.rise = rise;
    }
    */

    public Date getSet() {
        SunTimes sunTimes = SunTimes
                .compute()
                .on(ConversionUtils.returnTodayMidnight()).oneDay()
                .at(latitude, longitude)
                .execute();
        this.set = sunTimes.getSet();
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

    private long getTotalHours() {
        if (getSet() != null || getRise() != null) {
            long rise = getRise().getTime();
            long set = getSet().getTime();
            return set - rise;
        } else {
            return 0;
        }
    }

    // 11:54 Total Hours
    public String getTotalHoursString() {
        long totalhours = getTotalHours();
        if (totalhours != 0) {
            long hours = totalhours / (60 * 60 * 1000) % 24;
            long minutes = totalhours / (60 * 1000) % 60;
            return hours + ":" + minutes + " Total Hours";
        } else {
            return "";
        }
    }

    private long getHoursLeft() {
        if (getSet() != null) {
            return getSet().getTime() - new Date(System.currentTimeMillis()).getTime();
        } else {
            return 0;
        }
    }

    // 08:07 Hours Left
    public String getHoursLeftString() {
        long hoursleft = getHoursLeft();
        if (hoursleft > 0) {
            long hours = hoursleft / (60 * 60 * 1000) % 24;
            long minutes = hoursleft / (60 * 1000) % 60;
            return hours + ":" + minutes + "  Hours Til Sunset";
        } else {
            return "";
        }
    }
}
