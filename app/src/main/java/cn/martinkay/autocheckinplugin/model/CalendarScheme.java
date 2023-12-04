package cn.martinkay.autocheckinplugin.model;

import android.text.TextUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.haibin.calendarview.Calendar;

public class CalendarScheme {

    public static final String AUTO_SIGN_COUNT_1 = "auto_sign_count_1";
    public static final String AUTO_SIGN_COUNT_2 = "auto_sign_count_2";
    public static final String AUTO_SIGN_COUNT_3 = "auto_sign_count_3";
    public static final String AUTO_SIGN_COUNT_4 = "auto_sign_count_4";
    public static final String AUTO_SIGN_DAY_ALLOW = "auto_sign_day_allow";
    public static final String AUTO_SIGN_DAY_FORBIDDEN = "auto_sign_day_forbidden";

    @JSONField(name = "date") public String date;
    @JSONField(name = "scheme") public String scheme;

    public void nextScheme() {
        if (TextUtils.isEmpty(scheme)) {
            scheme = AUTO_SIGN_COUNT_1;
            return;
        }
        switch (scheme) {
            case AUTO_SIGN_COUNT_1:
                scheme = AUTO_SIGN_COUNT_2;
                break;
            case AUTO_SIGN_COUNT_2:
                scheme = AUTO_SIGN_COUNT_3;
                break;
            case AUTO_SIGN_COUNT_3:
            case AUTO_SIGN_COUNT_4:
                scheme = AUTO_SIGN_COUNT_4;
                break;
            default:
                scheme = AUTO_SIGN_COUNT_1;
        }
    }

    /**
     * 是否是未来的打卡任务，包括今天
     */
    public boolean isFutureTask() {
        java.util.Calendar calendarInstance = java.util.Calendar.getInstance();
        Calendar currentCalendar = new Calendar();
        currentCalendar.setYear(calendarInstance.get(java.util.Calendar.YEAR));
        currentCalendar.setMonth(calendarInstance.get(java.util.Calendar.MONTH) + 1);
        currentCalendar.setDay(calendarInstance.get(java.util.Calendar.DAY_OF_MONTH));
        return date.compareTo(currentCalendar.toString()) >= 0;
    }
}
