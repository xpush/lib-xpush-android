package io.xpush.link.activities;

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

import io.xpush.chat.ApplicationController;
import io.xpush.chat.core.XPushCore;
import io.xpush.link.R;

public class IntroActivity extends AppCompatActivity {

    static final int NUM_ITEMS = 4;
    ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    ViewPager viewPager;
    public static final String[] IMAGE_NAME = {"intro01", "intro02", "intro03", "intro04"};

    ImageView[] dots;
    Button button;


    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        public void onPageScrollStateChanged(int paramAnonymousInt)
        {
            if (paramAnonymousInt == 0) {
                IntroActivity.this.updateState();
            }
        }

        public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2) {}

        public void onPageSelected(int paramAnonymousInt)
        {
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

    void updateState()
    {
        if (this.viewPager.getCurrentItem() + 1 == this.NUM_ITEMS) {
            this.button.setVisibility(View.VISIBLE);
            return;
        }
        this.button.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        this.dots = new ImageView[] { (ImageView)findViewById(R.id.dot1), (ImageView)findViewById(R.id.dot2), (ImageView)findViewById(R.id.dot3), (ImageView)findViewById(R.id.dot4) };
        this.dots[0].setImageResource(R.drawable.page_on);


        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(imageFragmentPagerAdapter);

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("SHOW_INTRO", true);
                editor.commit();

                Intent intent = null;

                if (null == XPushCore.getInstance().getXpushSession()) {
                    intent = new Intent(IntroActivity.this, LoginActivity.class);
                } else {
                    if( pref.getBoolean("SITE_READY", false) ) {
                        intent = new Intent(IntroActivity.this, MainActivity.class);
                    } else {
                        intent = new Intent(IntroActivity.this, UnreadyActivity.class);
                    }
                }
                startActivity(intent);
                finish();
            }
        });

        this.viewPager.addOnPageChangeListener(this.onPageChangeListener);
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
            SwipeFragment fragment = new SwipeFragment();
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