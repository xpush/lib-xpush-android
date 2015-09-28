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

import io.xpush.chat.ApplicationController;
import io.xpush.chat.models.XPushSession;
import io.xpush.chat.util.RealPathUtil;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.activities.EditProfileNameActivity;

public class ProfileFragment extends Fragment {

    private String TAG = ProfileFragment.class.getSimpleName();
    private Context mActivity;
    private View nicknameButton;
    private View mImageBox;
    private SimpleDraweeView thumbnail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
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

        mImageBox = view.findViewById(R.id.imageBox);
        mImageBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openGallery(110);
            }
        });

        thumbnail = (SimpleDraweeView) view.findViewById(R.id.thumbnail);

        return view;
    }

    public void editUserName() {
        Intent localIntent = new Intent(mActivity, EditProfileNameActivity.class);
        getActivity().startActivityForResult(localIntent, 103);
    }

    public void openGallery(int req_code){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to use profile"), req_code);
    }

    public void setImage(Uri uri){
        //thumbnail.setImageURI(uri);
        UploadImageTask imageUpload = new UploadImageTask(uri);
        imageUpload.execute();
    }

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public void uploadImage(Uri uri){

        XPushSession session = ApplicationController.getInstance().getXpushSession();
        String url = session.getServerUrl()+"/upload";

        JSONObject userData = new JSONObject();

        try {
            userData.put( "U", session.getId() );
            userData.put( "D", session.getDeviceId() );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String realPath = RealPathUtil.getRealPath(mActivity, uri);

        File aFile = new File(realPath);

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", aFile.getName(), RequestBody.create(MEDIA_TYPE_PNG, aFile)).build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("XP-A", ApplicationController.getInstance().getAppId() )
                .addHeader("XP-C", session.getId())
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
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class UploadImageTask extends AsyncTask<Void, Void, Void> {
        Uri mUri;

        public UploadImageTask( Uri uri ){
            this.mUri = uri;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            uploadImage(mUri );
            return null;
        }

        @Override
        protected  void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
        }
    }
}
