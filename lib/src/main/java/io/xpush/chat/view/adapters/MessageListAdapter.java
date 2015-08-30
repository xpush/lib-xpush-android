package io.xpush.chat.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.util.DateUtils;


public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<XPushMessage> mXPushMessages;

    public MessageListAdapter(Context context, List<XPushMessage> xpushMessages) {
        mXPushMessages = xpushMessages;
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

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        XPushMessage xpushMessage = mXPushMessages.get(position);
        viewHolder.setMessage(xpushMessage.getMessage());
        viewHolder.setUsername(xpushMessage.getSender());
        viewHolder.setIime(xpushMessage.getUpdated());
    }

    @Override
    public int getItemCount() {
        return mXPushMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mXPushMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvUser;
        private TextView tvMessage;
        private TextView tvTime;
        private SimpleDraweeView thumbNail;

        private ViewHolder(View itemView) {
            super(itemView);

            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            thumbNail = (SimpleDraweeView) itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "position = " + getPosition(), Toast.LENGTH_SHORT).show();
        }


        public void setUsername(String username) {
            if (null == tvUser) return;
            tvUser.setText(username);
        }

        public void setMessage(String message) {
            if (null == tvMessage) return;
            tvMessage.setText(message);
        }

        public void setIime(long timestamp) {
            if (null == tvTime) return;
            tvTime.setText(DateUtils.getDate(timestamp, "a h:mm"));
        }
    }
}
