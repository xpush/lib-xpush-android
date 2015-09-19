package io.xpush.sampleChat.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import io.xpush.sampleChat.R;

public class EditProfileNameActivity extends FragmentActivity {

    private TextView userName = null;
    private TextView textCount;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.edit_profile_name);

        this.userName = ((EditText)findViewById(R.id.user_name));
        this.textCount = ((TextView)findViewById(R.id.text_count));
    }
}
