package io.xpush.sampleChat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import io.xpush.chat.ApplicationController;
import io.xpush.chat.view.SlidingTabLayout;
import io.xpush.sampleChat.R;
import io.xpush.chat.fragments.ChannelFragment;
import io.xpush.chat.fragments.UsersFragment;
import io.xpush.sampleChat.fragments.FriendsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext;

    private ViewPager mViewPager;

    private Toolbar mToolbar;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle( getResources().getStringArray(R.array.tabs)[0] );
        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final Adapter apater = new Adapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(apater);

        SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        tabLayout.setDistributeEvenly(true);
        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabIndicator);
            }
        });
        tabLayout.setCustomTabView(R.layout.custom_tab_view, R.id.tabText);
        tabLayout.setViewPager(mViewPager);

        tabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
            }

            @Override
            public void onPageSelected(int position){
                mToolbar.setTitle(apater.getTitle(position) );
            }

            @Override
            public void onPageScrollStateChanged(int state){
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    class Adapter extends FragmentPagerAdapter {

        int icons[] = {R.drawable.ic_chat, R.drawable.ic_person};
        String[] tabText = getResources().getStringArray(R.array.tabs);

        private Toolbar mToolbar;
        private Context mContext;

        public Adapter(FragmentManager fm, Activity activity) {
            super(fm);
            tabText = getResources().getStringArray(R.array.tabs);

            android.util.Log.d(TAG, tabText.toString());
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f = null;
            switch (position) {
                case 0:
                    f = new FriendsFragment();
                    break;
                case 1:
                    f = new ChannelFragment();
                    break;
            }
            return f;

        }

        @Override
        public int getCount() {
            return tabText.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable drawable = getResources().getDrawable(icons[position]);
            drawable.setBounds(0,0,64,64);
            ImageSpan imageSpan = new ImageSpan(drawable);
            SpannableString spannableString = new SpannableString(" ");
            spannableString.setSpan(imageSpan,0,spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }

        public String getTitle(int position){
            return tabText[position];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        mMenu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else if( item.getItemId() == R.id.action_setting ){

            Intent intent = intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}