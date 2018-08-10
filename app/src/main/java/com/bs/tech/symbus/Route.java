package com.bs.tech.symbus;

/**
 * Created by bhumika on 11/4/18.
 */

public class Route {
    private double fromLat, fromLong, toLat, toLong;

    Route()
    {
        fromLat= fromLong= toLat= toLong= 0.0;
    }

    Route(double fromLat, double fromLong,
          double toLat, double toLong)
    {
        this.fromLat= fromLat;
        this.fromLong= fromLong;
        this.toLat= toLat;
        this.toLong= toLong;
    }

    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double fromLat) {
        this.fromLat = fromLat;
    }

    public double getFromLong() {
        return fromLong;
    }

    public void setFromLong(double fromLong) {
        this.fromLong = fromLong;
    }

    public double getToLat() {
        return toLat;
    }

    public void setToLat(double toLat) {
        this.toLat = toLat;
    }

    public double getToLong() {
        return toLong;
    }

    public void setToLong(double toLong) {
        this.toLong = toLong;
    }
}
