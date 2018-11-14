package net.hobbitsoft.android.sailingbuddy.data;

import net.hobbitsoft.android.sailingbuddy.utilities.CoordinateUtils;

public class Wind {
    private String mDirectionDegrees;
    private String mDirection;
    private double mSpeed;
    private String mMeasurement;
    private double mGust;

    public Wind(String directionDegrees, String direction, double speed, String measurement, double gust) {
        this.mDirectionDegrees = directionDegrees;
        this.mDirection = direction;
        this.mSpeed = speed;
        this.mMeasurement = measurement;
        this.mGust = gust;
    }

    public Wind() {
    }

    public String getDirectionDegrees() {
        return mDirectionDegrees;
    }

    public void setDirectionDegrees(String directionDegrees) {
        this.mDirectionDegrees = directionDegrees;
    }

    public String getDirection() {
        return mDirection;
    }

    public String getDirectionString() {
        return mDirection + " (" + mDirectionDegrees + CoordinateUtils.DEGREE_SYMBOL + ")";
    }

    public void setDirection(String direction) {
        this.mDirection = direction;
    }

    public double getSpeed() {
        return mSpeed;
    }

    public String getSpeedString() {
        return String.valueOf(mSpeed) + " " + getMeasurement();
    }

    public void setSpeed(double speed) {
        this.mSpeed = speed;
    }

    public String getMeasurement() {
        return mMeasurement;
    }

    public void setMeasurement(String measurement) {
        this.mMeasurement = measurement;
    }

    public double getGust() {
        return mGust;
    }

    public String getGustString() {
        return String.valueOf(mGust) + " " + getMeasurement();
    }

    public void setGust(double gust) {
        this.mGust = gust;
    }
}
