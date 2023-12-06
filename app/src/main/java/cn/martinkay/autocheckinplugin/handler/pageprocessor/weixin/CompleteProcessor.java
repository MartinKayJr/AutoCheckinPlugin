package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;
import cn.martinkay.autocheckinplugin.utils.AutoSignPermissionUtils;

public class CompleteProcessor extends BasePageProcessor {
    @Override public boolean canParse(AccessibilityEvent event,
            MyAccessibilityService myAccessibilityService) {
        if (AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/bov", 0)
                != null && AccessibilityHelper.getNodeById(myAccessibilityService,
                "com.tencent.wework:id/bov", 0).toString().contains("后打卡")) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "后打卡", 0) != null) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "今日打卡已完成，好好休息", 0)
                != null) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "上班·正常", 0) != null) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "下班·正常", 0) != null) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "下班自动打卡·正常", 0)
                != null) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "上班自动打卡·正常", 0)
                != null) {
            return true;
        }
        if (AccessibilityHelper.getNodeByText(myAccessibilityService, "上班自动打卡·正常", 0)
                != null) {
            return true;
        }
        return false;
    }

    @Override public void processPage(AccessibilityEvent event,
            MyAccessibilityService myAccessibilityService) {
        if (!AutoSignPermissionUtils.INSTANCE.isMobileAutoSignLaunch()) {
            return;
        }
        Log.w("CompleteProcessor", "打卡成功 关闭");
        AutoSignPermissionUtils.INSTANCE.increaseTodayAutoSignCount();
        myAccessibilityService.clickHomeKey();
        myAccessibilityService.closeApp("com.tencent.wework");
        myAccessibilityService.autoLock();
    }
}
