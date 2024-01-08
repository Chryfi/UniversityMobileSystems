package com.chryfi.jogjoy.data;

import java.util.Objects;

public class GPSPoint {
    private long runid;
    private long timestamp;
    private double longitude;
    private double latitude;

    public GPSPoint(long runid, long timestamp, double longitude, double latitude) {
        this.runid = runid;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getRunid() {
        return this.runid;
    }

    public void setRunid(long runid) {
        this.runid = runid;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GPSPoint)) return false;
        GPSPoint gpsPoint = (GPSPoint) o;
        return this.runid == gpsPoint.runid
                && this.timestamp == gpsPoint.timestamp
                && Double.compare(gpsPoint.longitude, this.longitude) == 0
                && Double.compare(gpsPoint.latitude, this.latitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.runid, this.timestamp, this.longitude, this.latitude);
    }
}
