package com.chryfi.jogjoy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chryfi.jogjoy.data.User;
import com.chryfi.jogjoy.data.tables.UserTable;

import java.util.Optional;

/**
 * Enables a registered user to login.
 */
public class LoginUserActivity extends AppCompatActivity {
    /**
     * onCreate gets started when opening Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* sets the corresponding UI View */
        this.setContentView(R.layout.activity_login_user);
    }

    /**
     * Event method for UI when clicking on login button.
     * @param view
     */
    public void loginUser(View view) {
        EditText username = this.findViewById(R.id.username_input);
        EditText password = this.findViewById(R.id.password_input);

        /* validate input */
        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            return;
        }

        try (UserTable userTable = new UserTable(this)) {
            Optional<User> user = userTable.getUserByUsername(username.getText().toString());
            /* check user for existence and passwords for equality */
            if (user.isPresent() && user.get().getPassword().equals(password.getText().toString())) {
                MainActivity.loginUser(user.get().getUsername());
                /* explicit intent to start RunStartActivity*/
                Intent intent = new Intent(this, RunStartActivity.class);
                /* don't let the user go back to login page */
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                this.startActivity(intent);
                this.finish();
            } else {
                /* popup that tells the user that password or username was wrong */
                new AlertDialog.Builder(this)
                        .setMessage(this.getResources().getString(R.string.login_pw_name_fail))
                        .show();
            }
        } catch (SQLException e) {
            /* If Database = "Boom" */
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.database_error))
                    .show();
        }
    }
}