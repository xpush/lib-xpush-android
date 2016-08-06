package io.xpush.chat.view.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.util.ContentUtils;
import io.xpush.chat.util.DateUtils;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private List<XPushMessage> mXPushMessages;
    private static MessageClickListener mMessageClickListener;
    private Context mContext;

    public MessageListAdapter(Context context, List<XPushMessage> xpushMessages) {
        mContext = context;
        mXPushMessages = xpushMessages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        if (viewType == XPushMessage.TYPE_SEND_MESSAGE ) {
            layout = R.layout.item_send_message;
        } else if (viewType == XPushMessage.TYPE_RECEIVE_MESSAGE ) {
            layout = R.layout.item_receive_message;
        } else if (viewType == XPushMessage.TYPE_INVITE || viewType == XPushMessage.TYPE_LEAVE ) {
            layout = R.layout.item_invite_message;
        } else if (viewType == XPushMessage.TYPE_SEND_IMAGE ) {
            layout = R.layout.item_send_image;
        } else if (viewType == XPushMessage.TYPE_RECEIVE_IMAGE ) {
            layout = R.layout.item_receive_image;
        }

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        XPushMessage xpushMessage = mXPushMessages.get(position);

        viewHolder.setUsername(xpushMessage.getSenderName());
        viewHolder.setTime(xpushMessage.getUpdated());
        viewHolder.setImage(xpushMessage.getImage());
        viewHolder.setMessage(xpushMessage.getMessage(), xpushMessage.getType(), xpushMessage.getMetadata());
    }

    @Override
    public int getItemCount() {
        return mXPushMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mXPushMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView tvUser;

        private TextView tvTime;
        private SimpleDraweeView thumbNail;
        private View vMessage;
        private View vClickableView;

        private ViewHolder(View itemView) {
            super(itemView);

            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            thumbNail = (SimpleDraweeView) itemView.findViewById(R.id.thumbnail);
            vMessage = (View) itemView.findViewById(R.id.vMessage);
            vClickableView = (View)itemView.findViewById(R.id.bubble);
            vClickableView.setOnClickListener(this);
            vClickableView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position  = ViewHolder.super.getAdapterPosition();
            mMessageClickListener.onMessageClick(mXPushMessages.get(position).getMessage(), mXPushMessages.get(position).getType());
        }

        @Override
        public boolean onLongClick(View view) {
            int position  = ViewHolder.super.getAdapterPosition();
            mMessageClickListener.onMessageLongClick(mXPushMessages.get(position).getMessage(), mXPushMessages.get(position).getType());
            return true;
        }

        public void setUsername(String username) {
            if (null == tvUser) return;
            tvUser.setText(username);
        }

        public void setMessage(String message, int type, final JSONObject metatdata) {

            if (null == vMessage) return;
            if( type == XPushMessage.TYPE_SEND_IMAGE || type == XPushMessage.TYPE_RECEIVE_IMAGE ) {


                final ImageView imageView = ((ImageView) vMessage);
                if( metatdata != null ){
                    try {
                        int metaWidth = metatdata.getInt("W");
                        int metaHeight = metatdata.getInt("H");

                        if( metaWidth > 0 && metaHeight > 0 ) {
                            int results[] = ContentUtils.getActualImageSize(metaWidth, metaHeight, mContext);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(results[0], results[1]);
                            imageView.setLayoutParams(layoutParams);
                        }
                    } catch ( JSONException e ){
                        e.printStackTrace();
                    }
                }

                Glide.with(mContext)
                    .load(Uri.parse(message))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            int originalWidth = bitmap.getWidth();
                            int originalHeight =  bitmap.getHeight();

                            if( metatdata == null ) {
                                //int results[] = ContentUtils.getActualImageSize(originalWidth, originalHeight, mContext);
                                //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(results[0], results[1]);
                                //imageView.setLayoutParams(layoutParams);
                            }
                            imageView.setImageBitmap(bitmap);
                        }
                    });
            } else {
                ( (TextView) vMessage ).setText(message);
            }
        }

        public void setTime(long timestamp) {
            if (null == tvTime) return;
            tvTime.setText(DateUtils.getDate(timestamp, "a h:mm"));
        }

        public void setImage(String image) {
            if (null == image || null == thumbNail) return;
            thumbNail.setImageURI(Uri.parse(image));
        }
    }

    public void setOnItemClickListener(MessageClickListener clickListener) {
        MessageListAdapter.mMessageClickListener = clickListener;
    }

    public interface MessageClickListener  {
        public void onMessageClick(String message, int type);

        public void onMessageLongClick(String message, int type);
    }
}
