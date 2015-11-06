package io.xpush.chat.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import io.xpush.chat.R;
import io.xpush.chat.models.XPushUser;

public class UserCursorAdapter extends CursorAdapter implements Filterable {

    private final LayoutInflater mInflater;
    private boolean checkable;

    public enum Mode{
        NORMAL, CHECKABLE
    }

    public UserCursorAdapter(Context context, Cursor cursor, int flags) {
        this(context, cursor, flags, Mode.NORMAL);
    }

    public UserCursorAdapter(Context context, Cursor cursor, int flags, Mode mode) {
        super(context, cursor, flags);
        if( mode == Mode.CHECKABLE ) {
            checkable = true;
        } else {
            checkable = false;
        }
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.fragment_friends_item, null);

        ViewHolder holder = new ViewHolder();
        holder.tvName = (TextView) view.findViewById(R.id.tvName);
        holder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        holder.thumbNail = (SimpleDraweeView) view.findViewById(R.id.thumbnail);
        holder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        if( checkable ){
            holder.checkBox.setVisibility(View.VISIBLE);
        }

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
        private CheckBox checkBox;
    }
}