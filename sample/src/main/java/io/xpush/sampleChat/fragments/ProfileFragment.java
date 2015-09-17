package io.xpush.sampleChat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.xpush.chat.ApplicationController;
import io.xpush.sampleChat.R;

public class ProfileFragment extends Fragment {

    private String TAG = ProfileFragment.class.getSimpleName();
    private Context mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //getActivity().setTheme(R.style...);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}
