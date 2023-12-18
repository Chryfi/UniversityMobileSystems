package com.chryfi.jogjoy.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Run {
    private long id = -1;
    private float goal;
    private String username;
    /**
     * This list should always be in a sorted state by timestamp and never contain
     * multiple equal timestamps.
     */
    private LinkedHashMap<Long, GPSPoint> gpspoints = new LinkedHashMap<>();

    public Run(long id, float goal, String username) {
        this.id = id;
        this.goal = goal;
        this.username = username;
    }

    public Run(float goal, String username) {
        this.goal = goal;
        this.username = username;
    }

    public Run(float goal, String username, Collection<GPSPoint> points) {
        this.goal = goal;
        this.username = username;
        this.addGPSpoints(points);
    }

    public Run(long id, float goal, String username, Collection<GPSPoint> points) {
        this.id = id;
        this.goal = goal;
        this.username = username;
        this.addGPSpoints(points);
    }

    public void setGPSpoints(List<GPSPoint> points) {
        this.gpspoints.clear();
        this.addGPSpoints(points);
    }

    /**
     * Adds the gps point, if the same timestamp already existed it will be overwritten.
     * This method then sorts the points by the timestamp ascending.
     * @param point
     */
    public void addGPSpoint(GPSPoint point) {
        this.gpspoints.put(point.getTimestamp(), point);
        this.sortPoints();
    }

    /**
     * Add the given points to the run. GPSpoints with equal timestamps will be overwritten
     * by the supplied point. This method then sorts the points by the timestamps ascending.
     * @param points
     */
    public void addGPSpoints(Collection<GPSPoint> points) {
        for (GPSPoint point : points) {
            this.gpspoints.put(point.getTimestamp(), point);
        }

        this.sortPoints();
    }

    public Optional<GPSPoint> getPointByTimestamp(long timestamp) {
        return Optional.ofNullable(this.gpspoints.get(timestamp));
    }

    /**
     * Sort the GPS points in ascending order by the timestamp.
     */
    private void sortPoints() {
        /* sort in place by timestamp in ascending order */
        this.gpspoints = this.gpspoints.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public List<GPSPoint> getGpspoints() {
        return new ArrayList<>(this.gpspoints.values());
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getGoal() {
        return this.goal;
    }

    public void setGoal(float goal) {
        this.goal = goal;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Run)) return false;
        Run run = (Run) o;
        return Float.compare(run.goal, this.goal) == 0
                && run.id == this.id
                && this.username.equals(run.username)
                && this.gpspoints.equals(run.gpspoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.goal, this.username, this.gpspoints);
    }
}
