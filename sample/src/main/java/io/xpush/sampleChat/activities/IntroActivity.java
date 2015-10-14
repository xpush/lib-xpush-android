package io.xpush.sampleChat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.xpush.chat.ApplicationController;
import io.xpush.sampleChat.R;

public class IntroActivity extends AppCompatActivity {

    static final int NUM_ITEMS = 4;
    ImageFragmentPagerAdapter imageFragmentPagerAdapter;

    public static final String[] IMAGE_NAME = {"intro_bear", "intro_bonobo", "intro_eagle", "intro_horse"};

    ImageView[] dots;

    @Bind(R.id.btn_start)
    Button mBtnStart;

    @Bind(R.id.viewPager)
    ViewPager mViewPager;

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        public void onPageScrollStateChanged(int paramAnonymousInt) {
            if (paramAnonymousInt == 0) {
                IntroActivity.this.updateState();
            }
        }

        public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2) {}

        public void onPageSelected(int paramAnonymousInt) {
            ImageView[] arrayOfImageView = IntroActivity.this.dots;
            int j = arrayOfImageView.length;
            int i = 0;
            while (i < j) {
                arrayOfImageView[i].setImageResource(R.drawable.page_off);
                i += 1;
            }
            IntroActivity.this.dots[paramAnonymousInt].setImageResource(R.drawable.page_on);
        }
    };

    @OnClick(R.id.btn_start)
    public void start() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("SHOW_INTRO", true);
        editor.commit();

        Intent intent = null;

        if (null == ApplicationController.getInstance().getXpushSession()) {
            intent = new Intent(IntroActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(IntroActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    void updateState() {
        if (mViewPager.getCurrentItem() + 1 == this.NUM_ITEMS) {
            mBtnStart.setVisibility(View.VISIBLE);
            return;
        }
        mBtnStart.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        this.dots = new ImageView[] { (ImageView)findViewById(R.id.dot1), (ImageView)findViewById(R.id.dot2), (ImageView)findViewById(R.id.dot3), (ImageView)findViewById(R.id.dot4) };
        this.dots[0].setImageResource(R.drawable.page_on);


        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(imageFragmentPagerAdapter);

        mViewPager.addOnPageChangeListener(this.onPageChangeListener);
    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {
        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
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
            String imageFileName = IMAGE_NAME[position];
            int imgResId = getResources().getIdentifier(imageFileName, "drawable", getActivity().getPackageName() );
            imageView.setImageResource(imgResId);
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
}