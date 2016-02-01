package io.xpush.sampleChat.activities;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.util.ArrayList;
import java.util.HashMap;

import io.xpush.sampleChat.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String TAG = ImageViewerActivity.class.getSimpleName();

    ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    ViewPager viewPager;
    static ArrayList<String> mUriList = new ArrayList<String>();
    ImageView mBtnClose;

    private String mCurrentUrl;

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        public void onPageScrollStateChanged(int paramAnonymousInt) {
            if (paramAnonymousInt == 0) {
                ImageViewerActivity.this.updateState();
            }
        }

        public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2) {}

        public void onPageSelected(int paramAnonymousInt) {
        }
    };

    void updateState() {
        Log.d(TAG, String.valueOf(this.viewPager.getCurrentItem()));
        Fragment f = imageFragmentPagerAdapter.getItem( this.viewPager.getCurrentItem() );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("imageUri");
        if( mUriList.indexOf( imageUri  ) < 0 ) {
            mUriList.add(imageUri);
        }

        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(imageFragmentPagerAdapter);
        this.viewPager.addOnPageChangeListener(this.onPageChangeListener);

        mBtnClose = (ImageView) findViewById(R.id.btnClose);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String mUrl = getIntent().getStringExtra("imageUri");
    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {

        private HashMap<Integer, Fragment> mFragments = new HashMap<>();

        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mUriList.size();
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments.get(position) == null) {
                Fragment f = SwipeFragment.newInstance(position);
                mFragments.put( position, f );
            }

            return mFragments.get(position);
        }
    }

    public static class SwipeFragment extends Fragment implements View.OnTouchListener {
        // These matrices will be used to scale points of the image
        Matrix matrix = new Matrix();
        Matrix savedMatrix = new Matrix();
        Matrix mMinScaleMatrix = new Matrix();

        // The 3 states (events) which the user is trying to perform
        static final int NONE = 0;
        static final int DRAG = 1;
        static final int ZOOM = 2;
        int mode = NONE;

        // these PointF objects are used to record the point(s) the user is touching
        PointF start = new PointF();
        PointF mid = new PointF();
        float oldDist = 1f;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.swipe_fragment, container, false);
            final ImageView imageView = (ImageView) swipeView.findViewById(R.id.imageView);

            imageView.setOnTouchListener(this);
            Bundle bundle = getArguments();
            final int position = bundle.getInt("position");

            Glide.with(this)
                    .load(mUriList.get(position))
                    .fitCenter()
                    .crossFade()
                    .into(new GlideDrawableImageViewTarget(imageView) {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            if (position == 0) {
                                mMinScaleMatrix = new Matrix(imageView.getImageMatrix());
                            }
                        }
                    });

            return swipeView;
        }

        static SwipeFragment newInstance(int position) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;
            view.setScaleType(ImageView.ScaleType.MATRIX);

            float scale;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:   // first finger down only
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_UP: // first finger lifted
                    float resScale = getMatrixScale(matrix);
                    if ( Math.round( resScale )  <= 1 ) {
                        matrix = new Matrix(mMinScaleMatrix);
                    } else {
                        break;
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                    mode = NONE;
                    resScale = getMatrixScale(matrix);
                    if (resScale < 1.0) {
                        matrix = new Matrix(mMinScaleMatrix);
                    } else {
                        break;
                    }

                    break;

                case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                    oldDist = spacing(event);
                    Log.d(TAG, "oldDist=" + oldDist);
                    if (oldDist > 5f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                        Log.d(TAG, "mode=ZOOM");
                    }
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                    } else if (mode == ZOOM) {
                        // pinch zooming
                        float newDist = spacing(event);
                        Log.d(TAG, "newDist=" + newDist);
                        if (newDist > 5f) {
                            matrix.set(savedMatrix);
                            scale = newDist / oldDist; // setting the scaling of the
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix); // display the transformation on screen
            return true; // indicate event was handled
        }

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);
        }

        private void midPoint(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }

        float[] mTmpValues = new float[9];

        private float getMatrixScale(Matrix matrix) {
            matrix.getValues(mTmpValues);
            return mTmpValues[Matrix.MSCALE_X];
        }
    }
}