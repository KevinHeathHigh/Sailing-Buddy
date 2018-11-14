package net.hobbitsoft.android.sailingbuddy.data;


public class Wave {

    private double height;
    private double period;
    private String direction;

    public Wave(double height, double period, String direction) {
        this.height = height;
        this.period = period;
        this.direction = direction;
    }

    public Wave() {
    }

    public String getHeightString() {
        return String.valueOf(height);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getPeriodString() {
        return String.valueOf(period);
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
