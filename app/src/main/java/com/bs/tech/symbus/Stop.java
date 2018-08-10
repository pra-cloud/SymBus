package com.bs.tech.symbus;

/**
 * Created by bhumika on 5/4/18.
 */

public class Stop {
    private String name= "";
    private double lattitude, longitude;
    private int stopNo;

    Stop() {
        this.lattitude= 0; this.longitude= 0; this.stopNo= 0;
    }

    Stop(String name, double lattitude, double longitude, int stopNo)
    {
        this.name= name;
        this.lattitude= lattitude;
        this.longitude= longitude;
        this.stopNo= stopNo;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStopNo() {
        return stopNo;
    }

    public void setStopNo(int stopNo) {
        this.stopNo = stopNo;
    }
}
