package cn.martinkay.checkin.model;


import cn.martinkay.checkin.constant.Constant;

public class WeixinInfo extends ActiveAppinfo {
    public static final String name = "weixin";
    public static final String version = "v1.0";

    public WeixinInfo() {
        setAppName(name);
        setModuleName(name);
        setPackageName(Constant.package_name_weixin);
        setActivityName(Constant.className_weixin);
    }
}