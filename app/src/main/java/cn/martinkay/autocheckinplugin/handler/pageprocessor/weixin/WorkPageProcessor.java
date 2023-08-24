package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;


import android.graphics.Path;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;

/**
 * 在控制台页面中，需要向下滑动直到找到打卡按钮，然后点击
 */
public class WorkPageProcessor extends BasePageProcessor {
    @Override
    public void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        try {
            Thread.sleep(1000L);
            AccessibilityNodeInfo findNodesByText = findNodesByText(myAccessibilityService, "打卡");
            if (findNodesByText == null) {
                Path path = new Path();
                float f = MyAccessibilityService.width / 2;
                path.moveTo(f, MyAccessibilityService.height * 0.8f);
                path.lineTo(f, MyAccessibilityService.height * 0.4f);
                AccessibilityHelper.dispatchGestrue(path, myAccessibilityService);
                return;
            }
            AccessibilityHelper.clickButtonByNode(myAccessibilityService, findNodesByText);
        } catch (Exception unused) {
            Log.i("Weixin", "工作台打卡失败");
        }
    }

    @Override
    public boolean canParse(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        // 顶部的文字
        AccessibilityNodeInfo topTextView = AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/lkw", 0);
        boolean isMessagePage = topTextView.getText().toString().contains("工作台");
        return isMessagePage;
    }
}