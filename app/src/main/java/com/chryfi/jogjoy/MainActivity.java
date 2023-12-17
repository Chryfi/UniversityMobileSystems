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
        Run testRun = new Run(12.1F, "test2", points);

        try (UserTable userTable = new UserTable(this);
             RunTable runTable = new RunTable(this);
             GPSTable gpsTable = new GPSTable(this)) {

            for (GPSPoint point : points) {
                System.out.println("delete point " + gpsTable.deleteGPSPoint(point.getRunid(), point.getTimestamp()));
            }

            System.out.println("delete run id " + runTable.deleteRun(testRun.getId().get(), testRun.getUsername()));
            System.out.println("delete test2 user " + userTable.deleteUser("test2"));

            System.out.println("insert test2 user " + userTable.insertUser(testUser));
            System.out.println("insert testRun " + runTable.insertRun(testRun));

            points.add(new GPSPoint(testRun.getId().get(), 1, 2, 3));
            points.add(new GPSPoint(testRun.getId().get(), 2, 3, 3));
            points.add(new GPSPoint(testRun.getId().get(), 3, 4, 3));
            points.add(new GPSPoint(testRun.getId().get(), 4, 2, 3));

            for (GPSPoint point : points) {
                System.out.println("insert gps point " + gpsTable.insertGPSPoint(point));
            }

            System.out.println("testrun equality " + runTable.getRunById(testRun.getId().get()).get());
            System.out.println("user equality " + userTable.getUserByUsername("test2").get().equals(testUser));

            List<GPSPoint> pointsDB = gpsTable.getGPSPoints(testRun.getId().get());
            for (int i = 0; i < pointsDB.size() && i < points.size(); i++) {
                GPSPoint pointDB = pointsDB.get(i);
                GPSPoint point = points.get(i);
                System.out.println("GPSpoint equality with timestamp " + pointDB.getTimestamp() + " " + point.equals(pointDB));
            }
        }
    }
}