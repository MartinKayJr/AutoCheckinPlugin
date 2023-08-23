package cn.martinkay.autocheckinplugin.service;


import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;
import android.util.Log;

/* loaded from: classes.dex */
public class BackgroundAccess {
    public static boolean canBackgroundStart(Context context) {
        try {
            return ((AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE)).checkOp("android:system_alert_window", Process.myUid(), context.getPackageName()) == 0;
        } catch (Exception e) {
            Log.e("mes", "not support", e);
            return false;
        }
    }
}