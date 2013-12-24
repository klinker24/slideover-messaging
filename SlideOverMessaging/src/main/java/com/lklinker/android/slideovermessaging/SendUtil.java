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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.klinker.android.send_message.Settings;

public class SendUtil {
    public static Settings getSendSettings(Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Settings sendSettings = new Settings();

        sendSettings.setMmsc(sharedPrefs.getString("mmsc_url", ""));
        sendSettings.setProxy(sharedPrefs.getString("mms_proxy", ""));
        sendSettings.setPort(sharedPrefs.getString("mms_port", ""));
        sendSettings.setGroup(sharedPrefs.getBoolean("group_message", false));
        sendSettings.setWifiMmsFix(sharedPrefs.getBoolean("wifi_mms_fix", false));
        sendSettings.setPreferVoice(sharedPrefs.getBoolean("voice_enabled", false));
        sendSettings.setDeliveryReports(sharedPrefs.getBoolean("delivery_reports", false));
        sendSettings.setSplit(sharedPrefs.getBoolean("split_sms", false));
        sendSettings.setSplitCounter(sharedPrefs.getBoolean("split_counter", false));
        sendSettings.setStripUnicode(sharedPrefs.getBoolean("strip_unicode", false));
        sendSettings.setSignature(sharedPrefs.getString("signature", ""));
        sendSettings.setPreText(sharedPrefs.getBoolean("giffgaff_delivery", false) ? "*0#" : "");
        sendSettings.setSendLongAsMms(sharedPrefs.getBoolean("send_as_mms", false));
        sendSettings.setSendLongAsMmsAfter(sharedPrefs.getInt("mms_after", 4));
        sendSettings.setAccount(sharedPrefs.getString("voice_account", null));
        sendSettings.setRnrSe(sharedPrefs.getString("voice_rnrse", null));

        return sendSettings;
    }

}
