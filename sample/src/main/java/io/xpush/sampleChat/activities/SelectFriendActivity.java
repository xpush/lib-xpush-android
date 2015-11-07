package io.xpush.sampleChat.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.xpush.sampleChat.R;
import io.xpush.sampleChat.fragments.SearchUserFragment;
import io.xpush.sampleChat.fragments.SelectFriendFragment;

public class SelectFriendActivity extends AppCompatActivity {

    public static final String TAG = SelectFriendActivity.class.getSimpleName();

    private SelectFriendFragment f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        f = new SelectFriendFragment();
        f.setArguments(getIntent().getExtras());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, f, TAG).commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
    }

}
