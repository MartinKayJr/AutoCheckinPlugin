package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;

public class SiginInProcessor extends BasePageProcessor {
    private final String TAG = "Weixin-SigninPageProcessor";

    @Override
    public void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        try {
            Thread.sleep(2500L);
            if (findNodesByText(myAccessibilityService, "你已在打卡范围内") != null) {
                // 意味着这个时间段已经打卡过了
                if (AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/bov", 0).toString().contains("后打卡")) {
                    Log.i("Weixin-SigninPageProcessor", "拦截重复打卡");
                    myAccessibilityService.clickHomeKey();
                    myAccessibilityService.closeApp("com.tencent.wework");
                    myAccessibilityService.autoLock();
                    return;
                }
                AccessibilityNodeInfo workSign = AccessibilityHelper.getNodeByText(myAccessibilityService, "上班打卡", 0);
                AccessibilityNodeInfo offWorkSign = AccessibilityHelper.getNodeByText(myAccessibilityService, "下班打卡", 0);

                if (workSign != null) {
                    AccessibilityHelper.clickButtonByNode(myAccessibilityService, workSign);
                    Thread.sleep(3000L);
                    Log.i("Weixin-SigninPageProcessor", "打卡成功-已完成，返回页面");
                    myAccessibilityService.clickHomeKey();
                    myAccessibilityService.closeApp("com.tencent.wework");
                    myAccessibilityService.autoLock();
                }

                if (offWorkSign != null) {
                    AccessibilityHelper.clickButtonByNode(myAccessibilityService, offWorkSign);
                    Thread.sleep(3000L);
                    Log.i("Weixin-SigninPageProcessor", "打卡成功-已完成，返回页面");
                    myAccessibilityService.clickHomeKey();
                    myAccessibilityService.closeApp("com.tencent.wework");
                    myAccessibilityService.autoLock();
                }
            } else {
                AccessibilityHelper.moveToUp(myAccessibilityService);
            }
            // 第一次打卡完成
            if (findNodesByText(myAccessibilityService, "今日打卡已完成") != null) {
                Log.i("Weixin-SigninPageProcessor", "打卡成功-已完成，返回页面");
                myAccessibilityService.clickHomeKey();
                myAccessibilityService.closeApp("com.tencent.wework");
                myAccessibilityService.autoLock();
            }
        } catch (Exception unused) {
            Log.i("Weixin", "工作台打卡失败");
        }
    }

    @Override
    public boolean canParse(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        return AccessibilityHelper.getNodeByText(myAccessibilityService, "打卡", 0) != null;
    }
}