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

import android.annotation.SuppressLint;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

import java.util.Calendar;

@SuppressWarnings("deprecation")
public class TextMessageReceiver extends BroadcastReceiver {

    public static final String SMS_EXTRA_NAME = "pdus";
    public SharedPreferences sharedPrefs;

    private boolean alert = true;

    @SuppressLint("Wakelock")
    public void onReceive(final Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_enabled", true)) {

            String body = "";
            String address = "";
            String name = "";
            String dateReceived = "";
            String date = "";

            if (extras != null) {
                Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

                for (int i = 0; i < smsExtra.length; ++i) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

                    body += sms.getMessageBody().toString();
                    address = sms.getOriginatingAddress();
                    date = sms.getTimestampMillis() + "";
                }
            } else {
                return;
            }

            name = ContactUtil.findContactName(address, context);
            Calendar cal = Calendar.getInstance();
            dateReceived = cal.getTimeInMillis() + "";

            if (sharedPrefs.getBoolean("vibrate_on_incoming", false)) {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(250);
            }

            if (sharedPrefs.getBoolean("override", false)) {
                ContentValues values = new ContentValues();
                values.put("address", address);
                values.put("body", body);
                values.put("date", dateReceived);
                values.put("read", "0");
                values.put("date_sent", date);

                context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);

            }

            Intent updateHalo = new Intent("com.klinker.android.messaging.UPDATE_SLIDEOVER");
            updateHalo.putExtra("name", name);
            updateHalo.putExtra("message", body);
            updateHalo.putExtra("number", address);
            context.sendBroadcast(updateHalo);

            if (sharedPrefs.getBoolean("override", false) && sharedPrefs.getBoolean("slideover_enabled", true)) {
                try {
                    this.abortBroadcast();
                } catch (Exception e) {
                }

            }
        }
    }
}
