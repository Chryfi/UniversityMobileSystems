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


        User testUser = new User("test2", "password", 80, 180, User.Gender.MALE);

        List<GPSPoint> points = new ArrayList<>();
        points.add(new GPSPoint(-1, 1, 2, 3));
        points.add(new GPSPoint(-1, 2, 3, 3));
        points.add(new GPSPoint(-1, 3, 4, 3));
        points.add(new GPSPoint(-1, 4, 2, 3));
        Run testRun = new Run(12.1F, "test2", points);

        try (UserTable userTable = new UserTable(this);
             RunTable runTable = new RunTable(this);
             GPSTable gpsTable = new GPSTable(this)) {
            System.out.println("Test2 deleted " + userTable.deleteUser("test2"));
            System.out.println("Insert test2 user " + userTable.insertUser(testUser));
            System.out.println("Insert testRun " + runTable.insertRun(testRun));
            System.out.println("testrun equality " + runTable.getRunById(testRun.getId().get()).get());
            System.out.println("EQUALS " + userTable.getUserByUsername("test2").get().equals(testUser));
        }
    }
}