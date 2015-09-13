package io.xpush.sampleChat.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
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
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

    private String TAG = SettingsFragment.class.getSimpleName();
    private Context mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //getActivity().setTheme(R.style...);
        mActivity = getActivity();
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

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.d(TAG,"=====================");
        Log.d(TAG,key);

        if (key.equals("username")) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("email")) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
