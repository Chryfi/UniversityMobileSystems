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

/**
 * This activity provides login and register buttons.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * This stores the username when the user logs in.
     */
    private static String loggedInUsername = "";

    public static String getLoggedinUsername() {
        return loggedInUsername;
    }

    /**
     * This only clears the stored username.
     */
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
         * main activity can't be accessed when the user is logged in
         * start the RunStartActivity and clear the back-stack so the user cannot return.
         */
        if (!MainActivity.getLoggedinUsername().isEmpty()) {
            Intent intent = new Intent(this, RunStartActivity.class);
            /*
             * this clears the back-stack
             * when logged in, everything before that shouldn't be accessed anymore
             */
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(intent);
            /* finish destroys the activity and prevents the user from going back to it */
            this.finish();
        }
    }

    /**
     * Event method for the UI
     * @param view
     */
    public void onRegister(View view) {
        this.startActivity(new Intent(this, RegisterUserActivity.class));
    }

    /**
     * Event method for the UI
     * @param view
     */
    public void onLogin(View view) {
        this.startActivity(new Intent(this, LoginUserActivity.class));
    }
}