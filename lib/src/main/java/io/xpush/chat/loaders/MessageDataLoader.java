package io.xpush.chat.loaders;

import java.util.List;

import android.content.Context;

import io.xpush.chat.models.XPushMessage;
import io.xpush.chat.persist.DataSource;

public class MessageDataLoader extends AbstractDataLoader<List<XPushMessage>> {
    private DataSource<XPushMessage> mDataSource;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mGroupBy;
    private String mHaving;
    private String mSortOrder;

    public void setSelection( String selection ){
        this.mSelection = selection;
    }

    public void setSortOrder( String sortOrder ){
        this.mSortOrder = sortOrder;
    }

    public MessageDataLoader(Context context, DataSource dataSource, String selection, String[] selectionArgs,
                                String groupBy, String having, String sortOrder) {
        super(context);
        mDataSource = dataSource;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mGroupBy = groupBy;
        mHaving = having;
        mSortOrder = sortOrder;
    }

    @Override
    protected List<XPushMessage> buildList() {
        List<XPushMessage> testList = mDataSource.read(mSelection, mSelectionArgs, mGroupBy, mHaving,
                mSortOrder);
        return testList;
    }

    public void insert(XPushMessage entity) {
        new InsertTask(this).execute(entity);
    }

    public void update(XPushMessage entity) {
        new UpdateTask(this).execute(entity);
    }

    public void delete(XPushMessage entity) {
        new DeleteTask(this).execute(entity);
    }

    private class InsertTask extends ContentChangingTask<XPushMessage, Void, Void> {
        InsertTask(MessageDataLoader loader) {
            super(loader);
        }

        @Override
        protected Void doInBackground(XPushMessage... params) {
            mDataSource.insert(params[0]);
            return (null);
        }
    }

    private class UpdateTask extends ContentChangingTask<XPushMessage, Void, Void> {
        UpdateTask(MessageDataLoader loader) {
            super(loader);
        }

        @Override
        protected Void doInBackground(XPushMessage... params) {
            mDataSource.update(params[0]);
            return (null);
        }
    }

    private class DeleteTask extends ContentChangingTask<XPushMessage, Void, Void> {
        DeleteTask(MessageDataLoader loader) {
            super(loader);
        }

        @Override
        protected Void doInBackground(XPushMessage... params) {
            mDataSource.delete(params[0]);
            return (null);
        }
    }
}
