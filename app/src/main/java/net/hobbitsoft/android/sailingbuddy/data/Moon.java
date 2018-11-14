package net.hobbitsoft.android.sailingbuddy.data;

import net.hobbitsoft.android.sailingbuddy.utilities.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Moon {

    private Date rise;
    private Date set;
    private int percent;
    private String phase;

    public Moon(Date rise, Date set, int percent, String phase) {
        this.rise = rise;
        this.set = set;
        this.percent = percent;
        this.phase = phase;
    }

    public Moon() {
    }

    public Date getRise() {
        return rise;
    }

    public String getRiseString() {
        if (rise != null) {
            return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(rise);
        } else {
            return " ";
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
            return " ";
        }
    }

    public void setSet(Date set) {
        this.set = set;
    }

    public int getPercent() {
        return percent;
    }

    public String getPercentString() {
        return String.valueOf(percent) + "%";
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

}
