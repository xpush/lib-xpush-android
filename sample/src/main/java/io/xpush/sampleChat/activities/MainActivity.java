package io.xpush.sampleChat.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import io.xpush.chat.R;
import io.xpush.chat.fragments.ChannelFragment;
import io.xpush.chat.fragments.FriendsFragment;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setHorizontalScrollBarEnabled(true);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    static class Adapter extends FragmentPagerAdapter {
        int oldPosition = -1;
        private HashMap<Integer, Fragment> mFragments = new HashMap<>();
        private Map<Integer, String> mIdMap;

        private Toolbar mToolbar;

        private Context mContext;

        public Adapter(FragmentManager fm, Activity activity) {
            super(fm);
            mContext = activity;

            mIdMap = new HashMap<Integer, String>();

            mIdMap.put( 0, "Users");
            mIdMap.put( 1, "Channels");
        }

        @Override
        public Fragment getItem(int position) {
            String menuId = mIdMap.get(position);

            Fragment f = null;

            if (mFragments.get(position) == null) {

                if( position == 0 ) {
                    f = new FriendsFragment();
                } else if( position == 1 ){
                    f = new ChannelFragment();
                }
            } else {
                mFragments.get( position );
            }
            return f;
        }

        @Override
        public int getCount() {
            return mIdMap.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mIdMap.get(position );
        }
    }
}