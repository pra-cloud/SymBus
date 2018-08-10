package com.bs.tech.symbus;

/**
 * Created by bhumika on 28/3/18.
 */

public class Bus {
    private String busNo="", destn=""; //from QR code
    private double lattitude, longitude, destnLat, destnLong;
    private int nextStop;
    private String isRunning="";


    Bus() {
        busNo=""; destn="";
        lattitude= longitude = destnLat= destnLong= 0.0;
        nextStop= 0; isRunning="";
    }

    Bus(String busNo, double lattitude, double longitude, int nextStop,
        double destnLat, double destnLong, String destn, String isRunning) {
        this.busNo= busNo;
        this.lattitude= lattitude;
        this.longitude= longitude;
        this.nextStop= nextStop;
        this.destnLat= destnLat;
        this.destnLong= destnLong;
        this.destn= destn;
        this.isRunning+= isRunning;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getBusNo() {
        return busNo;
    }

    public void setBusNo(String busNo) {
        this.busNo += busNo;
    }

    public int getNextStop() {
        return nextStop;
    }

    public void setNextStop(int nextStop) {
        this.nextStop = nextStop;
    }

    public double getDestnLat() {
        return destnLat;
    }

    public void setDestnLat(double destnLat) {
        this.destnLat = destnLat;
    }

    public double getDestnLong() {
        return destnLong;
    }

    public void setDestnLong(double destnLong) {
        this.destnLong = destnLong;
    }

    public String getDestn() {
        return destn;
    }

    public void setDestn(String destn) {
        this.destn = destn;
    }

    public String getIsRunning() { return isRunning; }

    public void setIsRunning(String isRunning) { this.isRunning += isRunning; }
}
