package com.chryfi.jogjoy;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.Location;

import com.chryfi.jogjoy.data.GPSPoint;
import com.chryfi.jogjoy.data.tables.GPSTable;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationRequest;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.chryfi.jogjoy.data.Run;
import com.chryfi.jogjoy.data.tables.RunTable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This activity monitors the active run and provides live feedback.
 */
public class RunActivity extends AppCompatActivity {
    /**
     * The current run instance.
     */
    private Run activeRun;
    /**
     * The current run goal in km with 2 decimal places.
     */
    private float goal;
    /**
     * Handles the timer
     */
    private Handler timerHandler;
    /**
     * Stores the method to execute for the timer
     */
    private Runnable timerRunnable;
    /**
     * To account for race condition with gps updates.
     * The first GPSPoint that is inserted will start the timer.
     */
    private boolean timerStarted = false;
    private final SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    /**
     * Stores the method to execute for the regular gps updates.
     */
    private LocationCallback locationCallback;
    /**
     * Request code for the gps permissions to identify them.
     */
    public static final int GPS_PERMISSION_REQUEST = 123;
    /**
     * The message is a boolean whether the run goal was achieved.
     */
    public final static String RUNGOAL_ACHIEVED_MESSAGE = "JogJoy.RUNGOAL_ACHIEVED";
    /**
     * The message is a boolean whether there was
     * an error when inserting the run and gps points into the database.
     */
    public final static String DATABASE_ERROR_MESSAGE = "JogJoy.DATABASE_ERROR";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_run_ui);

        Intent intent = this.getIntent();
        this.goal = intent.getFloatExtra(RunStartActivity.RUNGOAL_MESSAGE, 0.0F);

        /* with a goal of 0, stop this activity as goal of 0 does not make sense */
        if (this.goal == 0.0F) this.finish();

        /* setup timer */
        this.timerHandler = new Handler(this.getMainLooper());
        this.timerRunnable = new Runnable() {
            @Override
            public void run() {
                List<GPSPoint> points = activeRun.getGpspoints();
                long millis = System.currentTimeMillis() - points.get(0).getTimestamp();

                TextView timerTextView = findViewById(R.id.timeCurrent);
                String timerTextString = getResources().getString(R.string.actual_time);
                timerTextView.setText(String.format(timerTextString, timerFormat.format(millis)));

                /* timer starts itself again in 500ms */
                if (timerStarted) timerHandler.postDelayed(this, 500);
            }
        };

        this.activeRun = new Run(goal, MainActivity.getLoggedinUsername());

        this.setupUI();

        /* intercept back button and ask the user if they really want to stop the run */
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.run_abort))
                .setPositiveButton(this.getResources().getString(R.string.yes), (dialog, which) -> stopRun())
                .setNegativeButton(this.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                alert.show();
            }
        };
        /* here the back button is intercepted. */
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        /* check if app has the gps permissions, if not request them from the user */
        if (!this.hasGPSPermission()) {
            /*
             * when the user has declined the permission in the past,
             * the default android request prompt cannot be shown again
             */
            if (this.shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                this.offerAppSettings();
            } else {
                /* show android request prompt */
                this.askPermissions();
            }
        } else {
            /*
             * Permissions have been granted in the past -> request location updates.
             */
            this.requestCurrentLocation();
            this.requestLocationUpdates();
        }
    }

    private void setupUI() {
        Intent intent = this.getIntent();

        /* set 0 as the initial distance so the user does not see the placeholder */
        TextView currentDistance = this.findViewById(R.id.distanceCurrent);
        String currentDistanceText = this.getResources().getString(R.string.distance);
        currentDistance.setText(String.format(currentDistanceText, "0"));

        /* set the timer to 0 state so the user does not see the placeholder */
        TextView timer = this.findViewById(R.id.timeCurrent);
        String timerText = this.getResources().getString(R.string.actual_time);
        timer.setText(String.format(timerText, this.timerFormat.format(0)));

        /* set the user defined goal distance */
        TextView goalDistance = this.findViewById(R.id.goalDistance);
        String goalDistanceText = this.getResources().getString(R.string.goal_distance);
        goalDistance.setText(String.format(goalDistanceText, String.valueOf(this.goal)));

        ProgressBar progressBar = this.findViewById(R.id.progressBar);
        /* goal has maximum 2 decimal places, which need to be turned into an integer. */
        progressBar.setMax((int) (goal * 100));
    }

    /**
     * @return true if the app has fine and coarse location permissions.
     */
    private boolean hasGPSPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Add the current location immediately to the active run.
     */
    private void requestCurrentLocation() {
        if (!this.hasGPSPermission()) {
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Task<Location> task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);
        task.addOnSuccessListener(this::addLocation);
    }

    private void requestLocationUpdates() {
        if (!this.hasGPSPermission()) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(2000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(3000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        /* the method to be called for each gps update */
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    addLocation(location);
                }
            }
        };

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(locationRequest, this.locationCallback, Looper.getMainLooper());
    }

    /**
     * Adds the provided location to the {@link #activeRun}.
     * @param location
     */
    private void addLocation(Location location) {
        /*
         * approximation so the distance does not increase
         * due to GPS inaccuracies over time when standing still
         */
        double longitude = Math.round(location.getLongitude() * 10000D) / 10000D;
        double latitude = Math.round(location.getLatitude() * 10000D) / 10000D;
        GPSPoint point = new GPSPoint(this.activeRun.getId(), System.currentTimeMillis(), longitude, latitude);

        this.activeRun.addGPSpoint(point);
        System.out.println("Captured GPS point " + point);

        /* update UI distance text and progressbar*/
        double path = PathCalculator.calculatePathLength(this.activeRun.getGpspoints()) / 1000D;

        TextView currentDistance = this.findViewById(R.id.distanceCurrent);
        String currentDistanceText = this.getResources().getString(R.string.distance);
        DecimalFormat format = new DecimalFormat("0.##");
        String currentDistanceValue = format.format(path);
        currentDistance.setText(String.format(currentDistanceText, currentDistanceValue));

        ProgressBar progressBar = this.findViewById(R.id.progressBar);
        progressBar.setProgress((int) (Double.valueOf(currentDistanceValue) * 100));

        if (Double.valueOf(currentDistanceValue) >= this.goal) {
            this.findViewById(R.id.cat_success).setVisibility(View.VISIBLE);
        }

        /* the first inserted GPS location starts the timer */
        if (!this.timerStarted) {
            this.timerStarted = true;
            this.timerHandler.postDelayed(this.timerRunnable, 0);
        }
    }

    /**
     * Show the permission prompt so the user grants the required permissions.
     */
    private void askPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{"android.permission.ACCESS_FINE_LOCATION",
                        "android.permission.ACCESS_COARSE_LOCATION"},
                GPS_PERMISSION_REQUEST);
    }

    /**
     * Offer the user to open the app settings so they can change the gps permission settings.
     */
    private void offerAppSettings() {
        /*
         * Dismissing this (e.g. back button) will close the activity
         */
        new AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.gps_permissions_not_granted))
                .setPositiveButton(this.getResources().getString(R.string.go_to_settings), (dialog, which) -> {
                    /* implicit intent to open android settings of the app */
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setOnDismissListener((dialog) -> finish())
                .show();
    }

    /**
     * This method is called when permissions have been updated.
     * When the user declines the GPS permission, the user should be reminded, that the app won't work properly
     * and exit out of the run activity.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != GPS_PERMISSION_REQUEST || grantResults.length == 0) return;

        boolean allGranted = true;
        for (int permission : grantResults) {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        /* user granted permissions, now we need to add location requests */
        if (allGranted) {
            this.requestCurrentLocation();
            this.requestLocationUpdates();
            return;
        }

        /* user declined the permissions request - offer to open app settings */
        this.offerAppSettings();
    }

    /**
     * Event method called by stop run button.
     * @param view
     */
    public void onStopRun(View view) {
        this.stopRun();
    }

    public void stopRun() {
        this.cleanUpTasks();

        boolean dbError = false;
        /*
         * Insert run and gps points into the database, only if there are 2 gps points minimum.
         */
        try (RunTable runTable = new RunTable(this);
             GPSTable gpsTable = new GPSTable(this)) {
            if (this.activeRun.getGpspoints().size() >= 2) {
                if (runTable.insertRun(this.activeRun)) {
                    /* run id is only available after inserting run into database */
                    for (GPSPoint point : this.activeRun.getGpspoints()) {
                        point.setRunid(this.activeRun.getId());
                    }

                    /* insert the gps points and count how many were successfully inserted */
                    int i = 0;
                    for (GPSPoint point : this.activeRun.getGpspoints()) {
                        if (gpsTable.insertGPSPoint(point)) {
                            i++;
                        }
                    }

                    /*
                     * not enough points were inserted,
                     * delete everything to ensure correct constraints
                     */
                    if (i < 2) {
                        runTable.deleteRun(this.activeRun);

                        for (GPSPoint point : this.activeRun.getGpspoints()) {
                            gpsTable.deleteGPSPoint(point);
                        }

                        dbError = true;
                    }
                } else {
                    dbError = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dbError = true;
        }

        /* provide intent as result for the activity that launched this run */
        double finishedDistance = PathCalculator.calculatePathLength(this.activeRun.getGpspoints());
        Intent intent = new Intent();
        intent.putExtra(DATABASE_ERROR_MESSAGE, dbError);
        intent.putExtra(RUNGOAL_ACHIEVED_MESSAGE,finishedDistance / 1000D >= this.goal);
        this.setResult(RESULT_OK, intent);
        /* finish destroys the activity and prevents the user from going back to it */
        this.finish();
    }

    /**
     * Clean up update requests, timers etc. so they don't continue running in the background
     */
    private void cleanUpTasks() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.removeLocationUpdates(this.locationCallback);
        this.timerHandler.removeCallbacks(this.timerRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
         * as a safety measurement in case the activity is stopped by something else
         * than the user pressing the button
         */
        this.cleanUpTasks();
    }
}
