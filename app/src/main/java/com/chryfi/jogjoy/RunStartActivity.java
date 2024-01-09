package com.chryfi.jogjoy;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chryfi.jogjoy.data.GPSPoint;
import com.chryfi.jogjoy.data.Run;
import com.chryfi.jogjoy.data.tables.RunTable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Provides a list of recent runs and the functionality to start a new run.
 */
public class RunStartActivity extends AppCompatActivity {
    /**
     * Message key when sending the goal input to the RunActivity.
     */
    public final static String RUNGOAL_MESSAGE = "JogJoy.RUNGOALDISTANCE";
    /**
     * Request code to identify the result returned from the run activity.
     */
    public static final int RUN_RESULT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_run_userdata);

        EditText runGoal = this.findViewById(R.id.run_goal);

        /*
         * focusChangeListener is triggered when the user enters or exits the text element.
         * Input gets rounded when exiting, prevent too many decimal places
         */
        runGoal.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                try {
                    runGoal.setText(String.valueOf(this.roundRunGoal(Float.parseFloat(runGoal.getText().toString()))));
                } catch (NumberFormatException e) { }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode != RUN_RESULT_REQUEST) return;

        if (intent.getBooleanExtra(RunActivity.DATABASE_ERROR_MESSAGE, false)) {
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.database_error))
                    .show();
            return;
        }

        if (intent.getBooleanExtra(RunActivity.RUNGOAL_ACHIEVED_MESSAGE, false)) {
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.run_goal_achieved))
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.run_goal_not_achieved))
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
         * when the user comes back from the run activity, the list needs to be updated
         * onResume() is also called after onCreate()
         */
        this.listRuns();
    }

    /**
     * Read runs by the logged in user from the database and insert them into the UI.
     * Runs are sorted by date descending.
     */
    private void listRuns() {
        try (RunTable runTable = new RunTable(this)) {
            List<Run> runs = runTable.getRunsDescTime(MainActivity.getLoggedinUsername());
            /* clear the run list in case the list is updated */
            LinearLayout scrollView = this.findViewById(R.id.runs_scroll_linear);
            scrollView.removeAllViews();

            for (Run run : runs) {
                List<GPSPoint> points = run.getGpspoints();

                TextView runText = new TextView(this);
                runText.setBackground(this.getDrawable(R.drawable.border));
                String runElement = this.getResources().getString(R.string.run_element);
                /* Sets the formatted values for the UI */
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                DecimalFormat format = new DecimalFormat("0.##");
                long time = (points.get(points.size() - 1).getTimestamp() - points.get(0).getTimestamp());
                double path = PathCalculator.calculatePathLength(run.getGpspoints()) / 1000D;

                runText.setText(String.format(runElement,
                        dateFormat.format(points.get(0).getTimestamp()), timeFormat.format(time),
                        format.format(path), format.format(run.getGoal())));

                scrollView.addView(runText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event method for UI when clicking on logout button.
     * @param view
     */
    public void logout(View view) {
        MainActivity.logoutUser();
        Intent intent = new Intent(this, MainActivity.class);
        /* reset back-stack so the user can't go back to the run activity when logging out */
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
        this.finish();
    }

    /**
     * Event method for UI when clicking on start run button.
     * @param view
     */
    public void startRun(View view) {
        EditText runGoal = this.findViewById(R.id.run_goal);

        if (!this.validateInput()) return;
        float runGoalValue = this.roundRunGoal(Float.parseFloat(runGoal.getText().toString()));
        /* clear run goal, so when the user comes back it's empty */
        runGoal.setText("");

        Intent intent = new Intent(this, RunActivity.class);
        intent.putExtra(RUNGOAL_MESSAGE, runGoalValue);
        this.startActivityForResult(intent, RUN_RESULT_REQUEST);
    }

    /**
     * Validates inputs and sets error messages to the UI.
     * @return false if there has been an error.
     */
    private boolean validateInput() {
        EditText runGoal = this.findViewById(R.id.run_goal);

        if (runGoal.getText().toString().isEmpty()) {
            runGoal.setError(this.getResources().getString(R.string.input_missing));
            return false;
        }

        try {
            Float.parseFloat(runGoal.getText().toString());
        } catch (NumberFormatException e) {
            return false;
        }

        float runGoalValue = this.roundRunGoal(Float.parseFloat(runGoal.getText().toString()));

        if (runGoalValue == 0.0F) {
            runGoal.setError(this.getResources().getString(R.string.run_goal_number_low));
            return false;
        }

        return true;
    }

    private float roundRunGoal(float runGoal) {
        return Math.round(runGoal * 10) / 10F;
    }
}
