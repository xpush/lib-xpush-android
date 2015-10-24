package io.xpush.sampleChat.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.xpush.chat.ApplicationController;
import io.xpush.chat.common.Constants;
import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.network.StringRequest;
import io.xpush.chat.util.RealPathUtil;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.activities.EditNickNameActivity;
import io.xpush.sampleChat.activities.EditStatusMessageActivity;

public class ProfileFragment extends Fragment {

    private String TAG = ProfileFragment.class.getSimpleName();
    private Context mActivity;

    private XPushSession mSession;
    private JSONObject mJsonUserData;

    @Bind(R.id.nickname_button)
    View mNicknameButton;

    @Bind(R.id.status_message_button)
    View mStatusMessageButton;

    @Bind(R.id.imageBox)
    View mImageBox;

    @Bind(R.id.thumbnail)
    SimpleDraweeView mThumbnail;

    @Bind(R.id.nickname)
    TextView mTvNickname;

    @Bind(R.id.status_message)
    TextView mTvStatusMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mSession = XPushCore.getInstance().getXpushSession();
        mJsonUserData = mSession.getUserData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        if( mSession.getImage() != null ) {
            mThumbnail.setImageURI(Uri.parse(mSession.getImage()));
        }

        if( null != mSession.getName() ) {
            mTvNickname.setText(mSession.getName());
        }

        if( null != mSession.getMessage() ) {
            mTvStatusMessage.setText(mSession.getMessage());
        }

        return view;
    }

    @OnClick(R.id.nickname_button)
    public void editNickName() {
        Intent localIntent = new Intent(mActivity, EditNickNameActivity.class);
        getActivity().startActivityForResult(localIntent, Constants.REQUEST_EDIT_NICKNAME);
    }

    @OnClick(R.id.status_message_button)
    public void editStatusMessage() {
        Intent localIntent = new Intent(mActivity, EditStatusMessageActivity.class);
        getActivity().startActivityForResult(localIntent, Constants.REQUEST_EDIT_STATUS_MESSAGE);
    }

    @OnClick(R.id.imageBox)
    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to use profile"), Constants.REQUEST_EDIT_IMAGE);
    }

    public void setImage(Uri uri){
        UploadImageTask imageUpload = new UploadImageTask(uri);
        imageUpload.execute();
    }

    public void setNickName(String name){
        try {
            if( mJsonUserData != null ) {
                mJsonUserData.put("NM", name);
                updateProfile();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setStatusMessage(String message){
        try {
            if( mJsonUserData != null ) {
                mJsonUserData.put("MG", message);
                updateProfile();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateProfile(){
        XPushCore.getInstance().updateUser(mJsonUserData, new CallbackEvent() {
            @Override
            public void call(Object... args) {
                try {
                    if( args.length > 0 ) {
                        JSONObject result = (JSONObject) args[0];
                        mThumbnail.setImageURI(Uri.parse(result.getString("I")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class UploadImageTask extends AsyncTask<Void, Void, String> {
        Uri mUri;

        public UploadImageTask( Uri uri ){
            this.mUri = uri;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String downloadUrl = XPushCore.getInstance().uploadImage(mUri);
            try {
                mJsonUserData.put("I", downloadUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return downloadUrl;
        }

        @Override
        protected  void onPostExecute(final String imageUrl){
            super.onPostExecute(imageUrl);
            if( imageUrl != null ){
                updateProfile();
            }
        }
    }
}