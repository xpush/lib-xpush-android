package io.xpush.chat.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        } else if (viewType == XPushMessage.TYPE_INVITE ) {
            layout = R.layout.item_invite_message;
        } else if (viewType == XPushMessage.TYPE_SEND_IMAGE ) {
            layout = R.layout.item_send_image;
        } else if (viewType == XPushMessage.TYPE_RECEIVE_IMAGE ) {
            layout = R.layout.item_invite_message;
        }

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        XPushMessage xpushMessage = mXPushMessages.get(position);

        viewHolder.setUsername(xpushMessage.getSenderName());
        viewHolder.setIime(xpushMessage.getUpdated());
        viewHolder.setImage(xpushMessage.getImage());
        viewHolder.setMessage(xpushMessage.getMessage(), xpushMessage.getType());
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

        private TextView tvTime;
        private SimpleDraweeView thumbNail;
        private View vMessage;

        private ViewHolder(View itemView) {
            super(itemView);

            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            thumbNail = (SimpleDraweeView) itemView.findViewById(R.id.thumbnail);
            vMessage = (View) itemView.findViewById(R.id.vMessage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }


        public void setUsername(String username) {
            if (null == tvUser) return;
            tvUser.setText(username);
        }

        public void setMessage(String message, int type) {

            Log.d("TAG", "33333");

            if (null == vMessage) return;
            if( type == XPushMessage.TYPE_SEND_IMAGE || type == XPushMessage.TYPE_RECEIVE_IMAGE ) {

                Log.d("TAG", "2222222");
                Log.d("TAG", Uri.parse(message).toString());
                ( (ImageView) vMessage ).setImageURI(Uri.parse(message));
            } else {
                ( (TextView) vMessage ).setText(message);
            }
        }

        public void setIime(long timestamp) {
            if (null == tvTime) return;
            tvTime.setText(DateUtils.getDate(timestamp, "a h:mm"));
        }

        public void setImage(String image) {
            if (null == image || null == thumbNail) return;
            thumbNail.setImageURI(Uri.parse(image));
        }
    }
}
