/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.deskclock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * DeskClock clock view for desk docks.
 */
public class DeskClock extends Activity {
    private static final boolean DEBUG = false;

    private static final String LOG_TAG = "DeskClock";

    // Package ID of the music player.
    private static final String MUSIC_PACKAGE_ID = "com.android.music";

    // Alarm action for midnight (so we can update the date display).
    private static final String ACTION_MIDNIGHT = "com.android.deskclock.MIDNIGHT";

    // Interval between polls of the weather widget. Its refresh period is
    // likely to be much longer (~3h), but we want to pick up any changes
    // within 5 minutes.
    private final long FETCH_WEATHER_DELAY = 5 * 60 * 1000; // 5 min

    // Delay before engaging the burn-in protection mode (green-on-black).
    private final long SCREEN_SAVER_TIMEOUT = 10 * 60 * 1000; // 10 min

    // Repositioning delay in screen saver.
    private final long SCREEN_SAVER_MOVE_DELAY = 60 * 1000; // 1 min

    // Color to use for text & graphics in screen saver mode.
    private final int SCREEN_SAVER_COLOR = 0xFF308030;
    private final int SCREEN_SAVER_COLOR_DIM = 0xFF183018;

    // Internal message IDs.
    private final int FETCH_WEATHER_DATA_MSG     = 0x1000;
    private final int UPDATE_WEATHER_DISPLAY_MSG = 0x1001;
    private final int SCREEN_SAVER_TIMEOUT_MSG   = 0x2000;
    private final int SCREEN_SAVER_MOVE_MSG      = 0x2001;

    // Weather widget query information.
    private static final String GENIE_PACKAGE_ID = "com.google.android.apps.genie.geniewidget";
    private static final String WEATHER_CONTENT_AUTHORITY = GENIE_PACKAGE_ID + ".weather";
    private static final String WEATHER_CONTENT_PATH = "/weather/current";
    private static final String[] WEATHER_CONTENT_COLUMNS = new String[] {
            "location",
            "timestamp",
            "temperature",
            "highTemperature",
            "lowTemperature",
            "iconUrl",
            "iconResId",
            "description",
        };

    // State variables follow.
    private DigitalClock mTime;
    private TextView mDate;

    private TextView mNextAlarm = null;
    private TextView mBatteryDisplay;

    private TextView mWeatherCurrentTemperature;
    private TextView mWeatherHighTemperature;
    private TextView mWeatherLowTemperature;
    private TextView mWeatherLocation;
    private ImageView mWeatherIcon;

    private String mWeatherCurrentTemperatureString;
    private String mWeatherHighTemperatureString;
    private String mWeatherLowTemperatureString;
    private String mWeatherLocationString;
    private Drawable mWeatherIconDrawable;

    private Resources mGenieResources = null;

    private boolean mDimmed = false;
    private boolean mScreenSaverMode = false;

    private DateFormat mDateFormat;

    private int mBatteryLevel = -1;
    private boolean mPluggedIn = false;

    private boolean mInDock = false;

    private int mIdleTimeoutEpoch = 0;

    private boolean mWeatherFetchScheduled = false;

    private Random mRNG;

    private PendingIntent mMidnightIntent;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_DATE_CHANGED.equals(action)) {
                refreshDate();
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                handleBatteryUpdate(
                    intent.getIntExtra("status", BATTERY_STATUS_UNKNOWN),
                    intent.getIntExtra("level", 0));
            }
        }
    };

    private final Handler mHandy = new Handler() {
        @Override
        public void handleMessage(Message m) {
            if (m.what == FETCH_WEATHER_DATA_MSG) {
                if (!mWeatherFetchScheduled) return;
                mWeatherFetchScheduled = false;
                new Thread() { public void run() { fetchWeatherData(); } }.start();
                scheduleWeatherFetchDelayed(FETCH_WEATHER_DELAY);
            } else if (m.what == UPDATE_WEATHER_DISPLAY_MSG) {
                updateWeatherDisplay();
            } else if (m.what == SCREEN_SAVER_TIMEOUT_MSG) {
                if (m.arg1 == mIdleTimeoutEpoch) {
                    saveScreen();
                }
            } else if (m.what == SCREEN_SAVER_MOVE_MSG) {
                moveScreenSaver();
            }
        }
    };


    private void moveScreenSaver() {
        moveScreenSaverTo(-1,-1);
    }
    private void moveScreenSaverTo(int x, int y) {
        if (!mScreenSaverMode) return;

        final View saver_view = findViewById(R.id.saver_view);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (x < 0 || y < 0) {
            int myWidth = saver_view.getMeasuredWidth();
            int myHeight = saver_view.getMeasuredHeight();
            x = (int)(mRNG.nextFloat()*(metrics.widthPixels - myWidth));
            y = (int)(mRNG.nextFloat()*(metrics.heightPixels - myHeight));
        }

        if (DEBUG) Log.d(LOG_TAG, String.format("screen saver: %d: jumping to (%d,%d)",
                System.currentTimeMillis(), x, y));

        saver_view.setLayoutParams(new AbsoluteLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            x,
            y));

        // Synchronize our jumping so that it happens exactly on the second.
        mHandy.sendEmptyMessageDelayed(SCREEN_SAVER_MOVE_MSG,
            SCREEN_SAVER_MOVE_DELAY +
            (1000 - (System.currentTimeMillis() % 1000)));
    }

    private void setWakeLock(boolean hold) {
        if (DEBUG) Log.d(LOG_TAG, (hold ? "hold" : " releas") + "ing wake lock");
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        winParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        winParams.flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        if (hold)
            winParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        else
            winParams.flags &= (~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        win.setAttributes(winParams);
    }

    private void restoreScreen() {
        if (!mScreenSaverMode) return;
        if (DEBUG) Log.d(LOG_TAG, "restoreScreen");
        mScreenSaverMode = false;
        initViews();
        doDim(false); // restores previous dim mode
        refreshAll();
    }

    // Special screen-saver mode for OLED displays that burn in quickly
    private void saveScreen() {
        if (mScreenSaverMode) return;
        if (DEBUG) Log.d(LOG_TAG, "saveScreen");

        // quickly stash away the x/y of the current date
        final View oldTimeDate = findViewById(R.id.time_date);
        int oldLoc[] = new int[2];
        oldTimeDate.getLocationOnScreen(oldLoc);

        mScreenSaverMode = true;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);

        // give up any internal focus before we switch layouts
        final View focused = getCurrentFocus();
        if (focused != null) focused.clearFocus();

        setContentView(R.layout.desk_clock_saver);

        mTime = (DigitalClock) findViewById(R.id.time);
        mDate = (TextView) findViewById(R.id.date);
        mNextAlarm = (TextView) findViewById(R.id.nextAlarm);

        final int color = mDimmed ? SCREEN_SAVER_COLOR_DIM : SCREEN_SAVER_COLOR;

        ((TextView)findViewById(R.id.timeDisplay)).setTextColor(color);
        ((TextView)findViewById(R.id.am_pm)).setTextColor(color);
        mDate.setTextColor(color);
        mNextAlarm.setTextColor(color);
        mNextAlarm.setCompoundDrawablesWithIntrinsicBounds(
            getResources().getDrawable(mDimmed 
                ? R.drawable.ic_lock_idle_alarm_saver_dim 
                : R.drawable.ic_lock_idle_alarm_saver),
            null, null, null);

        mBatteryDisplay =
        mWeatherCurrentTemperature =
        mWeatherHighTemperature =
        mWeatherLowTemperature =
        mWeatherLocation = null;
        mWeatherIcon = null;

        refreshDate();
        refreshAlarm();

        moveScreenSaverTo(oldLoc[0], oldLoc[1]);
    }

    @Override
    public void onUserInteraction() {
        if (mScreenSaverMode)
            restoreScreen();
    }

    private boolean supportsWeather() {
        return (mGenieResources != null);
    }

    private void scheduleWeatherFetchDelayed(long delay) {
        if (mWeatherFetchScheduled) return;

        if (DEBUG) Log.d(LOG_TAG, "scheduling weather fetch message for " + delay + "ms from now");

        mWeatherFetchScheduled = true;

        mHandy.sendEmptyMessageDelayed(FETCH_WEATHER_DATA_MSG, delay);
    }

    private void unscheduleWeatherFetch() {
        mWeatherFetchScheduled = false;
    }

    private static final boolean sCelsius;
    static {
        String cc = Locale.getDefault().getCountry().toLowerCase();
        sCelsius = !("us".equals(cc) || "bz".equals(cc) || "jm".equals(cc));
    }

    private static int celsiusToLocal(int tempC) {
        return sCelsius ? tempC : (int) Math.round(tempC * 1.8f + 32);
    }

    private void fetchWeatherData() {
        // if we couldn't load the weather widget's resources, we simply
        // assume it's not present on the device.
        if (mGenieResources == null) return;

        Uri queryUri = new Uri.Builder()
            .scheme(android.content.ContentResolver.SCHEME_CONTENT)
            .authority(WEATHER_CONTENT_AUTHORITY)
            .path(WEATHER_CONTENT_PATH)
            .appendPath(new Long(System.currentTimeMillis()).toString())
            .build();

        if (DEBUG) Log.d(LOG_TAG, "querying genie: " + queryUri);

        Cursor cur;
        try {
            cur = managedQuery(
                queryUri,
                WEATHER_CONTENT_COLUMNS,
                null,
                null,
                null);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "Weather query failed", e);
            cur = null;
        }

        if (cur != null && cur.moveToFirst()) {
            if (DEBUG) {
                java.lang.StringBuilder sb =
                    new java.lang.StringBuilder("Weather query result: {");
                for(int i=0; i<cur.getColumnCount(); i++) {
                    if (i>0) sb.append(", ");
                    sb.append(cur.getColumnName(i))
                        .append("=")
                        .append(cur.getString(i));
                }
                sb.append("}");
                Log.d(LOG_TAG, sb.toString());
            }

            mWeatherIconDrawable = mGenieResources.getDrawable(cur.getInt(
                cur.getColumnIndexOrThrow("iconResId")));
            mWeatherCurrentTemperatureString = String.format("%d\u00b0",
                celsiusToLocal(cur.getInt(cur.getColumnIndexOrThrow("temperature"))));
            mWeatherHighTemperatureString = String.format("%d\u00b0",
                celsiusToLocal(cur.getInt(cur.getColumnIndexOrThrow("highTemperature"))));
            mWeatherLowTemperatureString = String.format("%d\u00b0",
                celsiusToLocal(cur.getInt(cur.getColumnIndexOrThrow("lowTemperature"))));
            mWeatherLocationString = cur.getString(
                cur.getColumnIndexOrThrow("location"));
        } else {
            Log.w(LOG_TAG, "No weather information available (cur="
                + cur +")");
            mWeatherIconDrawable = null;
            mWeatherHighTemperatureString = "";
            mWeatherLowTemperatureString = "";
            mWeatherLocationString = "Weather data unavailable."; // TODO: internationalize
        }

        mHandy.sendEmptyMessage(UPDATE_WEATHER_DISPLAY_MSG);
    }

    private void refreshWeather() {
        if (supportsWeather())
            scheduleWeatherFetchDelayed(0);
        updateWeatherDisplay(); // in case we have it cached
    }

    private void updateWeatherDisplay() {
        if (mWeatherCurrentTemperature == null) return;

        mWeatherCurrentTemperature.setText(mWeatherCurrentTemperatureString);
        mWeatherHighTemperature.setText(mWeatherHighTemperatureString);
        mWeatherLowTemperature.setText(mWeatherLowTemperatureString);
        mWeatherLocation.setText(mWeatherLocationString);
        mWeatherIcon.setImageDrawable(mWeatherIconDrawable);
    }

    // Adapted from KeyguardUpdateMonitor.java
    private void handleBatteryUpdate(int plugStatus, int batteryLevel) {
        final boolean pluggedIn = (plugStatus == BATTERY_STATUS_CHARGING || plugStatus == BATTERY_STATUS_FULL);
        if (pluggedIn != mPluggedIn) {
            setWakeLock(pluggedIn);
        }
        if (pluggedIn != mPluggedIn || batteryLevel != mBatteryLevel) {
            mBatteryLevel = batteryLevel;
            mPluggedIn = pluggedIn;
            refreshBattery();
        }
    }

    private void refreshBattery() {
        if (mBatteryDisplay == null) return;

        if (mPluggedIn /* || mBatteryLevel < LOW_BATTERY_THRESHOLD */) {
            mBatteryDisplay.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, android.R.drawable.ic_lock_idle_charging, 0);
            mBatteryDisplay.setText(
                getString(R.string.battery_charging_level, mBatteryLevel));
            mBatteryDisplay.setVisibility(View.VISIBLE);
        } else {
            mBatteryDisplay.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshDate() {
        final Date now = new Date();
        if (DEBUG) Log.d(LOG_TAG, "refreshing date..." + now);
        mDate.setText(mDateFormat.format(now));
    }

    private void refreshAlarm() {
        if (mNextAlarm == null) return;

        String nextAlarm = Settings.System.getString(getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if (!TextUtils.isEmpty(nextAlarm)) {
            mNextAlarm.setText(nextAlarm);
            //mNextAlarm.setCompoundDrawablesWithIntrinsicBounds(
            //    android.R.drawable.ic_lock_idle_alarm, 0, 0, 0);
            mNextAlarm.setVisibility(View.VISIBLE);
        } else {
            mNextAlarm.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshAll() {
        refreshDate();
        refreshAlarm();
        refreshBattery();
        refreshWeather();
    }

    private void doDim(boolean fade) {
        View tintView = findViewById(R.id.window_tint);
        if (tintView == null) return;

        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();

        winParams.flags |= (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        winParams.flags |= (WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // dim the wallpaper somewhat (how much is determined below)
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (mDimmed) {
            winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            winParams.dimAmount = 0.67f; // pump up contrast in dim mode

            // show the window tint
            tintView.startAnimation(AnimationUtils.loadAnimation(this,
                fade ? R.anim.dim
                     : R.anim.dim_instant));
        } else {
            winParams.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            winParams.dimAmount = 0.5f; // lower contrast in normal mode

            // hide the window tint
            tintView.startAnimation(AnimationUtils.loadAnimation(this,
                fade ? R.anim.undim
                     : R.anim.undim_instant));
        }

        win.setAttributes(winParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(LOG_TAG, "onResume");

        // reload the date format in case the user has changed settings
        // recently
        final SimpleDateFormat dateFormat = (SimpleDateFormat)
                java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL);
        // This is a little clumsy; we want to honor the locale's date format
        // (rather than simply hardcoding "Weekday, Month Date") but
        // DateFormat.FULL includes the year (at least, in enUS). So we lop
        // that bit off if it's there; should have no effect on
        // locale-specific date strings that look different.
        mDateFormat = new SimpleDateFormat(dateFormat.toPattern()
                    .replace(", yyyy", "")); // no year

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(ACTION_MIDNIGHT);

        Calendar today = Calendar.getInstance();
        today.add(Calendar.DATE, 1);
        mMidnightIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_MIDNIGHT), 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, today.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mMidnightIntent);
        registerReceiver(mIntentReceiver, filter);

        doDim(false);
        restoreScreen();
        refreshAll(); // will schedule periodic weather fetch

        setWakeLock(mPluggedIn);

        mIdleTimeoutEpoch++;
        mHandy.sendMessageDelayed(
            Message.obtain(mHandy, SCREEN_SAVER_TIMEOUT_MSG, mIdleTimeoutEpoch, 0),
            SCREEN_SAVER_TIMEOUT);

        final boolean launchedFromDock 
            = getIntent().hasCategory(Intent.CATEGORY_DESK_DOCK);

        if (supportsWeather() && launchedFromDock && !mInDock) {
            if (DEBUG) Log.d(LOG_TAG, "Device now docked; forcing weather to refresh right now");
            sendBroadcast(
                new Intent("com.google.android.apps.genie.REFRESH")
                    .putExtra("requestWeather", true));
        }

        mInDock = launchedFromDock;
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.d(LOG_TAG, "onPause");

        // Turn off the screen saver. (But don't un-dim.)
        restoreScreen();

        // Other things we don't want to be doing in the background.
        unregisterReceiver(mIntentReceiver);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(mMidnightIntent);
        unscheduleWeatherFetch();

        super.onPause();
    }

    @Override
    public void onStop() {
        if (DEBUG) Log.d(LOG_TAG, "onStop");

        // Avoid situations where the user launches Alarm Clock and is
        // surprised to find it in dim mode (because it was last used in dim
        // mode, but that last use is long in the past).
        mDimmed = false;

        super.onStop();
    }

    private void initViews() {
        // give up any internal focus before we switch layouts
        final View focused = getCurrentFocus();
        if (focused != null) focused.clearFocus();

        setContentView(R.layout.desk_clock);

        mTime = (DigitalClock) findViewById(R.id.time);
        mDate = (TextView) findViewById(R.id.date);
        mBatteryDisplay = (TextView) findViewById(R.id.battery);

        mTime.getRootView().requestFocus();

        mWeatherCurrentTemperature = (TextView) findViewById(R.id.weather_temperature);
        mWeatherHighTemperature = (TextView) findViewById(R.id.weather_high_temperature);
        mWeatherLowTemperature = (TextView) findViewById(R.id.weather_low_temperature);
        mWeatherLocation = (TextView) findViewById(R.id.weather_location);
        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);

        mNextAlarm = (TextView) findViewById(R.id.nextAlarm);

        final ImageButton alarmButton = (ImageButton) findViewById(R.id.alarm_button);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(DeskClock.this, AlarmClock.class));
            }
        });

        final ImageButton galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            .putExtra("slideshow", true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (android.content.ActivityNotFoundException e) {
                    Log.e(LOG_TAG, "Couldn't launch image browser", e);
                }
            }
        });

        final ImageButton musicButton = (ImageButton) findViewById(R.id.music_button);
        musicButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent musicAppQuery = getPackageManager()
                        .getLaunchIntentForPackage(MUSIC_PACKAGE_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (musicAppQuery != null) {
                        startActivity(musicAppQuery);
                    }
                } catch (android.content.ActivityNotFoundException e) {
                    Log.e(LOG_TAG, "Couldn't launch music browser", e);
                }
            }
        });

        final ImageButton homeButton = (ImageButton) findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(
                    new Intent(Intent.ACTION_MAIN)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addCategory(Intent.CATEGORY_HOME));
            }
        });

        final ImageButton nightmodeButton = (ImageButton) findViewById(R.id.nightmode_button);
        nightmodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDimmed = ! mDimmed;
                doDim(true);
            }
        });

        nightmodeButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                saveScreen();
                return true;
            }
        });

        final View weatherView = findViewById(R.id.weather);
        weatherView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!supportsWeather()) return;

                Intent genieAppQuery = getPackageManager()
                    .getLaunchIntentForPackage(GENIE_PACKAGE_ID)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (genieAppQuery != null) {
                    startActivity(genieAppQuery);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!mScreenSaverMode) {
            initViews();
            doDim(false);
            refreshAll();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_alarms) {
            startActivity(new Intent(DeskClock.this, AlarmClock.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.desk_clock_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mRNG = new Random();

        try {
            mGenieResources = getPackageManager().getResourcesForApplication(GENIE_PACKAGE_ID);
        } catch (PackageManager.NameNotFoundException e) {
            // no weather info available
            Log.w(LOG_TAG, "Can't find "+GENIE_PACKAGE_ID+". Weather forecast will not be available.");
        }

        initViews();
    }

}