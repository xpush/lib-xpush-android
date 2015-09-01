package io.xpush.chat.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushChannel;
import io.xpush.chat.util.DateUtils;

public class ChannelCursorAdapter extends CursorAdapter {

    private final LayoutInflater mInflater;
    private Context context;
    private List<XPushChannel> objects;

    public ChannelCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.fragment_channel_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        TextView tvDate = (TextView) view.findViewById(R.id.tvDate);
        TextView tvCount = (TextView) view.findViewById(R.id.tvCount);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);

        SimpleDraweeView thumbNail = (SimpleDraweeView) view.findViewById(R.id.thumbnail);

        XPushChannel xpushChannel = new XPushChannel(cursor);

        String title = xpushChannel.getId();
        long date = xpushChannel.getUpdated();

        if( xpushChannel.getImage() != null && !"".equals(xpushChannel.getImage()) ) {
            thumbNail.setImageURI(Uri.parse(xpushChannel.getImage()));
        }

        tvMessage.setText(xpushChannel.getMessage());

        if( xpushChannel.getCount() > 0 ) {
            tvCount.setText(String.valueOf(xpushChannel.getCount()));
            tvCount.setVisibility(View.VISIBLE);
        } else {
            tvCount.setText(String.valueOf(xpushChannel.getCount()));
            tvCount.setVisibility(View.INVISIBLE);
        }
        tvDate.setText(DateUtils.getTimeString(date) );

        tvTitle.setText(title);
    }
}