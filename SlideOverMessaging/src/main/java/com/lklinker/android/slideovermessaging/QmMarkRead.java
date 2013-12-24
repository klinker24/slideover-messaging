/*
 * Copyright 2013 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lklinker.android.slideovermessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class QmMarkRead extends IntentService {

    public QmMarkRead() {
        super("service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("mark_read", "marking all messages as read...");

        readSMS(this);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        stopSelf();
    }

    public void readSMS(Context context) {
        try {
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[]{"_id", "thread_id", "address",
                            "person", "date", "body", "read"}, null, null, "date DESC LIMIT 10");

            if (c != null && c.moveToFirst()) {
                do {
                    String id = c.getString(0);

                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + id, null);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {

        }
    }
}
