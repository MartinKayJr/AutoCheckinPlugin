package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;

public class LoginProcessor extends BasePageProcessor {
    @Override
    public boolean canParse(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        AccessibilityNodeInfo topTextView = AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/lkw", 0);

        return false;
    }

    @Override
    public void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {

    }
}
