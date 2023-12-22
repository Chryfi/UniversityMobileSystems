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
    }

    public void onRegister(View view) {
        startActivity(new Intent(this, RegisterUserActivity.class));
    }

    public void onLogin(View view) {
        startActivity(new Intent(this, LoginUserActivity.class));
    }
}