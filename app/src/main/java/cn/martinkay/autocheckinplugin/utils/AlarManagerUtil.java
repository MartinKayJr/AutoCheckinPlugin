package cn.martinkay.autocheckinplugin.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;

/* loaded from: classes.dex */
public class AlarManagerUtil {
    private static Context activityA;
    public static AlarmManager alarmManager;
    private static PendingIntent pendingIntentAfWork;
    private static PendingIntent pendingIntentAfOffWork;

    private static PendingIntent pendingIntentMonWork;
    private static PendingIntent pendingIntentMonOffWork;

    public static void timedTackMonWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent("wework");
        Bundle bundle = new Bundle();
        bundle.putInt("hour", hour);
        bundle.putInt("minute", minute);
        bundle.putInt("requestCode", requestCode);
        intent.putExtra("timer", bundle);
        intent.setPackage(activity.getPackageName());
        PendingIntent broadcast;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        pendingIntentMonWork = broadcast;
        alarmManager.cancel(broadcast);
        long timeInMillis = calendar.getTimeInMillis();
        if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
            timeInMillis += 86400000;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonWork);
                    return;
                }
            }
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonWork);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonWork);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonWork);
        }
    }

    public static void timedTackMonOffWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent("wework");
        Bundle bundle = new Bundle();
        bundle.putInt("hour", hour);
        bundle.putInt("minute", minute);
        bundle.putInt("requestCode", requestCode);
        intent.putExtra("timer", bundle);
        intent.setPackage(activity.getPackageName());
        PendingIntent broadcast;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        pendingIntentMonOffWork = broadcast;
        alarmManager.cancel(broadcast);
        long timeInMillis = calendar.getTimeInMillis();
        if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
            timeInMillis += 86400000;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonOffWork);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonOffWork);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentMonOffWork);
        }
    }

    public static void timedTackAfWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent("wework");
        Bundle bundle = new Bundle();
        bundle.putInt("hour", hour);
        bundle.putInt("minute", minute);
        bundle.putInt("requestCode", requestCode);
        intent.putExtra("timer", bundle);
        intent.setPackage(activity.getPackageName());
        PendingIntent broadcast;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        pendingIntentAfWork = broadcast;
        alarmManager.cancel(broadcast);
        long timeInMillis = calendar.getTimeInMillis();
        if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
            timeInMillis += 86400000;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentAfWork);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentAfWork);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentAfWork);
        }
    }

    public static void timedTackAfOffWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent("wework");
        Bundle bundle = new Bundle();
        bundle.putInt("hour", hour);
        bundle.putInt("minute", minute);
        bundle.putInt("requestCode", requestCode);
        intent.putExtra("timer", bundle);
        intent.setPackage(activity.getPackageName());
        PendingIntent broadcast;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            broadcast = PendingIntent.getBroadcast(activity, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        pendingIntentAfOffWork = broadcast;
        alarmManager.cancel(broadcast);
        long timeInMillis = calendar.getTimeInMillis();
        if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0) {
            timeInMillis += 86400000;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentAfOffWork);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentAfOffWork);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntentAfOffWork);
        }
    }

    public static void cancelTimetacker(Context context, boolean showMsg) {
        try {
            activityA = context;
            alarmManager.cancel(pendingIntentMonWork);
            alarmManager.cancel(pendingIntentMonOffWork);
            alarmManager.cancel(pendingIntentAfWork);
            alarmManager.cancel(pendingIntentAfOffWork);
            if (showMsg) {
                Toast.makeText(activityA, "自动签到已关闭", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activityA, "取消失败！", Toast.LENGTH_SHORT).show();
        }
    }
}