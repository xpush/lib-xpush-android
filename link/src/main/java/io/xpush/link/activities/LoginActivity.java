package io.xpush.link.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.xpush.chat.network.LoginRequest;
import io.xpush.link.R;

public class LoginActivity extends AppCompatActivity  {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int REQUEST_SIGNUP = 100;

    EditText mIdText;
    EditText mPasswordText;
    Button mLoginButton;
    TextView mSignupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mIdText = (EditText)findViewById(R.id.input_id);
        mPasswordText = (EditText)findViewById(R.id.input_password);
        mLoginButton = (Button)findViewById(R.id.btn_login);
        mSignupLink = (TextView)findViewById(R.id.link_signup);

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        mSignupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        mLoginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String id = mIdText.getText().toString();
        String password = mPasswordText.getText().toString();

        final Map<String,String> params = new HashMap<String, String>();

        params.put("A", getString(R.string.app_id));
        params.put("U", id);
        params.put("PW", password);
        params.put("D", getString(R.string.device_id));

        String url = getString(R.string.host_name)+"/auth";

        LoginRequest request = new LoginRequest(url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if( "ok".equalsIgnoreCase(response.getString("status")) ){
                            progressDialog.dismiss();
                            onLoginSuccess();
                        } else {
                            progressDialog.dismiss();
                            onLoginFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Login error ======================");
                    error.printStackTrace();
                    progressDialog.dismiss();
                    onLoginFailed();
                }
            }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(), "Signup success", Toast.LENGTH_LONG).show();
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        mLoginButton.setEnabled(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Intent intent = null;
        if( pref.getBoolean("SITE_READY", false) ) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, UnreadyActivity.class);
        }
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mLoginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String id = mIdText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (id.isEmpty() || id.length() < 4 || id.length() > 10) {
            mIdText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mIdText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}