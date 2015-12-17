package io.xpush.sampleChat.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.xpush.chat.core.CallbackEvent;
import io.xpush.sampleChat.R;
import io.xpush.chat.fragments.XPushChatFragment;
import io.xpush.sampleChat.activities.SelectFriendActivity;

public class ChatFragment extends XPushChatFragment {

    public static final String TAG = ChatFragment.class.getSimpleName();

    private ImageView mChatPlus;
    private RelativeLayout mHiddenPannel;
    private GridView mGridView;

    private Integer[] mThumbIds = { R.drawable.ic_photo_black, R.drawable.ic_camera_black };
    private Integer[] mTitles = { R.string.action_select_photo, R.string.action_take_photo };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_new, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mChatPlus = (ImageView) view.findViewById(R.id.action_chat_plus);

        mHiddenPannel = (RelativeLayout) view.findViewById(R.id.hidden_panel);

        mGridView = (GridView) view.findViewById(R.id.grid_chat_plus);
        mGridView.setAdapter(new ChatMenuAdapter());

        mChatPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mHiddenPannel.getVisibility() == View.GONE ){
                    mHiddenPannel.setVisibility(View.VISIBLE);
                } else {
                    mHiddenPannel.setVisibility(View.GONE);
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
            startActivityForResult(intent, 201);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 수행을 제대로 한 경우
        if( requestCode == 201 && resultCode == mActivity.RESULT_OK){
            Log.d(TAG, " =========== invite Result =========== ");
            ArrayList<String> userArrayList = data.getStringArrayListExtra("userArray");
            ArrayList<String> userNameArray = data.getStringArrayListExtra("userNameArray");

            if( userArrayList != null) {

                Log.d(TAG, userArrayList.toString() );
                Log.d(TAG, userNameArray.toString());

                mChannelCore.channelJoin(userArrayList, new CallbackEvent() {
                    @Override
                    public void call(Object... args) {

                    }
                });
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