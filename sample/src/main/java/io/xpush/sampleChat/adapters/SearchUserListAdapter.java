package io.xpush.sampleChat.adapters;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.xpush.sampleChat.R;
import io.xpush.chat.models.XPushUser;
import io.xpush.sampleChat.fragments.SearchUserFragment;


public class SearchUserListAdapter extends RecyclerView.Adapter<SearchUserListAdapter.ViewHolder> {

    private static final String TAG = SearchUserListAdapter.class.getSimpleName();

    private SearchUserFragment mFragment;
    private List<XPushUser> mXpushUsers;
    private ArrayList<XPushUser> userList;
    private Handler mHandler;

    public SearchUserListAdapter(SearchUserFragment fragment, List<XPushUser> xpushUsers) {
        mXpushUsers = xpushUsers;
        mFragment = fragment;

        this.userList = new ArrayList<XPushUser>();
        this.userList.addAll(xpushUsers);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        XPushUser xpushUser = mXpushUsers.get(position);

        if( xpushUser.getImage() != null && !"".equals(xpushUser.getImage()) ) {
            viewHolder.thumbNail.setImageURI(Uri.parse(xpushUser.getImage()));
        }

        viewHolder.tvTitle.setText(xpushUser.getName());
        if( xpushUser.getMessage() != null && !"".equals( xpushUser.getMessage().trim() ) ) {
            viewHolder.tvMessage.setText(xpushUser.getMessage());
            viewHolder.llMessage.setVisibility(View.VISIBLE);
        }

        viewHolder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragment.addFriend(mXpushUsers.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mXpushUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvMessage;
        private SimpleDraweeView thumbNail;
        private SimpleDraweeView addButton;
        private LinearLayout llMessage;

        private ViewHolder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            llMessage= (LinearLayout) itemView.findViewById(R.id.ll_message);
            thumbNail = (SimpleDraweeView) itemView.findViewById(R.id.thumbnail);
            addButton  = (SimpleDraweeView) itemView.findViewById(R.id.btnAdd);
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