package io.xpush.sampleChat.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.nkzawa.socketio.client.Ack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.xpush.chat.ApplicationController;
import io.xpush.sampleChat.R;
import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.models.XPushUser;
import io.xpush.chat.util.DateUtils;


public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private static final String TAG = UserListAdapter.class.getSimpleName();

    private List<XPushUser> mXpushUsers;
    private ArrayList<XPushUser> userList;
    private Handler mHandler;

    public UserListAdapter(Context context, List<XPushUser> xpushUsers, Handler handler) {
        mXpushUsers = xpushUsers;
        mHandler = handler;

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
        } else {
            viewHolder.tvMessage.setText("");
        }

        viewHolder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(mXpushUsers.get( position ).getId() );
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

    public void addFriend(String userId){
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            array.put( userId );

            jsonObject.put("GR", ApplicationController.getInstance().getXpushSession().getId()  );
            jsonObject.put("U", array);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, jsonObject.toString() );

        ApplicationController.getInstance().getClient().emit("group-add", jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                Log.d(TAG, response.toString());

                mHandler.sendEmptyMessage(0);
            }
        });
    }
}