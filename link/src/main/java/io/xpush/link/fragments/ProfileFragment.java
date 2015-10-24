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
import android.widget.Button;
import android.widget.EditText;

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
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.network.StringRequest;
import io.xpush.chat.util.RealPathUtil;
import io.xpush.link.R;

public class ProfileFragment extends Fragment {

    private String TAG = ProfileFragment.class.getSimpleName();
    private Context mActivity;

    private View mImageBox;
    private SimpleDraweeView mThumbnail;
    private XPushSession mSession;

    private EditText mUserName;
    private EditText mStatusMessage;
    private EditText mEmail;

    private JSONObject mJsonUserData;

    private Button mSaveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mSession = XPushCore.getInstance().getXpushSession();
        mJsonUserData = mSession.getUserData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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

        mUserName = (EditText) view.findViewById(R.id.userName);
        if( null != mSession.getName() ) {
            mUserName.setText(mSession.getName());
        }

        mStatusMessage = (EditText) view.findViewById(R.id.statusMessage);
        if( null != mSession.getName() ) {
            mStatusMessage.setText(mSession.getMessage());
        }

        mEmail = (EditText) view.findViewById(R.id.email);
        if( null != mSession.getEmail() ) {
            mEmail.setText(mSession.getEmail());
        }

        mSaveButton = (Button) view.findViewById(R.id.btn_save);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(mJsonUserData);
            }
        });

        return view;
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


        String appId = XPushCore.getInstance().getAppId();

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

            downloadUrl = mSession.getServerUrl() + "/download/" + appId + "/" + channel + "/" + mSession.getId() + "/"+tname;
            mJsonUserData.put("I", downloadUrl);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return downloadUrl;
    }

    private void updateProfile(final JSONObject jsonData){

        try {
            if( jsonData != null ) {
                jsonData.put("NM", mUserName.getText());
                jsonData.put("MG", mStatusMessage.getText());
                jsonData.put("EM", mEmail.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Map<String,String> params = new HashMap<String, String>();

        params.put("A", getString(R.string.app_id));
        params.put("U", mSession.getId());
        params.put("DT", jsonData.toString());
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

                                if( jsonData.has("I") ) {
                                    mSession.setImage(jsonData.getString("I"));
                                }
                                if( jsonData.has("NM") ) {
                                    mSession.setName(jsonData.getString("NM"));
                                }

                                if( jsonData.has("MG") ) {
                                    mSession.setMessage(jsonData.getString("MG"));
                                }

                                if( jsonData.has("EM") ) {
                                    mSession.setEmail(jsonData.getString("EM"));
                                }
                                XPushCore.getInstance().setXpushSession( mSession );
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
            mThumbnail.setImageURI(Uri.parse(imageUrl));
            super.onPostExecute(imageUrl);
        }
    }
}