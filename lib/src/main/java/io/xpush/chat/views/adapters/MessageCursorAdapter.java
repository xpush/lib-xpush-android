package io.xpush.chat.views.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.persist.MessageTable;
import io.xpush.chat.util.DateUtils;

import com.facebook.drawee.view.SimpleDraweeView;

public class MessageCursorAdapter extends CursorAdapter {

    private final LayoutInflater mInflater;
    private Context context;
    private List<XPushChannel> objects;
    private String charset;
    private String publisher;

    public MessageCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private int getItemViewType(Cursor cursor) {
        int type = 0;
        type = cursor.getInt(cursor.getColumnIndex(MessageTable.KEY_TYPE));
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor);

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

        View view =  mInflater.inflate(layout, null);
        ViewHolder holder = new ViewHolder();

        holder.tvTime = (TextView) view.findViewById(R.id.tvTime);
        holder.tvUser = (TextView) view.findViewById(R.id.tvUser);
        holder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        holder.thumbNail = (SimpleDraweeView) view.findViewById(R.id.thumbnail);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();
        XPushMessage xpushMessage = new XPushMessage(cursor);

        String userId = xpushMessage.getUsername();
        long date = xpushMessage.getTimestamp();

        if( holder.thumbNail != null && xpushMessage.getImage() != null && !"".equals(xpushMessage.getImage()) ) {
            holder.thumbNail.setImageURI(Uri.parse(xpushMessage.getImage()));
        }

        holder.tvMessage.setText(xpushMessage.getMessage());
        holder.tvTime.setText( DateUtils.getDate(date, "a h:mm") );
        holder.tvMessage.setText(userId);
    }

    public static class ViewHolder {
        private TextView tvUser;
        private TextView tvMessage;
        private TextView tvTime;
        public SimpleDraweeView thumbNail;
    }
}