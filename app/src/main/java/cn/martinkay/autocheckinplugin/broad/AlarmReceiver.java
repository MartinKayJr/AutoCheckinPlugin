package cn.martinkay.autocheckinplugin.broad;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.util.Random;

import cn.martinkay.autocheckinplugin.MainActivity;
import cn.martinkay.autocheckinplugin.constant.Constant;
import cn.martinkay.autocheckinplugin.util.AndroidRootUtils;
import cn.martinkay.autocheckinplugin.utils.AlarManagerUtil;

public class AlarmReceiver extends BroadcastReceiver {
    Random random = new Random();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ContentValues", "接收闹钟事件");
        try {
            Bundle bundleExtra = intent.getBundleExtra("timer");
            int hour = bundleExtra.getInt("hour");
            int minute = bundleExtra.getInt("minute");
            int requestCode = bundleExtra.getInt("requestCode");
            wakeUpAndUnlock(context);
            Intent intent2 = new Intent(context, MainActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent2);
            Log.i("ContentValues", "启动auto-sigin程序");
            Intent intent3 = new Intent("android.intent.action.MAIN");
            intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ComponentName componentName = new ComponentName(Constant.getActiveApp().getPackageName(), Constant.getActiveApp().getActivityName());
            Log.i("ContentValues", "openAppByPackageName: " + Constant.getActiveApp().getPackageName() + "," + Constant.getActiveApp().getActivityName());
            intent3.setComponent(componentName);
            context.startActivity(intent3);
            Log.i("ContentValues", "启动打卡程序");
            if (requestCode == 0) {
                Log.i("ContentValues", "重新注册上午上班打卡闹钟");
                AlarManagerUtil.timedTackMonWork(context, hour, minute, requestCode);
            } else if (requestCode == 1) {
                Log.i("ContentValues", "重新注册上午下班打卡闹钟");
                AlarManagerUtil.timedTackAfWork(context, hour, minute, requestCode);
            } else if (requestCode == 2) {
                Log.i("ContentValues", "重新注册下午上班打卡闹钟");
                AlarManagerUtil.timedTackMonOffWork(context, hour, minute, requestCode);
            } else if (requestCode == 3) {
                Log.i("ContentValues", "重新注册下午下班打卡闹钟");
                AlarManagerUtil.timedTackAfOffWork(context, hour, minute, requestCode);
            }
            Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(Constant.getActiveApp().getPackageName());
            if (launchIntentForPackage != null) {
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
                        Log.i("MyAccessibilityService", "息屏成功");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "autocheckinplugin:WakeLockTag");
            wakeLock.acquire();
            ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("unLock").disableKeyguard();
        }
    }
}