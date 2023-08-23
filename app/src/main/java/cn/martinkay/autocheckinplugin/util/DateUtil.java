package cn.martinkay.autocheckinplugin.util;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/* loaded from: classes.dex */
public class DateUtil {
    static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    static DateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getDateTimeString(Date date) {
        return datetimeFormatter.format(date);
    }

    public static String getDateString(Date date) {
        return dateFormatter.format(date);
    }

    public static String getCurrentDateTimeString() {
        return datetimeFormatter.format(new Date());
    }

    public static String getCurrentDateString() {
        return dateFormatter.format(new Date());
    }

    public static int isTimeout(String deadLine) {
        try {
            return new Date().compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(deadLine));
        } catch (Exception unused) {
            return 1;
        }
    }
}