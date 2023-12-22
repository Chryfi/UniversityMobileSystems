package com.chryfi.jogjoy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class RunActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_ui);
        Intent intent = this.getIntent();
        float message = intent.getFloatExtra(RunStartActivity.RUNGOAL_MESSAGE, 0.0F);

        if (message == 0.0F) this.finish();

        //TODO create run database stuff here -> ensures that only when run activity is visible and working data will be consistent
        //TODO when doing back action -> make an alert if you want to stop the run

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
    }

    public void onStopRun(View view) {

    }

    public void pauseRun(View view) {

    }

    public void stopRun() {

    }
}
