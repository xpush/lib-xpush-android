package io.xpush.sampleChat.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.xpush.chat.common.Constants;
import io.xpush.chat.core.CallbackEvent;
import io.xpush.chat.core.XPushCore;
import io.xpush.chat.util.RealPathUtil;
import io.xpush.sampleChat.R;
import io.xpush.chat.fragments.XPushChatFragment;
import io.xpush.sampleChat.activities.SelectFriendActivity;

public class ChatFragment extends XPushChatFragment {

    public static final String TAG = ChatFragment.class.getSimpleName();

    @Bind(R.id.action_chat_plus)
    ImageView mChatPlus;

    @Bind(R.id.hidden_panel)
    RelativeLayout mHiddenPannel;

    @Bind(R.id.grid_chat_plus)
    GridView mGridView;

    private Integer[] mThumbIds = { R.drawable.ic_photo_black, R.drawable.ic_camera_black };
    private Integer[] mTitles = { R.string.action_select_photo, R.string.action_take_photo };

    private String mCurrentPhotoPath;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_new, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mGridView.setAdapter(new ChatMenuAdapter());

        mChatPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHiddenPannel.getVisibility() == View.GONE) {
                    mHiddenPannel.setVisibility(View.VISIBLE);
                    mChatPlus.setImageResource(R.drawable.ic_close_black);
                } else {
                    mHiddenPannel.setVisibility(View.GONE);
                    mChatPlus.setImageResource(R.drawable.ic_add_black);
                }

            }
        });

        mGridView.setNumColumns(mTitles.length);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                if (position == 0) {
                    openGallery();
                } else if (position == 1) {
                    takePicture();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_leave) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mActivity.getString(R.string.action_leave_dialog_title))
                    .setMessage(mActivity.getString(R.string.action_leave_dialog_description))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            channelLeave();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }

                    });

            AlertDialog dialog = builder.create();    // 알림창 객체 생성
            dialog.show();    // 알림창 띄우기

            return true;
        } else if (id == R.id.action_invite) {
            Intent intent = new Intent(mActivity, SelectFriendActivity.class);
            intent.putExtra("channelId", mChannel);
            intent.putParcelableArrayListExtra("channelUsers", mXpushUsers);
            startActivityForResult(intent, Constants.REQUEST_INVITE_USER);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 수행을 제대로 한 경우
        if( resultCode == mActivity.RESULT_OK){
            if( requestCode == Constants.REQUEST_INVITE_USER  ) {
                ArrayList<String> userArrayList = data.getStringArrayListExtra("userArray");
                ArrayList<String> userNameArray = data.getStringArrayListExtra("userNameArray");

                if( userArrayList != null) {

                    mChannelCore.channelJoin(userArrayList, new CallbackEvent() {
                        @Override
                        public void call(Object... args) {

                        }
                    });
                }
            } else if ( requestCode == Constants.REQUEST_IMAGE_SELECT ){

                mHiddenPannel.setVisibility(View.GONE);
                mChatPlus.setImageResource(R.drawable.ic_add_black);

                Uri selectedImageUri = data.getData();

                String realPath = RealPathUtil.getRealPath(mActivity, selectedImageUri);
                if( RealPathUtil.isImagePath( realPath ) ) {
                    UploadImageTask imageUpload = new UploadImageTask(selectedImageUri);
                    imageUpload.execute();
                } else {
                    Toast.makeText(mActivity, getString(R.string.success_message_signup), Toast.LENGTH_SHORT).show();
                }

            } else if ( requestCode == Constants.REQUEST_IMAGE_CAPTURE ){

                mHiddenPannel.setVisibility(View.GONE);
                mChatPlus.setImageResource(R.drawable.ic_add_black);

                galleryAddPic();
                UploadImageTask imageUpload = new UploadImageTask(Uri.parse(mCurrentPhotoPath));
                imageUpload.execute();
            }
        }
    }

    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file to use profile"), Constants.REQUEST_IMAGE_SELECT);
    }

    private void takePicture(){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(intent,  Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }

    private class UploadImageTask extends AsyncTask<Void, Void, String> {
        Uri mUri;

        public UploadImageTask( Uri uri ){
            this.mUri = uri;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String downloadUrl = XPushCore.getInstance().uploadImage(mUri);
            return downloadUrl;
        }

        @Override
        protected  void onPostExecute(final String imageUrl){
            super.onPostExecute(imageUrl);
            if( imageUrl != null ){

                Log.d(TAG, " Upload result imageUrl : " + imageUrl );

                if( mChannelCore != null ){
                    mChannelCore.sendMessage( imageUrl, "IM" );
                }
            }
        }
    }

    public class ChatMenuAdapter extends BaseAdapter{
        LayoutInflater inflater;

        public ChatMenuAdapter(){
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public final int getCount(){
            return mThumbIds.length;
        }

        public final Object getItem(int position){
            return null;
        }

        public final long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ChatMenuItemViewHolder itemViewHolder;

            if( convertView == null ){
                convertView = inflater.inflate(R.layout.item_chat_plus, parent, false);
                itemViewHolder = new ChatMenuItemViewHolder(convertView);
                convertView.setTag(itemViewHolder);
            } else {
                itemViewHolder = (ChatMenuItemViewHolder) convertView.getTag();
            }

            itemViewHolder.ivIcon.setImageResource( mThumbIds[position] );
            itemViewHolder.tvTitle.setText( getString(mTitles[position] ) );

            return convertView;
        }
    }

    private class ChatMenuItemViewHolder {
        ImageView ivIcon;
        TextView tvTitle;

        public ChatMenuItemViewHolder(View item) {
            ivIcon = (ImageView) item.findViewById(R.id.thumbImage);
            tvTitle = (TextView) item.findViewById(R.id.thumbTitle);
        }
    }
}