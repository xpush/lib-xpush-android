package io.xpush.chat.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushUser;

public class UserCursorAdapter extends CursorAdapter implements Filterable {

    private final LayoutInflater mInflater;

    public UserCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.fragment_friends_item, null);

        ViewHolder holder = new ViewHolder();
        holder.tvName = (TextView) view.findViewById(R.id.tvName);
        holder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        holder.thumbNail = (SimpleDraweeView) view.findViewById(R.id.thumbnail);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        XPushUser user = new XPushUser(cursor);

        if( user.getImage() != null && !"".equals(user.getImage()) ) {
            holder.thumbNail.setImageURI(Uri.parse(user.getImage()));
        }

        holder.tvName.setText(user.getName());
        if( user.getMessage() != null && !"".equals( user.getMessage().trim() ) ) {
            holder.tvMessage.setText(user.getMessage());
        } else {
            holder.tvMessage.setText("");
        }
    }

    public static class ViewHolder {
        private TextView tvName;
        private TextView tvMessage;
        private SimpleDraweeView thumbNail;
    }
}