package io.xpush.sampleChat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.xpush.sampleChat.R;
import io.xpush.sampleChat.activities.EditProfileNameActivity;
import io.xpush.sampleChat.activities.IntroActivity;

public class ProfileFragment extends Fragment {

    private String TAG = ProfileFragment.class.getSimpleName();
    private Context mActivity;
    private View nicknameButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //getActivity().setTheme(R.style...);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nicknameButton = view.findViewById(R.id.nicknameButton);
        nicknameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUserName();
            }
        });

        return view;
    }

    public void editUserName() {
        Intent localIntent = new Intent(mActivity, EditProfileNameActivity.class);
        getActivity().startActivityForResult(localIntent, 103);
    }
}
