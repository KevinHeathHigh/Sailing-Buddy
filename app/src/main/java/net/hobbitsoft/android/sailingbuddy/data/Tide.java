package net.hobbitsoft.android.sailingbuddy.data;

import net.hobbitsoft.android.sailingbuddy.utilities.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;


public class Tide {

    public static final String HIGH = "High";
    public static final String LOW = "Low";

    private Date time;
    private String type;
    private double height;

    public Tide(Date time, String type, double height) {
        this.time = time;
        this.type = type;
        this.height = height;
    }

    public Tide() {
    }

    public Date getTime() {
        return time;
    }

    public String getTimeString() {
        if (time != null) {
            return new SimpleDateFormat(DateUtils.TIME_FORMAT).format(time);
        } else {
            return new String();
        }
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @NonNull
    @Override
    public String toString() {
        return getTimeString() + " " + getType() + " " + String.valueOf(getHeight());
    }
}
