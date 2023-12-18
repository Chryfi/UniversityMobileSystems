package com.chryfi.jogjoy;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.chryfi.jogjoy.data.GPSPoint;
import com.chryfi.jogjoy.data.Run;
import com.chryfi.jogjoy.data.User;
import com.chryfi.jogjoy.data.tables.GPSTable;
import com.chryfi.jogjoy.data.tables.RunTable;
import com.chryfi.jogjoy.data.tables.UserTable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
    }
}