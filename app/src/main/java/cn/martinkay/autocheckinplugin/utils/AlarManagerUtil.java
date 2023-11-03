package cn.martinkay.autocheckinplugin.utils;

import static cn.martinkay.autocheckinplugin.SharePrefHelperKt.IS_ENABLE_TIME_JITTER;
import static cn.martinkay.autocheckinplugin.SharePrefHelperKt.TIME_JITTER_VALUE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

import cn.martinkay.autocheckinplugin.SharePrefHelper;
import cn.martinkay.autocheckinplugin.broad.AlarmReceiver;

public class AlarManagerUtil {
    private static Context activityA;
    public static AlarmManager alarmManager;
    private static PendingIntent pendingIntentAfWork;
    private static PendingIntent pendingIntentAfOffWork;

    private static PendingIntent pendingIntentMonWork;
    private static PendingIntent pendingIntentMonOffWork;

    private static int generateMinute(int week, int baseMinute) {
        int timeJitterValueInt = 3;
        int daysSinceLastRest = (week == 1) ? 0 : (week - 1);
        double negativeWeight = 1.0 - (daysSinceLastRest / 7.0);
        double random = new Random().nextDouble();
        double adjustedRandom = random * (1.0 - negativeWeight) + negativeWeight;
        double lambda = 1.0;
        double jitterValue = Math.log(1 - adjustedRandom) / (-lambda);
        int jitterValueInt = (int) Math.ceil(jitterValue);
        jitterValueInt = Math.min(jitterValueInt, timeJitterValueInt);
        if (random < negativeWeight) {
            return baseMinute - jitterValueInt;
        } else {
            return baseMinute + jitterValueInt;
        }
    }

    public static void timedTackMonWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        // 判断是否开启了时间抖动
        boolean isEnableTimeJitter = SharePrefHelper.INSTANCE.getBoolean(IS_ENABLE_TIME_JITTER, false);
        if (isEnableTimeJitter) {
            minute = generateMinute(AlarmReceiver.getWeek() + 1, minute);
        }

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
        configure(timeInMillis, pendingIntentMonWork);
    }


    public static void timedTackMonOffWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        // 判断是否开启了时间抖动
        boolean isEnableTimeJitter = SharePrefHelper.INSTANCE.getBoolean(IS_ENABLE_TIME_JITTER, false);
        if (isEnableTimeJitter) {
            minute = generateMinute(AlarmReceiver.getWeek() + 1, minute);
        }

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
        configure(timeInMillis, pendingIntentMonOffWork);
    }

    public static void timedTackAfWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        // 判断是否开启了时间抖动
        boolean isEnableTimeJitter = SharePrefHelper.INSTANCE.getBoolean(IS_ENABLE_TIME_JITTER, false);
        if (isEnableTimeJitter) {
            minute = generateMinute(AlarmReceiver.getWeek() + 1, minute);
        }

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
        configure(timeInMillis, pendingIntentAfWork);
    }

    public static void timedTackAfOffWork(Context activity, int hour, int minute, int requestCode) {
        activityA = activity;
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        // 判断是否开启了时间抖动
        boolean isEnableTimeJitter = SharePrefHelper.INSTANCE.getBoolean(IS_ENABLE_TIME_JITTER, false);
        if (isEnableTimeJitter) {
            minute = generateMinute(AlarmReceiver.getWeek() + 1, minute);
        }

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
        configure(timeInMillis, pendingIntentAfOffWork);
    }

    private static void configure(long timeInMillis, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                    return;
                }
            }
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
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