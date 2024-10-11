package cn.martinkay.checkin.handler.pageprocessor.weixin;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.martinkay.checkin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.checkin.service.MyAccessibilityService;
import cn.martinkay.checkin.util.AccessibilityHelper;

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
