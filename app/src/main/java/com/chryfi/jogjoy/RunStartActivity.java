package com.chryfi.jogjoy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class RunStartActivity extends AppCompatActivity {
    public final static String RUNGOAL_MESSAGE = "JobJoy.RUNGOALDISTANCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_userdata);

        EditText runGoal = this.findViewById(R.id.run_goal);

        /* event listeners when the user finished typing the text to auto correct it */
        runGoal.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                try {
                    runGoal.setText(String.valueOf(this.roundRunGoal(Float.parseFloat(runGoal.getText().toString()))));
                } catch (NumberFormatException e) { }
            }
        });
    }

    public void logout(View view) {
        MainActivity.logoutUser();
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    public void startRun(View view) {
        EditText runGoal = this.findViewById(R.id.run_goal);

        if (!this.validateInput()) return;
        float runGoalValue = Math.round(Float.parseFloat(runGoal.getText().toString()) * 100) / 100F;

        Intent intent = new Intent(this, RunActivity.class);
        intent.putExtra(RUNGOAL_MESSAGE, runGoalValue);
        this.startActivity(intent);
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
