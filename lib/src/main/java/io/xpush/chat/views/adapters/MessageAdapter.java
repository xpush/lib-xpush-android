package io.xpush.chat.views.adapters;

/**
 * Created by 정진영 on 2015-08-08.
 */
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.util.DateUtils;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<XPushMessage> mXpushMessages;
    private int[] mUsernameColors;

    public MessageAdapter(Context context, List<XPushMessage> xpushMessages) {
        mXpushMessages = xpushMessages;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        if (viewType == XPushMessage.TYPE_SEND_MESSAGE ) {
            layout = R.layout.item_send_message;
        } else if (viewType == XPushMessage.TYPE_RECEIVE_MESSAGE ) {
            layout = R.layout.item_receive_message;
        } else if( viewType == XPushMessage.TYPE_LOG ) {
            layout = R.layout.item_log;
        } else if( viewType == XPushMessage.TYPE_ACTION ) {
            layout = R.layout.item_action;
        }

        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        XPushMessage xpushMessage = mXpushMessages.get(position);
        viewHolder.setMessage(xpushMessage.getMessage());
        viewHolder.setUsername(xpushMessage.getUsername());
        viewHolder.setIime(xpushMessage.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return mXpushMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mXpushMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvUser;
        private TextView mTvMessage;
        private TextView mTvTime;

        public ViewHolder(View itemView) {
            super(itemView);

            mTvUser = (TextView) itemView.findViewById(R.id.tvUser);
            mTvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            mTvTime = (TextView) itemView.findViewById(R.id.tvTime);
        }

        public void setUsername(String username) {
            if (null == mTvUser) return;
            mTvUser.setText(username);
            mTvUser.setTextColor(getUsernameColor(username));
        }

        public void setMessage(String message) {
            if (null == mTvMessage) return;
            mTvMessage.setText(message);
        }

        public void setIime(long timestamp) {
            if (null == mTvTime) return;
            mTvTime.setText(DateUtils.getDate(timestamp, "a h:mm"));
        }

        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }
    }
}