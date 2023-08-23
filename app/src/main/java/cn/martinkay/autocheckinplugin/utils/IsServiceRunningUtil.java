package cn.martinkay.autocheckinplugin.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class IsServiceRunningUtil {
    public static boolean isAccessibilitySettingsOn(Context context, String accessibilityServiceName) {
        int i;
        String str = context.getPackageName() + "/" + accessibilityServiceName;
        try {
            i = Settings.Secure.getInt(context.getContentResolver(), "accessibility_enabled", 0);
        } catch (Exception e) {
            Log.e("ContentValues", "get accessibility enable failed, the err:" + e.getMessage());
            i = 0;
        }
        if (i == 1) {
            TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
            String string = Settings.Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
            if (string != null) {
                simpleStringSplitter.setString(string);
                while (simpleStringSplitter.hasNext()) {
                    if (simpleStringSplitter.next().equalsIgnoreCase(str)) {
                        Log.v("ContentValues", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d("ContentValues", "Accessibility service disable");
        }
        return false;
    }
}