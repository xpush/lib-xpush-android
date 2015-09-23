package io.xpush.chat.view.listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = RecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    public enum RecylclerDirection{
        UP, DOWN
    }

    private int current_page = 1;
    private RecylclerDirection mDirection;

    private LinearLayoutManager mLinearLayoutManager;

    public RecyclerOnScrollListener(LinearLayoutManager linearLayoutManager, RecylclerDirection direction) {
        this.mDirection = direction;
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if( RecylclerDirection.UP == mDirection ) {
            // if scroll to bottom
            if (dy > 0) {
                return;
            }

        } else {
            // if scroll to up
            if (dy < 0) {
                return;
            }
        }

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if( RecylclerDirection.UP == mDirection ) {

            // Thread Hold reached
            if (!loading && firstVisibleItem <= visibleThreshold) {
                // Do something
                current_page++;
                onLoadMore(current_page);
                loading = true;
            }

        } else {

            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {

                // End has been reached
                // Do something
                current_page++;
                onLoadMore(current_page);
                loading = true;
            }
        }
    }

    public abstract void onLoadMore(int current_page);

    public void reset(int previousTotal, boolean loading) {
        this.previousTotal = previousTotal;
        this.loading = loading;
    }
}

