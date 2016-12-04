package matrians.instapaysam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import matrians.instapaysam.pojo.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Team Matrians
 */
public class LoginActivity extends AppCompatActivity {

    private String TAG = this.getClass().getName();
    static final int STATUS_LOGGED_IN = 1;
    static final int STATUS_LOGGED_OUT = 0;
    private ProgressDialog dialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            setResult(2);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab;
        if ((fab = (FloatingActionButton) findViewById(R.id.fab)) != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(
                            new Intent(v.getContext(), RegisterActivity.class),
                            HomeActivity.CODE_REGISTER);
                    finish();
                }
            });
        }

        final TextInputEditText loginId = (TextInputEditText) findViewById(R.id.login_id);
        if (loginId != null)
            loginId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    TextInputEditText editText = (TextInputEditText) v;
                    if (!hasFocus) {
                        if (editText.getText().length() == 0) {
                            Toast.makeText(v.getContext(), getResources().getString(
                                    R.string.errEmptyField), Toast.LENGTH_SHORT).show();
                            editText.setHintTextColor(ContextCompat.getColor(
                                    v.getContext(), android.R.color.holo_red_light));
                        }
                    } else {
                        editText.setHintTextColor(ContextCompat.getColor(
                                v.getContext(), R.color.colorSecondaryText));
                        editText.setTextColor(ContextCompat.getColor(
                                v.getContext(), R.color.colorPrimaryText));
                    }
                }
            });

        final TextInputEditText password = (TextInputEditText) findViewById(R.id.password);
        if (password != null)
            password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    TextInputEditText editText = (TextInputEditText) v;
                    if (!hasFocus) {
                        if (editText.getText().length() == 0) {
                            Toast.makeText(v.getContext(), getResources().getString(
                                    R.string.errEmptyField), Toast.LENGTH_SHORT).show();
                            editText.setHintTextColor(ContextCompat.getColor(
                                    v.getContext(), android.R.color.holo_red_light));
                        }
                    } else {
                        editText.setHintTextColor(ContextCompat.getColor(
                                v.getContext(), R.color.colorSecondaryText));
                        editText.setTextColor(ContextCompat.getColor(
                                v.getContext(), R.color.colorPrimaryText));
                    }
                }
            });

        Button loginButton;
        if ((loginButton = (Button) findViewById(R.id.btn_login)) != null) {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void onClick(final View v) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog = Utils.showProgress(v.getContext(), "Verifying credentials.");
                        }
                    });

                    boolean valid = true;

                    User user = new User(true);
                    if ( (user.userName = user.email = loginId.getText().toString()).length() == 0 ) {
                        Toast.makeText(v.getContext(),
                                R.string.errFieldEmpty, Toast.LENGTH_SHORT).show();
                        loginId.setHintTextColor(ContextCompat.getColor(
                                v.getContext(), android.R.color.holo_red_light));
                        loginId.requestFocus();
                        valid = false;
                    }
                    if ( password.getText().length() == 0 ) {
                        Toast.makeText(v.getContext(),
                                R.string.errFieldEmpty, Toast.LENGTH_SHORT).show();
                        loginId.setHintTextColor(ContextCompat.getColor(
                                v.getContext(), android.R.color.holo_red_light));
                        loginId.requestFocus();
                        valid = false;
                    }

                    if (!valid) {
                        Toast.makeText(v.getContext(), R.string.errRedFields, Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        user.password = password.getText().toString();
                    }

                    Call<User> call = Server.connect().loginUser(user);
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            dialog.dismiss();
                            if (200 == response.code()) {
                                SharedPreferences.Editor editor = PreferenceManager.
                                        getDefaultSharedPreferences(getApplicationContext()).edit();
                                editor.putInt(getString(R.string.prefLoginStatus), STATUS_LOGGED_IN);
                                editor.putString(getString(R.string.prefEmail), response.body().email);
                                editor.putString(getString(R.string.prefUserId), response.body()._id);
                                editor.apply();
                                setResult(1);
                                finish();
                            } else Snackbar.make((View) v.getParent(),
                                    response.body().err, Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            dialog.dismiss();
                            Snackbar.make((View) v.getParent(),
                                    R.string.snackNetworkError, Snackbar.LENGTH_LONG).show();
                            Log.d(TAG, t.toString());
                        }
                    });
                }
            });
        }
        View linkForgotPassword;
        if ((linkForgotPassword = findViewById(R.id.link_forgot_password)) != null) {
            linkForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(v.getContext(), RegisterActivity.class));
                    //finish();
                }
            });
        }
    }
}
