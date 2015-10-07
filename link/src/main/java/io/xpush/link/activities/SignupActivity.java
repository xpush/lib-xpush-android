package io.xpush.link.activities;

import android.app.ProgressDialog;
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

import io.xpush.chat.network.StringRequest;
import io.xpush.link.R;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = SignupActivity.class.getSimpleName();

    private EditText mNameText;
    private EditText mIdText;
    private EditText mPasswordText;
    private Button mSignupButton;
    private TextView mLoginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mNameText = (EditText)findViewById(R.id.input_name);
        mIdText = (EditText)findViewById(R.id.input_id);
        mPasswordText = (EditText)findViewById(R.id.input_password);
        mSignupButton = (Button)findViewById(R.id.btn_signup);
        mLoginLink = (TextView)findViewById(R.id.link_login);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        mSignupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = mNameText.getText().toString();
        String id = mIdText.getText().toString();
        String password = mPasswordText.getText().toString();
        final Map<String,String> params = new HashMap<String, String>();

        JSONObject data = new JSONObject();
        try {
            data.put("NM", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.put("A", getString(R.string.app_id));
        params.put("U", id);
        params.put("DT", data.toString());
        params.put("PW", password);
        params.put("D", getString(R.string.device_id));
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if( null != pref.getString("REGISTERED_NOTIFICATION_ID", null)){
            params.put("N", pref.getString("REGISTERED_NOTIFICATION_ID", null) );
        }
        String url = getString(R.string.host_name)+"/user/register";

        StringRequest request = new StringRequest(url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "SignUp success ======================");
                    Log.d(TAG, response.toString());
                    try {
                        if( "ok".equalsIgnoreCase(response.getString("status")) ){
                            progressDialog.dismiss();
                            onSignupSuccess();
                        } else {
                            progressDialog.dismiss();
                            onSignupFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "SignUp error ======================");
                    error.printStackTrace();
                    progressDialog.dismiss();
                    onSignupFailed();
                }
            }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    public void onSignupSuccess() {
        mSignupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "SignUp failed", Toast.LENGTH_LONG).show();

        mSignupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mNameText.getText().toString();
        String id = mIdText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mNameText.setError("at least 3 characters");
            valid = false;
        } else {
            mNameText.setError(null);
        }

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
