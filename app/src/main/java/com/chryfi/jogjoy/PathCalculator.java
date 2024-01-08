package com.chryfi.jogjoy;

import com.chryfi.jogjoy.data.GPSPoint;

import java.util.Collection;
import java.util.List;

public class PathCalculator {
    private static final double EARTH_RADIUS = 6371000;

    /**
     * Calculates the distance on a sphere with longitude and latitude coordinates.
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return distance in meters between two points on a sphere.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * @param coordinates list of gps points
     * @return total distance in meters of all spherical coordinates.
     */
    public static double calculatePathLength(List<GPSPoint> coordinates) {
        double totalLength = 0;

        for (int i = 0; i < coordinates.size() - 1; i++) {
            GPSPoint p0 = coordinates.get(i);
            GPSPoint p1 = coordinates.get(i + 1);
            double distance = haversine(p0.getLatitude(), p0.getLongitude(), p1.getLatitude(), p1.getLongitude());
            totalLength += distance;
        }

        return totalLength;
    }
}
