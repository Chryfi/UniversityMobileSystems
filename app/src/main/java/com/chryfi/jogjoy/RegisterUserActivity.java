package com.chryfi.jogjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.chryfi.jogjoy.data.User;
import com.chryfi.jogjoy.data.tables.UserTable;


public class RegisterUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        EditText weight = this.findViewById(R.id.weight_input);
        EditText height = this.findViewById(R.id.height_input);

        /* event listeners when the user finished typing the text to auto correct it */
        weight.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                try {
                    weight.setText(String.valueOf(roundWeight(Float.parseFloat(weight.getText().toString()))));
                } catch (NumberFormatException e) { }
            }
        });

        height.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                try {
                    height.setText(String.valueOf(roundHeight(Float.parseFloat(height.getText().toString()))));
                } catch (NumberFormatException e) { }
            }
        });
    }

    public void registerUser(View view) {
        EditText username = this.findViewById(R.id.username_input);
        EditText password0 = this.findViewById(R.id.password_input);
        EditText password1 = this.findViewById(R.id.password_input2);
        Spinner gender = this.findViewById(R.id.gender_spinner);
        EditText weight = this.findViewById(R.id.weight_input);
        EditText height = this.findViewById(R.id.height_input);

        /* check if everything has been input */
        if (username.getText().toString().isEmpty()
                || password0.getText().toString().isEmpty() || password1.getText().toString().isEmpty()
                || weight.getText().toString().isEmpty() || height.getText().toString().isEmpty()
                || gender.getSelectedItem() == null) {
            return;
        }

        boolean error = false;

        /* check if username already exists */
        try (UserTable userTable = new UserTable(this)) {
            if (userTable.getUserByUsername(username.getText().toString()).isPresent()) {
                username.setError(this.getResources().getString(R.string.username_already_exists));
                error = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.database_error))
                    .show();
            return;
        }

        /* check if passwords match */
        if (!password0.getText().toString().equals(password1.getText().toString())) {
            password0.getText().clear();
            password1.getText().clear();
            password0.setError(this.getResources().getString(R.string.password_no_match));
            password1.setError(this.getResources().getString(R.string.password_no_match));
            error = true;
        }

        /* validate number ranges */
        try {
            if (Float.parseFloat(weight.getText().toString()) <= 0
                    || Float.parseFloat(weight.getText().toString()) >= 1000) {
                weight.setError(this.getResources().getString(R.string.number_out_range));
                error = true;
            }
        } catch (NumberFormatException e) {
            weight.setError(this.getResources().getString(R.string.not_a_number));
            error = true;
        }

        try {
            if (Float.parseFloat(height.getText().toString()) <= 0
                    || Float.parseFloat(height.getText().toString()) >= 1000) {
                height.setError(this.getResources().getString(R.string.number_out_range));
                error = true;
            }
        } catch (NumberFormatException e) {
            height.setError(this.getResources().getString(R.string.not_a_number));
            error = true;
        }

        if (error) return;

        User.Gender genderValue = null;
        switch (gender.getSelectedItemPosition()) {
            case 0:
                genderValue = User.Gender.MALE;
                break;
            case 1:
                genderValue = User.Gender.FEMALE;
                break;
            case 2:
                genderValue = User.Gender.OTHER;
                break;
        }

        String password = password0.getText().toString();
        String usernameValue = username.getText().toString();
        float weightValue = this.roundWeight(Float.parseFloat(weight.getText().toString()));
        int heightValue = this.roundHeight(Float.parseFloat(height.getText().toString()));

        User user = new User(usernameValue, password, weightValue, heightValue, genderValue);

        try (UserTable userTable = new UserTable(this)) {
            if (userTable.insertUser(user)) {
                this.startActivity(new Intent(this, LoginUserActivity.class));
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(this.getResources().getString(R.string.database_error))
                        .show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.database_error))
                    .show();
        }
    }

    /**
     * Guarantee that event listeners and other methods round the same.
     * @param rawWeight
     * @return rounded weight
     */
    private float roundWeight(float rawWeight) {
        return Math.round(rawWeight * 100F) / 100F;
    }

    /**
     * Guarantee that event listeners and other methods round the same.
     * @param rawHeight
     * @return rounded height.
     */
    private int roundHeight(float rawHeight) {
        return Math.round(rawHeight);
    }
}