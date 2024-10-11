package cn.martinkay.checkin.constant;

import cn.martinkay.checkin.model.ActiveAppinfo;
import cn.martinkay.checkin.model.WeixinInfo;

public class Constant {

    public static ActiveAppinfo activeAppinfo = null;

    public static String active_app = "weixin";
    public static String package_name_weixin = "com.tencent.wework";
    public static String className_weixin = "com.tencent.wework.launch.LaunchSplashActivity";

    public static boolean isRandomPkg = false;

    public static String pkg = "cn.martinkay.autocheckinplugin";

    public static boolean isRoot = false;

    public static boolean isShizuku = false;

    public static ActiveAppinfo getActiveApp() {
        if (activeAppinfo == null) {
            String str = active_app;
            if (str.equals(WeixinInfo.name)) {
                activeAppinfo = new WeixinInfo();
            }
        }
        return activeAppinfo;
    }

}
