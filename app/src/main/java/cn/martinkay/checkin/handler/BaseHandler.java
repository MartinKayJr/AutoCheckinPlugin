package cn.martinkay.checkin.handler;

import android.view.accessibility.AccessibilityEvent;

import cn.martinkay.checkin.service.MyAccessibilityService;

public interface BaseHandler {
    boolean canHandler(String packageName);

    void doHandle(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) throws Exception;

}
