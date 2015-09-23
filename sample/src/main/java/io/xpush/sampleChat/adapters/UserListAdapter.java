package io.xpush.sampleChat.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.xpush.sampleChat.R;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.util.DateUtils;


public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private List<XPushUser> mXpushUsers;
    private ArrayList<XPushUser> userList;

    public UserListAdapter(Context context, List<XPushUser> xpushUsers) {
        mXpushUsers = xpushUsers;

        this.userList = new ArrayList<XPushUser>();
        this.userList.addAll(xpushUsers);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        XPushUser xpushUser = mXpushUsers.get(position);

        if( xpushUser.getImage() != null && !"".equals(xpushUser.getImage()) ) {
            viewHolder.thumbNail.setImageURI(Uri.parse(xpushUser.getImage()));
        }

        viewHolder.tvTitle.setText(xpushUser.getName());
        if( xpushUser.getMessage() != null && !"".equals( xpushUser.getMessage().trim() ) ) {
            viewHolder.tvMessage.setText(xpushUser.getMessage());
            viewHolder.llMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mXpushUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvTitle;
        private TextView tvMessage;
        private SimpleDraweeView thumbNail;
        private LinearLayout llMessage;

        private ViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            llMessage= (LinearLayout) itemView.findViewById(R.id.ll_message);
            thumbNail = (SimpleDraweeView) itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "position = " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mXpushUsers.clear();

        if (charText.length() == 0) {
            mXpushUsers.addAll(userList);
        } else {
            for (XPushUser u : userList) {
                if (u.getName().toLowerCase(Locale.getDefault()).contains(charText) || u.getId().toLowerCase(Locale.getDefault()).contains(charText) ) {
                    mXpushUsers.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void resetUsers(){
        this.userList = new ArrayList<XPushUser>();
        this.userList.addAll(mXpushUsers);
    }
}
