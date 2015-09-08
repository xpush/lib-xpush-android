package io.xpush.sampleChat.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.xpush.chat.view.activities.AppCompatPreferenceActivity;
import io.xpush.sampleChat.R;

/**
 * Created by James on 2015-09-05.
 */
public class SettingsFragment extends PreferenceFragment {

    private String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //getActivity().setTheme(R.style...);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View layout = inflater.inflate(R.layout.activity_settings, container, false);
        if (layout != null) {
            AppCompatPreferenceActivity activity = (AppCompatPreferenceActivity) getActivity();
            Toolbar toolbar = (Toolbar) layout.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            ActionBar bar = activity.getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            //bar.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            //bar.setTitle(getPreferenceScreen().getTitle());
        }
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() != null) {
            View frame = (View) getView().getParent();
            if (frame != null)
                frame.setPadding(0, 0, 0, 0);
        }
    }
}
