package com.chryfi.jogjoy;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.location.Location;

import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationRequest;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationRequestCompat;

import com.chryfi.jogjoy.data.Run;
import com.chryfi.jogjoy.data.tables.RunTable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class RunActivity extends AppCompatActivity {
    private Run activeRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_run_ui);

        Intent intent = this.getIntent();
        float goal = intent.getFloatExtra(RunStartActivity.RUNGOAL_MESSAGE, 0.0F);

        /* with a goal of 0, return to previous activity */
        if (goal == 0.0F) this.finish();

        this.activeRun = new Run(goal, MainActivity.getLoggedinUsername());

        try (RunTable runTable = new RunTable(this)) {
            if (!runTable.insertRun(this.activeRun)) {
                /*
                 * on dismiss listener is important so that anything that makes the dialog go away,
                 * leads to the user exiting the activity
                 */
                new AlertDialog.Builder(this)
                        .setMessage(this.getResources().getString(R.string.database_error))
                        .setPositiveButton(this.getResources().getString(R.string.ok), (dialog, which) -> finish())
                        .setOnDismissListener((dialog) -> finish())
                        .show();
            }
        } catch (SQLException e) {
            e.printStackTrace();

            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.database_error))
                    .setPositiveButton(this.getResources().getString(R.string.ok), (dialog, which) -> finish())
                    .setOnDismissListener((dialog) -> finish())
                    .show();

            return;
        }

        /* intercept back button and ask the user if they really want to stop the run */
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.run_abort))
                .setPositiveButton(this.getResources().getString(R.string.yes), (dialog, which) -> {
                    stopRun();
                    finish();
                })
                .setNegativeButton(this.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss());
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                alert.show();
            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);

        LocationRequest locationRequest = new LocationRequest.Builder(3000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdateDelayMillis(3000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                for (Location location : locationResult.getLocations()) {
                    System.out.println("lat " + location.getLatitude() + " long: " + location.getLongitude());
                }
            }
        };

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /* check if app has the gps permissions, if not request them from the user */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"},
                    1);
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void onStopRun(View view) {
        this.stopRun();
    }

    public void pauseRun(View view) {

    }

    public void stopRun() {
        Intent intent = new Intent(this, RunStartActivity.class);
        /* don't let the user go back to run, as it has been finished */
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        this.finish();
    }
}
