package io.xpush.link.fragments;

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

import io.xpush.chat.ApplicationController;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.network.StringRequest;
import io.xpush.chat.util.RealPathUtil;
import io.xpush.link.R;
import io.xpush.link.activities.EditNickNameActivity;
import io.xpush.link.activities.EditStatusMessageActivity;

public class ProfileFragment extends Fragment {

    private String TAG = ProfileFragment.class.getSimpleName();
    private Context mActivity;
    private View mNicknameButton;
    private View mStatusMessageButton;

    private View mImageBox;
    private SimpleDraweeView mThumbnail;
    private XPushSession mSession;
    private TextView mTvNickname;
    private TextView mTvStatusMessage;

    private JSONObject mJsonUserData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mSession = ApplicationController.getInstance().getXpushSession();
        mJsonUserData = mSession.getUserData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mNicknameButton = view.findViewById(R.id.nickname_button);
        mNicknameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUserName();
            }
        });

        mStatusMessageButton = view.findViewById(R.id.status_message_button);
        mStatusMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editStatusMessage();
            }
        });

        mImageBox = view.findViewById(R.id.imageBox);
        mImageBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openGallery(110);
            }
        });

        mThumbnail = (SimpleDraweeView) view.findViewById(R.id.thumbnail);
        if( mSession.getImage() != null ) {
            mThumbnail.setImageURI(Uri.parse(mSession.getImage()));
        }

        mTvNickname = (TextView) view.findViewById(R.id.nickname);
        if( null != mSession.getName() ) {
            mTvNickname.setText(mSession.getName());
        }

        mTvStatusMessage = (TextView) view.findViewById(R.id.status_message);
        if( null != mSession.getMessage() ) {
            mTvStatusMessage.setText(mSession.getMessage());
        }

        return view;
    }

    public void editUserName() {
        Intent localIntent = new Intent(mActivity, EditNickNameActivity.class);
        getActivity().startActivityForResult(localIntent, 103);
    }

    public void editStatusMessage() {
        Intent localIntent = new Intent(mActivity, EditStatusMessageActivity.class);
        getActivity().startActivityForResult(localIntent, 104);
    }

    public void openGallery(int req_code){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to use profile"), req_code);
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

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public String uploadImage(Uri uri){

        String downloadUrl = null;
        String url = mSession.getServerUrl()+"/upload";
        JSONObject userData = new JSONObject();

        try {
            userData.put( "U", mSession.getId() );
            userData.put( "D", mSession.getDeviceId() );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String realPath = RealPathUtil.getRealPath(mActivity, uri);

        File aFile = new File(realPath);

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", aFile.getName(), RequestBody.create(MEDIA_TYPE_PNG, aFile)).build();


        String appId = ApplicationController.getInstance().getAppId();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("XP-A", appId )
                .addHeader("XP-C", mSession.getId() +"^"+ appId)
                .addHeader("XP-U", userData.toString() )
                .addHeader("XP-FU-org",  aFile.getName())
                .addHeader("XP-FU-nm", aFile.getName().substring(0, aFile.getName().lastIndexOf(".") ) )
                .addHeader("XP-FU-tp", "image")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        com.squareup.okhttp.Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject res = new JSONObject( response.body().string() );
            JSONObject result = res.getJSONObject("result");

            String channel = result.getString("channel");
            String tname = result.getString("tname");

            downloadUrl = mSession.getServerUrl() + "/download/" + appId + "/" + channel + "/" + mSession.getId() + "/"+ApplicationController.getInstance().getClient().id() +"/"+tname;
            mJsonUserData.put("I", downloadUrl);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return downloadUrl;

    }

    private void updateProfile(){

        final Map<String,String> params = new HashMap<String, String>();

        params.put("A", getString(R.string.app_id));
        params.put("U", mSession.getId());
        params.put("DT", mJsonUserData.toString());
        params.put("PW", mSession.getPassword());
        params.put("D", mSession.getDeviceId());

        String url = getString(R.string.host_name)+"/user/update";

        StringRequest request = new StringRequest(url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Update user success ======================");
                        Log.d(TAG, response.toString());
                        try {
                            if( "ok".equalsIgnoreCase(response.getString("status")) ){
                                Log.d(TAG, response.getString("status"));

                                if( mJsonUserData.has("I") ) {
                                    mThumbnail.setImageURI(Uri.parse(mJsonUserData.getString("I")));
                                    mSession.setImage(mJsonUserData.getString("I"));
                                }
                                if( mJsonUserData.has("NM") ) {
                                    mSession.setName(mJsonUserData.getString("NM"));
                                }

                                if( mJsonUserData.has("MG") ) {
                                    mSession.setMessage(mJsonUserData.getString("MG"));
                                }
                                ApplicationController.getInstance().setXpushSession( mSession );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Update user error ======================");
                        error.printStackTrace();
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(mActivity);
        queue.add(request);
    }

    private class UploadImageTask extends AsyncTask<Void, Void, String> {
        Uri mUri;

        public UploadImageTask( Uri uri ){
            this.mUri = uri;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String uploadImageUrl = uploadImage(mUri);
            return uploadImageUrl;
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