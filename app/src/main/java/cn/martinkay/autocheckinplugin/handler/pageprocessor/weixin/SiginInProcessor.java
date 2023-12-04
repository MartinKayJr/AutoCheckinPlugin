package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import cn.martinkay.autocheckinplugin.SharePrefHelper;
import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;
import cn.martinkay.autocheckinplugin.utils.AutoSignPermissionUtils;

import static cn.martinkay.autocheckinplugin.SharePrefHelperKt.SIGN_OPEN_INTENT_START_TIME;

public class SiginInProcessor extends BasePageProcessor {
    private final String TAG = "SigninPageProcessor";

    @Override public void processPage(AccessibilityEvent event,
            MyAccessibilityService myAccessibilityService) {
        try {
            Thread.sleep(2500L);
            if (findNodesByText(myAccessibilityService, "你已在打卡范围内") != null) {
                // 意味着这个时间段已经打卡过了
                AccessibilityNodeInfo workSign =
                        AccessibilityHelper.getNodeByText(myAccessibilityService, "上班打卡", 0);
                AccessibilityNodeInfo offWorkSign =
                        AccessibilityHelper.getNodeByText(myAccessibilityService, "下班打卡", 0);

                if (workSign != null) {
                    AccessibilityHelper.clickButtonByNode(myAccessibilityService, workSign);
                    Thread.sleep(3000L);
                    Log.i("SigninPageProcessor", "打卡成功-已完成，返回页面");
                    runAccessibilityService(myAccessibilityService);
                }

                if (offWorkSign != null) {
                    AccessibilityHelper.clickButtonByNode(myAccessibilityService, offWorkSign);
                    Thread.sleep(3000L);
                    Log.i("SigninPageProcessor", "打卡成功-已完成，返回页面");
                    runAccessibilityService(myAccessibilityService);
                }
            } else {
                AccessibilityHelper.moveToUp(myAccessibilityService);
            }
            // 第一次打卡完成
            if (findNodesByText(myAccessibilityService, "今日打卡已完成") != null) {
                Log.i("SigninPageProcessor", "打卡成功-已完成，返回页面");
                runAccessibilityService(myAccessibilityService);
            }
        } catch (Exception unused) {
            Log.i("SigninPageProcessor", "工作台打卡失败");
        }
    }

    private void runAccessibilityService(MyAccessibilityService myAccessibilityService) {
        long startTime = SharePrefHelper.INSTANCE.getLong(SIGN_OPEN_INTENT_START_TIME, 0);
        if ((System.currentTimeMillis() - startTime) > 5000) {
            Log.i("SigninPageProcessor", "不是由程序打开的，忽略");
            return;
        }
        AutoSignPermissionUtils.INSTANCE.increaseTodayAutoSignCount();
        myAccessibilityService.clickHomeKey();
        myAccessibilityService.closeApp("com.tencent.wework");
        myAccessibilityService.autoLock();
    }

    @Override public boolean canParse(AccessibilityEvent event,
            MyAccessibilityService myAccessibilityService) {
        return AccessibilityHelper.getNodeByText(myAccessibilityService, "打卡", 0) != null;
    }
}