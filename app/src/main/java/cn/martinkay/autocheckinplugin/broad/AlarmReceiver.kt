package cn.martinkay.autocheckinplugin.broad;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import cn.martinkay.autocheckinplugin.SharePrefHelper;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import cn.martinkay.autocheckinplugin.MainActivity;
import cn.martinkay.autocheckinplugin.constant.Constant;
import cn.martinkay.autocheckinplugin.util.ShellUtils;
import cn.martinkay.autocheckinplugin.utils.AlarManagerUtil;
import cn.martinkay.autocheckinplugin.utils.HShizuku;
import kotlin.Pair;
import rikka.shizuku.Shizuku;

import static cn.martinkay.autocheckinplugin.SharePrefHelperKt.SIGN_OPEN_INTENT_START_TIME;

public class AlarmReceiver extends BroadcastReceiver {
    Random random = new Random();

    Integer[] autoCheckInWeek = {1, 2, 3, 4, 5, 6};

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ContentValues", "接收闹钟事件");
        try {
            // 初始化信息，例如ROOT权限
            initEnv(context);
            Bundle bundleExtra = intent.getBundleExtra("timer");
            int hour = bundleExtra.getInt("hour");
            int minute = bundleExtra.getInt("minute");
            int requestCode = bundleExtra.getInt("requestCode");
            wakeUpAndUnlock(context);
            // 先启动自己
            Intent intent2 = new Intent(context, MainActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent2);

            Log.i("ContentValues", "启动auto-sigin程序");

            // 再启动要打开的APP
            Intent intent3 = new Intent("android.intent.action.MAIN");
            intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ComponentName componentName = new ComponentName(Constant.getActiveApp().getPackageName(), Constant.getActiveApp().getActivityName());
            Log.i("ContentValues", "openAppByPackageName: " + Constant.getActiveApp().getPackageName() + "," + Constant.getActiveApp().getActivityName());
            intent3.setComponent(componentName);
            // 判断autoCheckInWeek是否包含今天的星期
            for (int i = 0; i < autoCheckInWeek.length; i++) {
                // 如果包含，就启动被打卡的APP
                if (Objects.equals(autoCheckInWeek[i], getWeek())) {
                    Log.i("ContentValues", "今天是要打卡的星期");
                    context.startActivity(intent3);
                    break;
                } else {
                    Log.i("ContentValues", "今天不是要打卡的星期");
                }
            }
            Log.i("ContentValues", "启动打卡程序");
            if (requestCode == 0) {
                Log.i("ContentValues", "重新注册上午上班打卡闹钟");
                AlarManagerUtil.timedTackMonWork(context, hour, minute, requestCode);
            } else if (requestCode == 1) {
                Log.i("ContentValues", "重新注册上午下班打卡闹钟");
                AlarManagerUtil.timedTackMonOffWork(context, hour, minute, requestCode);
            } else if (requestCode == 2) {
                Log.i("ContentValues", "重新注册下午上班打卡闹钟");
                AlarManagerUtil.timedTackAfWork(context, hour, minute, requestCode);
            } else if (requestCode == 3) {
                Log.i("ContentValues", "重新注册下午下班打卡闹钟");
                AlarManagerUtil.timedTackAfOffWork(context, hour, minute, requestCode);
            }
            Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(Constant.getActiveApp().getPackageName());
            if (launchIntentForPackage != null) {
                SharePrefHelper.INSTANCE.putLong(SIGN_OPEN_INTENT_START_TIME, System.currentTimeMillis());
                context.startActivity(launchIntentForPackage);
                Log.i("ContentValues", "启动打卡程序2");
            }
            Log.e("ContentValues", "定时成功！");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ContentValues", "定时失败！");
        }
    }

    public static void wakeUpAndUnlock(Context context) {
        if (Constant.isRoot) {
            if (Constant.isRoot) {
                try {
                    if (Shell.su("input keyevent 26").exec().isSuccess()) {
                        Log.i("AlarmReceiver", "ROOT shell亮屏成功");
                    }
                    if (Shell.su("input swipe 300 1000 300 500").exec().isSuccess()) {
                        Log.i("AlarmReceiver", "ROOT shell向上滑动解锁成功");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (Constant.isShizuku) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                boolean lockScreen = HShizuku.INSTANCE.getLockScreen();
                if (lockScreen) {
                    Log.i("AlarmReceiver", "Shizuku ibinder亮屏成功");
                } else {
                    Log.i("AlarmReceiver", "Shizuku ibinder亮屏失败");
                }
                Pair<Integer, String> execute = HShizuku.INSTANCE.execute("input swipe 300 1000 300 500", Constant.isRoot);
                if (execute.getFirst() == 0) {
                    Log.i("AlarmReceiver", "Shizuku ibinder向上滑动解锁成功");
                } else {
                    Log.i("AlarmReceiver", "Shizuku ibinder向上滑动解锁失败");
                }
            }
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "autocheckinplugin:WakeLockTag");
            wakeLock.acquire();
            ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("unLock").disableKeyguard();
        }
    }

    public static Integer getWeek() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        Integer[] weekDays = {7, 1, 2, 3, 4, 5, 6};
        return weekDays[dayOfWeek - 1];
    }

    public static void initEnv(Context context) {
        int isRoot = isRoot();
        if (isRoot == 0) {
            Constant.isRoot = true;
        } else {
            Constant.isRoot = false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            boolean isShizuku = HShizuku.INSTANCE.isEnable(context);
            if (isShizuku) {
                Constant.isShizuku = true;
            } else {
                Constant.isShizuku = false;
            }
        }
        if (Constant.isRoot) {
            Log.i("AlarmReceiver", "基于ROOT开启辅助功能");
            // 如果有ROOT并且没有开启辅助功能，就基于ROOT开启辅助功能
            if (Constant.isRoot && !AlarmReceiver.isAccessibility()) {
                AlarmReceiver.enableAccessibility();
            }
        } else {
            Log.i("AlarmReceiver", "基于Shizuku开启辅助功能");
            // 如果有Shizuku并且没有开启辅助功能，就基于Shizuku开启辅助功能
            if (Constant.isShizuku && !AlarmReceiver.isAccessibilityByShizuku(Constant.isRoot)) {
                AlarmReceiver.enableAccessibilityByShizuku(Constant.isRoot);
            }
        }
    }

    public static int isRoot() {
        try {
            List<String> cmds = new ArrayList<>();
            cmds.add("ls /data/data");
            ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);
            return result.result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isAccessibility() {
        List<String> cmds = new ArrayList<>();
        cmds.add("settings get secure enabled_accessibility_services");
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);
        // 注意：如果开启了多个辅助功能，这里的successMsg会有多个，所以不能用equals，而是用contains
        if ("cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService".contains(result.successMsg)) {
            List<String> cmds2 = new ArrayList<>();
            cmds2.add("settings get secure accessibility_enabled");
            ShellUtils.CommandResult result2 = ShellUtils.execCommand(cmds2, true, true);
            if (result2.result == 0 && "1".equals(result2.successMsg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAccessibilityByShizuku(boolean isRoot) {
        Pair<Integer, String> result = HShizuku.INSTANCE.execute("settings get secure enabled_accessibility_services", isRoot);
        // 注意：如果开启了多个辅助功能，这里的successMsg会有多个，所以不能用equals，而是用contains
        Log.i("AlarmReceiver", "isAccessibilityByShizuku: " + result.getFirst() + "--" + result.getSecond());
        if ("cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService".contains(result.getSecond())) {
            Pair<Integer, String> result2 = HShizuku.INSTANCE.execute("settings get secure accessibility_enabled", isRoot);
            if (result2.getFirst() == 0 && "1".equals(result2.getSecond())) {
                return true;
            }
        }
        return false;
    }

    public static int enableAccessibility() {
        List<String> cmds = new ArrayList<>();
        cmds.add("settings put secure enabled_accessibility_services cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService\n");
        cmds.add("settings put secure accessibility_enabled 1\n");
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmds, true, true);
        return result.result;
    }

    public static int enableAccessibilityByShizuku(boolean isRoot) {
        Pair<Integer, String> result = HShizuku.INSTANCE.execute("settings put secure enabled_accessibility_services cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService\n", true);
        Pair<Integer, String> result2 = HShizuku.INSTANCE.execute("settings put secure accessibility_enabled 1\n", true);
        Log.i("AlarmReceiver", "enableAccessibilityByShizuku: " + result.getFirst() + "--" + result.getSecond() + "--" + result2.getFirst() + "--" + result2.getSecond());
        return result2.getFirst();
    }
}