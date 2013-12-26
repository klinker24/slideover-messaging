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

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lklinker.android.slideovermessaging.slide_over.SlideOverService;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.security.PrivilegedExceptionAction;
import java.util.List;

public class MainActivity extends PreferenceActivity {

    public static Context context;
    public SharedPreferences sharedPrefs;

    public boolean showAll;

    private boolean enabled;
    private boolean haptic;
    private boolean close;
    private String secAction;
    private String side;
    private int sliver;
    private int vertical;
    private int activation;
    private int breakPoint;
    private int speed;
    private int padding;

    private List<ResolveInfo> mInstalledApps;
    private ApplicationsDialog.AppAdapter mAppAdapter;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.slideover_settings);
        setTitle(R.string.slide_over);

        context = this;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        getOriginal();

        showAll = sharedPrefs.getBoolean("show_all_settings", false);

        // Inflate a "Done/Discard" custom action bar view.
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_discard, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doneClick();
                        finish(); // TODO: don't just finish()!
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discardClick();
                        finish(); // TODO: don't just finish()!
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        final Preference finishFlat = findPreference("finish_flat_app");

        try {
            PackageManager manager = getPackageManager();
            ApplicationInfo info = manager.getApplicationInfo(sharedPrefs.getString("package_name", null), 0);
            finishFlat.setSummary(manager.getApplicationLabel(info));
        } catch (Exception e) {
            // dont set the subtitle for the option
        }

        finishFlat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                // set up intent used to search for all apps that show in the launcher
                final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mInstalledApps = context.getPackageManager().queryIntentActivities(mainIntent, 0);

                // creates applications dialog that lists all installed apps
                ApplicationsDialog appDialog = new ApplicationsDialog();
                mAppAdapter = appDialog.createAppAdapter(context, mInstalledApps);
                mAppAdapter.update();

                // Create listview with all launcher apps listed
                final ListView list = new ListView(context);
                list.setAdapter(mAppAdapter);

                // create new dialog box with applications list
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //builder.setTitle(R.string.choose_app);
                builder.setView(list);
                final Dialog dialog = builder.create();

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {

                        // if clicked item already on the list, do nothing and return
                        ApplicationsDialog.AppItem info = (ApplicationsDialog.AppItem) arg0.getItemAtPosition(arg2);
                        String packageName = info.packageName;

                        sharedPrefs.edit().putString("package_name", packageName).commit();
                        dialog.cancel();

                        finishFlat.setSummary(info.title);
                    }
                });

                dialog.show();
                return false;
            }

        });

        Preference advanced = findPreference("show_all_settings");
        advanced.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                recreate();
                return false;
            }

        });

        Preference googlePlus = findPreference("slideover_help");
        googlePlus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/117432358268488452276/posts/S1YMm5K69bQ")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        ColorPickerPreference haloColor = (ColorPickerPreference) findPreference("slideover_color");
        haloColor.setAlphaSliderEnabled(true);

        ColorPickerPreference haloUnreadColor = (ColorPickerPreference) findPreference("slideover_unread_color");
        haloUnreadColor.setAlphaSliderEnabled(true);

        if (!showAll) {
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("slideover_break_point"));
            ((PreferenceGroup) findPreference("slideover_general_category")).removePreference(findPreference("slideover_haptic_feedback"));
            ((PreferenceGroup) findPreference("slideover_general_category")).removePreference(findPreference("contact_picture_slideover"));
            ((PreferenceGroup) findPreference("slideover_quick_peek")).removePreference(findPreference("quick_peek_text_markers"));
            ((PreferenceGroup) findPreference("slideover_quick_peek")).removePreference(findPreference("quick_peek_transparency"));
            ((PreferenceGroup) findPreference("slideover_quick_peek")).removePreference(findPreference("close_quick_peek_on_send"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("slideover_disable_drag"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("slideover_disable_sliver_drag"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("slideover_new_sliver"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("ping_on_unlock"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("animate_text_on_ping"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("slideover_activation"));
            ((PreferenceGroup) findPreference("slideover_positioning_category")).removePreference(findPreference("slideover_return_timeout_length"));
            ((PreferenceGroup) findPreference("slideover_themeing")).removePreference(findPreference("slideover_animation_speed"));
            ((PreferenceGroup) findPreference("slideover_themeing")).removePreference(findPreference("slideover_unread_color"));
        }

        if (sharedPrefs.getBoolean("slideover_enabled", false)) {
            if (!isSlideOverRunning()) {
                Intent service = new Intent(getApplicationContext(), SlideOverService.class);
                startService(service);
            }
        }

        if (!sharedPrefs.getBoolean("knows_third_party", false)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("3rd Party Messaging Apps");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Some 3rd party messaging apps block the text message receivers that I use to display the messages to the SlideOver bubble. It is a very easy workaround though luckily, simply uninstall and then re-install your 3rd party messaging app (such as Go SMS) and the messages will come up in both your notification tray (through the 3rd party app) and the SlideOver bubble (through my app). \n" +
                            "\n" +
                            "A very easy fix!")
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.close),new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // don't write to the settings preference, they may want to see it again
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.never_again),new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // Don't show this to the user again
                            sharedPrefs.edit().putBoolean("knows_third_party", true).commit();
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }

        restartHaloPreference(findPreference("slideover_unread_color"),
                findPreference("slideover_haptic_feedback"),
                findPreference("slideover_break_point"),
                findPreference("slideover_color"),
                findPreference("slideover_activation"),
                findPreference("slideover_new_sliver"),
                findPreference("slideover_sliver"),
                findPreference("quick_peek_text_markers"),
                findPreference("quick_peek_transparency"),
                findPreference("quick_peek_transparency"),
                findPreference("slideover_disable_sliver_drag"),
                findPreference("slideover_disable_drag"),
                findPreference("slideover_only_unread"),
                findPreference("slideover_enabled"),
                findPreference("quick_peek_contact_num"),
                findPreference("enable_quick_peek"),
                findPreference("foreground_service"),
                findPreference("scaled_size")
        );
    }

    private boolean isSlideOverRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SlideOverService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean doneClick() {
        SlideOverService.restartHalo(context);
        finish();
        return true;
    }

    private boolean discardClick() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("slideover_enabled", enabled);
        editor.putString("slideover_side", side);
        editor.putInt("slideover_sliver", sliver);
        editor.putInt("slideover_vertical", vertical);
        editor.putInt("slideover_activation", activation);
        editor.putInt("slideover_break_point", breakPoint);
        editor.putBoolean("slideover_haptic_feedback", haptic);
        editor.putInt("slideover_animation_speed", speed);
        editor.putString("slideover_secondary_action", secAction);
        editor.putBoolean("full_app_popup_close", close);
        editor.putInt("slideover_padding", padding);
        editor.commit();

        SlideOverService.restartHalo(this);

        finish();
        return true;
    }

    private void getOriginal() {
        enabled = sharedPrefs.getBoolean("slideover_enabled", false);
        haptic = sharedPrefs.getBoolean("slideover_haptic_feedback", true);
        close = sharedPrefs.getBoolean("full_app_popup_close", true);
        secAction = sharedPrefs.getString("slideover_secondary_action", "conversations");
        side = sharedPrefs.getString("slideover_side", "left");
        sliver = sharedPrefs.getInt("slideover_sliver", 33);
        vertical = sharedPrefs.getInt("slideover_vertical", 50);
        activation = sharedPrefs.getInt("slideover_activation", 33);
        breakPoint = sharedPrefs.getInt("slideover_break_point", 33);
        speed = sharedPrefs.getInt("slideover_animation_speed", 33);
        padding = sharedPrefs.getInt("slideover_padding", 50);
    }

    private void restartHaloPreference(Preference... preferences) {
        for (Preference preference : preferences) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SlideOverService.restartHalo(context);
                    return true;
                }
            });
        }
    }
}