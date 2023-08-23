package cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import cn.martinkay.autocheckinplugin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;

public class MessagePageProcessor extends BasePageProcessor {
    @Override
    public void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        try {
            AccessibilityHelper.touchViewByNode(myAccessibilityService, findNodesByText(myAccessibilityService, "工作台"));
        } catch (Exception unused) {
            Log.e("Weixin", "首页处理失败");
        }
    }

    @Override
    public boolean canParse(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        return AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/kld", 0) != null;
    }
}