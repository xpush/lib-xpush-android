package io.xpush.sampleChat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.InputStream;
import java.util.ArrayList;

import io.xpush.chat.models.XPushChannel;
import io.xpush.sampleChat.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String TAG = ImageViewerActivity.class.getSimpleName();

    ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    ViewPager viewPager;
    static ArrayList<String> mUriList = new ArrayList<String>();

    private Toolbar mToolbar;

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        public void onPageScrollStateChanged(int paramAnonymousInt) {
            if (paramAnonymousInt == 0) {
                ImageViewerActivity.this.updateState();
            }
        }

        public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2) {}

        public void onPageSelected(int paramAnonymousInt) {
            Log.d(TAG, String.valueOf(paramAnonymousInt));
        }
    };

    void updateState() {
        Log.d(TAG, String.valueOf(this.viewPager.getCurrentItem()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.title_photo)) ;
        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("imageUri");
        mUriList.add( imageUri );

        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(imageFragmentPagerAdapter);

        this.viewPager.addOnPageChangeListener(this.onPageChangeListener);
    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {
        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mUriList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return SwipeFragment.newInstance(position);
        }
    }

    public static class SwipeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.swipe_fragment, container, false);
            ImageView imageView = (ImageView) swipeView.findViewById(R.id.imageView);
            Bundle bundle = getArguments();
            int position = bundle.getInt("position");

            Log.d(TAG, mUriList.get(position));
            //imageView.setImageURI(Uri.parse(mUriList.get(position)));
            //imageView.setImageResource(R.drawable.intro_eagle);
            new DownloadImageTask( imageView  ).execute( mUriList.get(position) );

            return swipeView;
        }

        static SwipeFragment newInstance(int position) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }
    }

    static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}