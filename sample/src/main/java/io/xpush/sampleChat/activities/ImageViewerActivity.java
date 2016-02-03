package io.xpush.sampleChat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.xpush.sampleChat.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String TAG = ImageViewerActivity.class.getSimpleName();

    ArrayList<String> mImageList;
    private GalleryPagerAdapter mAdapter;
    private int mCurrentIndex;

    @Bind(R.id.tvIndicator)
    TextView mTvIndicator;

    @Bind(R.id.btnClose)
    ImageView mBtnClose;

    @Bind(R.id.viewPager)
    ViewPager mViewPager;

    @OnClick(R.id.btnClose)
    void close() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String selectedImage = intent.getStringExtra("selectedImage");
        mImageList = intent.getStringArrayListExtra("imageList");

        if( mImageList == null ) {
            mImageList = new ArrayList<String>();
            if (mImageList.indexOf(selectedImage) < 0) {
                mImageList.add(selectedImage);
            }
        }

        mAdapter = new GalleryPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(4);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentIndex  = position ;
                setIndicatior();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //init current
        if (mImageList.indexOf(selectedImage) > -1){
            mCurrentIndex = mImageList.indexOf(selectedImage);
            mViewPager.setCurrentItem( mCurrentIndex );
        }

        setIndicatior();
    }

    private void setIndicatior(){
        String text = String.format(Locale.US, "%1$d/%2$d", new Object[] { mCurrentIndex+1, mImageList.size() });
        mTvIndicator.setText( text );
    }

    class GalleryPagerAdapter extends PagerAdapter {

        Context _context;
        LayoutInflater _inflater;

        public GalleryPagerAdapter(Context context) {
            _context = context;
            _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = _inflater.inflate(R.layout.item_image_viewer, container, false);
            container.addView(itemView);

            final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) itemView.findViewById(R.id.image);
            Glide.with(_context)
                    .load(mImageList.get(position))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            imageView.setImage(ImageSource.bitmap(bitmap));
                        }
                    });

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}