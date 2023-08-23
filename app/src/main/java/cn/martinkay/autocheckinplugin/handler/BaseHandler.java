package cn.martinkay.autocheckinplugin.handler;

import android.view.accessibility.AccessibilityEvent;

import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;

public interface BaseHandler {
    boolean canHandler(String packageName);

    void doHandle(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) throws Exception;

}
