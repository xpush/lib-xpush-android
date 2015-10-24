package io.xpush.sampleChat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import io.xpush.chat.util.XPushUtils;
import io.xpush.sampleChat.R;

public class EditNickNameActivity extends FragmentActivity implements TextWatcher{

    private TextView mUserName = null;
    private TextView mTextCount;

    private Context mActivity;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.edit_nickname);

        mActivity = this;

        mUserName = ((EditText)findViewById(R.id.user_name));
        mTextCount = ((TextView)findViewById(R.id.text_count));

        mUserName.addTextChangedListener(this);
        mUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int paramInt, KeyEvent paramKeyEvent) {

                if ((paramInt == EditorInfo.IME_ACTION_DONE ) || ((paramKeyEvent != null) && (paramKeyEvent.getKeyCode() == 66))) {
                    Intent intent = new Intent();
                    intent.putExtra("nickname", mUserName.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String strLen = XPushUtils.getInputStringLength( editable.toString(), 20 );
        mTextCount.setText( strLen );
    }
}
