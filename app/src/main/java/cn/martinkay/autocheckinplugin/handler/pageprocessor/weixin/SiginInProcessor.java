package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;

/* loaded from: classes.dex */
public class SiginInProcessor extends BasePageProcessor {
    private final String TAG = "Weixin-SigninPageProcessor";

    @Override
    public void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        try {
            Thread.sleep(2500L);
            if (findNodesByText(myAccessibilityService, "你已在打卡范围内") != null) {
                AccessibilityNodeInfo workSign = AccessibilityHelper.getNodeByText(myAccessibilityService, "上班打卡", 0);
                AccessibilityNodeInfo offWorkSign = AccessibilityHelper.getNodeByText(myAccessibilityService, "下班打卡", 0);

                if (workSign != null) {
                    AccessibilityHelper.clickButtonByNode(myAccessibilityService, workSign);
                    Thread.sleep(3000L);
                    Log.i("Weixin-SigninPageProcessor", "打卡成功-已完成，返回页面");
                    myAccessibilityService.clickHomeKey();
                }

                if (offWorkSign != null) {
                    AccessibilityHelper.clickButtonByNode(myAccessibilityService, offWorkSign);
                    Thread.sleep(3000L);
                    Log.i("Weixin-SigninPageProcessor", "打卡成功-已完成，返回页面");
                    myAccessibilityService.clickHomeKey();
                }
            } else {
                AccessibilityHelper.moveToUp(myAccessibilityService);
            }
            if (findNodesByText(myAccessibilityService, "今日打卡已完成") != null) {
                Log.i("Weixin-SigninPageProcessor", "打卡成功-已完成，返回页面");
                myAccessibilityService.clickHomeKey();
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