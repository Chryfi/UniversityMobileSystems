package com.chryfi.jogjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.chryfi.jogjoy.data.User;
import com.chryfi.jogjoy.data.tables.UserTable;


public class RegisterUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
    }

    public void registerUser(View view) {
        EditText username = this.findViewById(R.id.username_input);
        EditText password0 = this.findViewById(R.id.password_input);
        EditText password1 = this.findViewById(R.id.password_input2);
        Spinner gender = this.findViewById(R.id.gender_spinner);
        EditText weight = this.findViewById(R.id.weight_input);
        EditText height = this.findViewById(R.id.height_input);

        if (username.getText().toString().isEmpty()
                || password0.getText().toString().isEmpty() || password1.getText().toString().isEmpty()
                || weight.getText().toString().isEmpty() || height.getText().toString().isEmpty()
                || gender.getSelectedItem() == null) {
            return;
        }

        boolean error = false;

        try (UserTable userTable = new UserTable(this)) {
            if (userTable.getUserByUsername(username.getText().toString()).isPresent()) {
                username.setError(this.getResources().getString(R.string.username_already_exists));
                error = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!password0.getText().toString().equals(password1.getText().toString())) {
            password0.getText().clear();
            password1.getText().clear();
            password0.setError(this.getResources().getString(R.string.password_no_match));
            password1.setError(this.getResources().getString(R.string.password_no_match));
            error = true;
        }

        if (Float.parseFloat(weight.getText().toString()) <= 0
                || Float.parseFloat(weight.getText().toString()) >= 1000) {
            weight.setError(this.getResources().getString(R.string.number_out_range));
            error = true;
        }

        if (Float.parseFloat(height.getText().toString()) <= 0
                || Float.parseFloat(height.getText().toString()) >= 1000) {
            height.setError(this.getResources().getString(R.string.number_out_range));
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
        float weightValue = Math.round(Float.parseFloat(weight.getText().toString()) * 100F) / 100F;
        int heightValue = Math.round(Float.parseFloat(height.getText().toString()));

        User user = new User(usernameValue, password, weightValue, heightValue, genderValue);

        try (UserTable userTable = new UserTable(this)) {
            userTable.insertUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}