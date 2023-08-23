package cn.martinkay.autocheckinplugin.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import cn.martinkay.autocheckinplugin.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class JumpPermissionManagement {

    private static final String MANUFACTURER_HUAWEI = "Huawei";
    private static final String MANUFACTURER_XIAOMI = "Xiaomi";
    private static final String MANUFACTURER_LG = "LG";
    private static final String MANUFACTURER_LETV = "Letv";
    private static final String MANUFACTURER_OPPO = "OPPO";
    private static final String MANUFACTURER_SONY = "Sony";
    private static final String MANUFACTURER_MEIZU = "Meizu";
    private static final String MANUFACTURER_SMARTISAN = "Smartisan";


    private static final Map<String, ComponentName> ROM_COMPONENTS = new HashMap<>();

    static {
        ROM_COMPONENTS.put(MANUFACTURER_HUAWEI, new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
        ROM_COMPONENTS.put(MANUFACTURER_LG, new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity"));
        ROM_COMPONENTS.put(MANUFACTURER_LETV, new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps"));
        ROM_COMPONENTS.put(MANUFACTURER_OPPO, new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity"));
        ROM_COMPONENTS.put(MANUFACTURER_SONY, new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity"));
        ROM_COMPONENTS.put(MANUFACTURER_SMARTISAN, new ComponentName("com.smartisanos.security", "com.smartisanos.security.MainActivity"));
    }

    public static void GoToSetting(Activity activity) {
        String manufacturer = Build.MANUFACTURER;
        ComponentName componentName = ROM_COMPONENTS.get(manufacturer);

        if (componentName != null) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
            intent.setComponent(componentName);
            activity.startActivity(intent);
        } else if (MANUFACTURER_XIAOMI.equalsIgnoreCase(manufacturer)) {
            Xiaomi(activity);
        } else if (MANUFACTURER_MEIZU.equalsIgnoreCase(manufacturer)) {
            Meizu(activity);
        } else {
            ApplicationInfo(activity);
            Log.e("goToSetting", "目前暂不支持此系统");
        }
    }

    public static void ApplicationInfo(Activity activity) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction("android.intent.action.VIEW");
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        activity.startActivity(intent);
    }

    public static void Xiaomi(Activity activity) {
        Intent intent = new Intent();
        intent.setAction("miui.intent.action.APP_PERM_EDITOR");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra("extra_pkgname", activity.getPackageName());
        activity.startActivity(intent);
    }

    public static void Meizu(Activity activity) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        activity.startActivity(intent);
    }

    public static void SystemConfig(Activity activity) {
        activity.startActivity(new Intent("android.settings.SETTINGS"));
    }
}
