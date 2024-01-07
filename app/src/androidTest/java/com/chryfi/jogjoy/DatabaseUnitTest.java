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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseUnitTest {
    /**
     * Tests insertion, reading from database and comparing with the local object
     * and deleting the objects again.
     */
    @Test
    public void testDatabase() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String username = "testuser";
        List<Run> runs = new ArrayList<>();
        User user = new User(username, "password", 80, 180, User.Gender.MALE);
        Run run0 = new Run(12.5F, username);
        runs.add(run0);
        Run run1 = new Run(3F, username);
        runs.add(run1);
        Run run2 = new Run(4F, username);
        runs.add(run2);
        Run run3 = new Run(8F, username);
        runs.add(run3);

        try (UserTable userTable = new UserTable(context);
             RunTable runTable = new RunTable(context);
             GPSTable gpsTable = new GPSTable(context)) {
            System.out.println("Testing insertion of user.");
            assertTrue(userTable.insertUser(user));
            assertTrue(userTable.getUserByUsername(username).isPresent());
            assertEquals(userTable.getUserByUsername(username).get(), user);

            System.out.println("Testing insertion of run.");
            for (Run run : runs) {
                assertTrue(runTable.insertRun(run));
                assertTrue(runTable.getRunById(run.getId()).isPresent());
                assertEquals(runTable.getRunById(run.getId()).get(), run);
                this.addRandomGPSPoints(run, (int) (Math.random() * 50));
                this.insertGpsPoints(gpsTable, run);
                assertEquals(gpsTable.getGPSPoints(run.getId()), run.getGpspoints());
            }

            runs.sort(Comparator.comparingLong(element -> -element.getGpspoints().get(0).getTimestamp()));
            assertEquals(runTable.getRunsDescTime(user.getUsername()), runs);

            /* deletion */
            for (Run run : runs) {
                System.out.println("Testing deletion of gps point.");
                for (GPSPoint point : run.getGpspoints()) {
                    assertTrue(gpsTable.deleteGPSPoint(point));
                    assertFalse(gpsTable.getGPSPoint(point.getRunid(), point.getTimestamp()).isPresent());
                }

                System.out.println("Testing deletion of run.");
                assertTrue(runTable.deleteRun(run));
                assertFalse(runTable.getRunById(run.getId()).isPresent());
            }

            System.out.println("Testing deletion of user.");
            assertTrue(userTable.deleteUser(user));
            assertFalse(userTable.getUserByUsername(username).isPresent());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertGpsPoints(GPSTable gpsTable, Run run) {
        for (GPSPoint point : run.getGpspoints()) {
            assertTrue(gpsTable.insertGPSPoint(point));
            assertTrue(gpsTable.getGPSPoint(point.getRunid(), point.getTimestamp()).isPresent());
            assertEquals(gpsTable.getGPSPoint(point.getRunid(), point.getTimestamp()).get(), point);
        }
    }

    private void addRandomGPSPoints(Run run, int amount) {
        for (int i = 0; i < amount; i++) {
            run.addGPSpoint(new GPSPoint(run.getId(), (long) (Math.random() * 1000000), Math.random() * 10, Math.random() * 10));
        }
    }
}