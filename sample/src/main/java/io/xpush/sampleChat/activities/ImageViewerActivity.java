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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

import io.xpush.sampleChat.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String TAG = ImageViewerActivity.class.getSimpleName();
    
    ViewPager mViewPager;
    static ArrayList<String> mImageList;
    ImageView mBtnClose;
    private GalleryPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        Intent intent = getIntent();
        String selectedImage = intent.getStringExtra("selectedImage");
        mImageList = intent.getStringArrayListExtra("imageList");

        if( mImageList == null ) {
            mImageList = new ArrayList<String>();
            if (mImageList.indexOf(selectedImage) < 0) {
                mImageList.add(selectedImage);
            }
        }

        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mAdapter = new GalleryPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(4);

        //init current
        if (mImageList.indexOf(selectedImage) > -1){
            mViewPager.setCurrentItem(mImageList.indexOf(selectedImage));
        }

        mBtnClose = (ImageView) findViewById(R.id.btnClose);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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

            // Asynchronously load the image and set the thumbnail and pager view
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