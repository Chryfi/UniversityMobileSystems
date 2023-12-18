package com.chryfi.jogjoy;

import android.content.Context;
import android.database.SQLException;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.chryfi.jogjoy.data.GPSPoint;
import com.chryfi.jogjoy.data.Run;
import com.chryfi.jogjoy.data.User;
import com.chryfi.jogjoy.data.tables.GPSTable;
import com.chryfi.jogjoy.data.tables.RunTable;
import com.chryfi.jogjoy.data.tables.UserTable;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseUnitTest {
    @Test
    public void testDatabase() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String username = "testuser";
        User user = new User(username, "password", 80, 180, User.Gender.MALE);
        Run run = new Run(12.5F, username);

        try (UserTable userTable = new UserTable(context);
             RunTable runTable = new RunTable(context);
             GPSTable gpsTable = new GPSTable(context)) {

            System.out.println("Testing insertion of user.");
            assertTrue(userTable.insertUser(user));
            assertTrue(userTable.getUserByUsername(username).isPresent());

            System.out.println("Testing equality of db gps point and local object.");
            assertEquals(userTable.getUserByUsername(username).get(), user);

            System.out.println("Testing insertion of run.");
            assertTrue(runTable.insertRun(run));
            assertTrue(runTable.getRunById(run.getId()).isPresent());

            run.addGPSpoint(new GPSPoint(run.getId(), 3, 1.2318458, 2.2318484));
            run.addGPSpoint(new GPSPoint(run.getId(), 4, 3.15284, 4.842384));
            run.addGPSpoint(new GPSPoint(run.getId(), 1, 5.16982, 6.18659023));
            run.addGPSpoint(new GPSPoint(run.getId(), 2, 7.186523, 8.1986503));
            run.addGPSpoint(new GPSPoint(run.getId(), 2, 1, 2));

            for (GPSPoint point : run.getGpspoints()) {
                System.out.println("Testing insertion of gps point.");
                assertTrue(gpsTable.insertGPSPoint(point));
                assertTrue(gpsTable.getGPSPoint(point.getRunid(), point.getTimestamp()).isPresent());

                System.out.println("Testing equality of db gps point and local object.");
                assertEquals(gpsTable.getGPSPoint(point.getRunid(), point.getTimestamp()).get(), point);
            }

            System.out.println("Testing equality of run and local object");
            assertEquals(runTable.getRunById(run.getId()).get(), run);

            /* deletion */
            for (GPSPoint point : run.getGpspoints()) {
                System.out.println("Testing deletion of gps point.");
                assertTrue(gpsTable.deleteGPSPoint(point.getRunid(), point.getTimestamp()));
                assertFalse(gpsTable.getGPSPoint(point.getRunid(), point.getTimestamp()).isPresent());
            }

            System.out.println("Testing deletion of run.");
            assertTrue(runTable.deleteRun(run.getId(), username));
            assertFalse(runTable.getRunById(run.getId()).isPresent());

            System.out.println("Testing deletion of user.");
            assertTrue(userTable.deleteUser(username));
            assertFalse(userTable.getUserByUsername(username).isPresent());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}