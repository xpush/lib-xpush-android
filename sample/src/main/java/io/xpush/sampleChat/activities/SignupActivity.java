package io.xpush.sampleChat.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.network.StringRequest;
import io.xpush.sampleChat.R;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = SignupActivity.class.getSimpleName();

    @Bind(R.id.input_id)
    EditText mIdText;

    @Bind(R.id.input_name)
    EditText mNameText;

    @Bind(R.id.input_password)
    EditText mPasswordText;

    @Bind(R.id.btn_signup)
    Button mSignupButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_signup)
    public void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        mSignupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.progress_message_signup));
        progressDialog.show();

        String id = mIdText.getText().toString();
        String name = mNameText.getText().toString();
        String password = mPasswordText.getText().toString();

        XPushCore.getInstance().register(id, password, name, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                if (args == null || args.length == 0) {
                    progressDialog.dismiss();
                    onSignupSuccess();
                } else {
                    progressDialog.dismiss();
                    onSignupFailed();
                }
            }
        });
    }

    public void onSignupSuccess() {
        mSignupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    @OnClick(R.id.link_login)
    public void onSignupCancel() {
        mSignupButton.setEnabled(true);
        setResult(RESULT_CANCELED, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.fail_message_signup), Toast.LENGTH_LONG).show();

        mSignupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mNameText.getText().toString();
        String id = mIdText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (id.isEmpty() || id.length() < 4 || id.length() > 10) {
            mIdText.setError( getString(R.string.error_message_validation_4_10) );
            valid = false;
        } else {
            mIdText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError( getString(R.string.error_message_validation_4_10) );
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}
