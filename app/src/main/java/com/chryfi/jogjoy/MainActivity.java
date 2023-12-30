package com.chryfi.jogjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chryfi.jogjoy.data.GPSPoint;
import com.chryfi.jogjoy.data.Run;
import com.chryfi.jogjoy.data.User;
import com.chryfi.jogjoy.data.tables.GPSTable;
import com.chryfi.jogjoy.data.tables.RunTable;
import com.chryfi.jogjoy.data.tables.UserTable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String loggedInUsername = "";

    public static String getLoggedinUsername() {
        return loggedInUsername;
    }

    public static void logoutUser() {
        loggedInUsername = "";
    }

    public static void loginUser(String username) {
        loggedInUsername = username;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        /*
         * app was brought back from background with logged in user
         * return to logged in start activity
         */
        if (!MainActivity.getLoggedinUsername().isEmpty()) {
            Intent intent = new Intent(this, RunStartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(intent);
            this.finish();
        }
    }

    public void onRegister(View view) {
        this.startActivity(new Intent(this, RegisterUserActivity.class));
    }

    public void onLogin(View view) {
        this.startActivity(new Intent(this, LoginUserActivity.class));
    }
}