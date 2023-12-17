package com.chryfi.jogjoy.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Run {
    private Optional<Long> id = Optional.empty();
    private float goal;
    private String username;
    private final List<GPSPoint> gpspoints = new ArrayList<>();

    public Run(long id, float goal, String username) {
        this.id = Optional.of(id);
        this.goal = goal;
        this.username = username;
    }

    public Run(float goal, String username) {
        this.goal = goal;
        this.username = username;
    }

    public Run(float goal, String username, List<GPSPoint> points) {
        this.goal = goal;
        this.username = username;
        this.gpspoints.addAll(points);
    }

    public Run(long id, float goal, String username, List<GPSPoint> points) {
        this.id = Optional.of(id);
        this.goal = goal;
        this.username = username;
        this.gpspoints.addAll(points);
    }

    public void setGPSpoints(List<GPSPoint> points) {
        this.gpspoints.clear();
        this.gpspoints.addAll(points);
    }

    public void addGPSpoint(GPSPoint point) {
        this.gpspoints.add(point);
    }

    public void addGPSpoints(Collection<GPSPoint> point) {
        this.gpspoints.addAll(point);
    }

    public List<GPSPoint> getGpspoints() {
        return new ArrayList<>(this.gpspoints);
    }

    public Optional<Long> getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = Optional.of(id);
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
                && this.id.equals(run.id)
                && this.username.equals(run.username)
                && this.gpspoints.equals(run.gpspoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.goal, this.username, this.gpspoints);
    }
}
