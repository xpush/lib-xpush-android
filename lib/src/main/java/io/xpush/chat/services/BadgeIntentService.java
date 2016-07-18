/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.xpush.chat.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.xpush.chat.R;
import io.xpush.chat.persist.ChannelTable;
import io.xpush.chat.persist.DBHelper;
import me.leolin.shortcutbadger.ShortcutBadger;

public class BadgeIntentService extends IntentService {

    private static final String TAG = BadgeIntentService.class.getSimpleName();

    public BadgeIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase mDatabase = mDbHelper.getReadableDatabase();

        Cursor mCount = mDatabase.rawQuery("select sum(" + ChannelTable.KEY_COUNT + ") from " + getApplicationContext().getString(R.string.channel_table_name), null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        mDatabase.close();
        mDbHelper.close();

        ShortcutBadger.with(getApplicationContext()).count(count);
    }
}