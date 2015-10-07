package io.xpush.link.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private EditText inputSiteName;
    private EditText inputSiteUrl;
    private EditText inputSiteDescription;

    private Button btnRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputSiteName = (EditText)findViewById(R.id.input_site_name);
        inputSiteUrl = (EditText)findViewById(R.id.input_site_url);
        inputSiteDescription = (EditText)findViewById(R.id.input_site_description);

        btnRegister = (Button)findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public void register() {
        Log.d(TAG, "Register");

        if (!validate()) {
            onRegisterFailed();
            return;
        }

        btnRegister.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registering...");
        progressDialog.show();

        String name = inputSiteName.getText().toString();
        String url = inputSiteUrl.getText().toString();
        String description = inputSiteDescription.getText().toString();

        final Map<String,String> params = new HashMap<String, String>();

        JSONObject data = new JSONObject();
        try {
            data.put("name", name);
            data.put("url", url);
            data.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.put("A", getString(R.string.app_id));
        params.put("DT", data.toString());
        params.put("D", getString(R.string.device_id));

        String registerUrl = getString(R.string.host_name)+"/user/register";

        StringRequest request = new StringRequest(registerUrl, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "Register success ======================");
                    Log.d(TAG, response.toString());
                    try {
                        if( "ok".equalsIgnoreCase(response.getString("status")) ){
                            progressDialog.dismiss();
                            onRegisterSuccess();
                        } else {
                            progressDialog.dismiss();
                            onRegisterFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Register error ======================");
                    error.printStackTrace();
                    progressDialog.dismiss();
                    onRegisterFailed();
                }
            }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    public void onRegisterSuccess() {
        btnRegister.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onRegisterFailed() {
        Toast.makeText(getBaseContext(), "Register failed", Toast.LENGTH_LONG).show();

        btnRegister.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = inputSiteName.getText().toString();
        String url = inputSiteUrl.getText().toString();
        String description = inputSiteDescription.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            inputSiteName.setError("at least 3 characters");
            valid = false;
        } else {
            inputSiteName.setError(null);
        }

        if (url.isEmpty() || url.length() < 4 ) {
            inputSiteUrl.setError("at least 4 characters");
            valid = false;
        } else {
            inputSiteUrl.setError(null);
        }

        if (description.isEmpty() || description.length() < 4 ) {
            inputSiteDescription.setError("at least 4 characters");
            valid = false;
        } else {
            inputSiteDescription.setError(null);
        }

        return valid;
    }
}
