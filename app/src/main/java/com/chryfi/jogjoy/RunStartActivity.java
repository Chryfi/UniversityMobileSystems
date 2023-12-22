package com.chryfi.jogjoy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class RunStartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_userdata);
    }

    public void logout(View view) {
        MainActivity.logoutUser();
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    public void startRun(View view) {
        this.startActivity(new Intent(this, RunActivity.class));
    }
}
