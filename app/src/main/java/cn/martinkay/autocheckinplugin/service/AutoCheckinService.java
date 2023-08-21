package cn.martinkay.autocheckinplugin.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AutoCheckinService extends AccessibilityService {

    public static final String TAG = "AutoCheckinService";
    public static final String PACKAGE_WECHAT_WORK = "com.tencent.wework";

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(timeReceiver, intentFilter);
        Log.e(TAG, " onCreate()");

    }

    private BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                checkToDoSignTask();
            }
        }
    };

    private void checkToDoSignTask() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }


}
