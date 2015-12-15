package io.xpush.sampleChat.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import io.xpush.chat.core.CallbackEvent;
import io.xpush.sampleChat.R;
import io.xpush.chat.fragments.XPushChatFragment;
import io.xpush.sampleChat.activities.SelectFriendActivity;

public class ChatFragment extends XPushChatFragment {

    public static final String TAG = ChatFragment.class.getSimpleName();

    private ImageView mChatPlus;
    private RelativeLayout mHiddenPannel;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_new, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mChatPlus = (ImageView) view.findViewById(R.id.action_chat_plus);

        mHiddenPannel = (RelativeLayout) view.findViewById(R.id.hidden_panel);

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

        Log.d(TAG, String.valueOf( requestCode ) );
        Log.d(TAG, String.valueOf(resultCode) );

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
}