package cn.martinkay.checkin.handler.pageprocessor.weixin;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import cn.martinkay.checkin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.checkin.service.MyAccessibilityService;
import cn.martinkay.checkin.util.AccessibilityHelper;
import cn.martinkay.checkin.utils.AutoSignPermissionUtils;

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
        if (!AutoSignPermissionUtils.INSTANCE.isMobileAutoSignLaunch()) {
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