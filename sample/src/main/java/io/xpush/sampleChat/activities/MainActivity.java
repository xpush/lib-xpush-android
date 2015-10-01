package io.xpush.sampleChat.activities;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Locale;

import io.xpush.chat.fragments.ChannelFragment;
import io.xpush.chat.view.SlidingTabLayout;
import io.xpush.sampleChat.R;
import io.xpush.sampleChat.fragments.FriendsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private Menu mMenu;
    private DrawerLayout mDrawerLayout;
    private Adapter mApdater;
    private FloatingActionButton fab;
    private EditText mEditSearch;

    private SearchView mSearchView;
    private int mPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mContext = this;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser();
            }
        });

        mToolbar.setTitle( getResources().getStringArray(R.array.tabs)[0] );
        setSupportActionBar(mToolbar);

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mApdater = new Adapter(getSupportFragmentManager());
        mViewPager.setAdapter(mApdater);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                mToolbar.setTitle(mApdater.getTitle(position));
                if (position == 0) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
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

        int icons[] = {R.drawable.ic_person, R.drawable.ic_chat};
        String[] tabText = getResources().getStringArray(R.array.tabs);

        public Adapter(FragmentManager fm) {
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

        //mMenu = menu;
        //getMenuInflater().inflate(R.menu.main_menu, menu);

        /**
        final MenuItem searchViewItem = mMenu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchViewItem.getActionView();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() > 2) {
                    filterData(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        mSearchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToolbar.setTitle("");
                mSearchView.setMaxWidth(10000);
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mToolbar.setTitle(mApdater.getTitle(mPosition));
                return false;
            }
        });

         */

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if( item.getItemId() == R.id.action_search_user ){
            searchUser();
        } else if( item.getItemId() == R.id.action_setting ){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if( item.getItemId() == R.id.action_profile ){
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        menuItem.setChecked(true);

                        if (menuItem.getItemId() == R.id.nav_swap) {

                        } else if (menuItem.getItemId() == R.id.nav_review) {

                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 수행을 제대로 한 경우
        if(requestCode == 110 && resultCode == RESULT_OK && data != null) {
            ( (FriendsFragment) mApdater.getItem(0) ).getUsers();
        }  else if(resultCode == RESULT_CANCELED){

        }
    }

    public void searchUser(){
        Intent intent = new Intent(MainActivity.this, SearchUserActivity.class);
        startActivityForResult(intent, 110);
    }
}