package net.hobbitsoft.android.sailingbuddy.data;

import net.hobbitsoft.android.sailingbuddy.utilities.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Sun {

    private Date rise;
    private Date set;

    public Sun(Date rise, Date set) {
        this.rise = rise;
        this.set = set;
    }

    public Sun() {
    }

    public Date getRise() {
        return rise;
    }

    public String getRiseString() {
        if (rise != null) {
            return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(rise);
        } else {
            return new String();
        }
    }

    public void setRise(Date rise) {
        this.rise = rise;
    }

    public Date getSet() {
        return set;
    }

    public String getSetString() {

        if (set != null) {
            return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(set);
        } else {
            return new String();
        }
    }

    public void setSet(Date set) {
        this.set = set;
    }

    private Date getTotalHours() {
        if (set != null || rise != null) {
            return new Date(set.getTime() - rise.getTime());
        } else {
            return null;
        }
    }

    // 11:54 Total Hours
    public String getTotalHoursString() {
        if (getTotalHours() != null) {
            return new SimpleDateFormat(DateUtils.HOURS_FORMAT).format(getTotalHours()) + " Total Hours";
        } else {
            return new String();
        }
    }

    private Date getHoursLeft() {
        Date currentTime = new Date();
        if (set != null) {
            return new Date(set.getTime() - currentTime.getTime());
        } else {
            return null;
        }
    }

    // 08:07 Hours Left
    public String getHoursLeftString() {
        if (getHoursLeft() != null) {
            return new SimpleDateFormat(DateUtils.HOURS_FORMAT).format(getHoursLeft()) + " Hours Left";
        } else {
            return new String();
        }
    }
}
