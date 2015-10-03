package io.xpush.link.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.xpush.link.R;

public class UnreadyActivity extends AppCompatActivity  {
    private static final String TAG = UnreadyActivity.class.getSimpleName();
    private static final int REQUEST_REGISTER = 100;

    Button mBtnRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unready);

        mBtnRegister = (Button)findViewById(R.id.btn_register);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(), "Register success", Toast.LENGTH_LONG).show();
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}