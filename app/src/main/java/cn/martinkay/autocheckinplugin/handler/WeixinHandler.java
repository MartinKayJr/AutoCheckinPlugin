package cn.martinkay.autocheckinplugin.handler;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin.MessagePageProcessor;
import cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin.SiginInProcessor;
import cn.martinkay.autocheckinplugin.handler.pageprocessor.weixin.WorkPageProcessor;
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;
import cn.martinkay.autocheckinplugin.util.AccessibilityHelper;

public class WeixinHandler implements BaseHandler {
    private static final String TAG = "WeixinHandler";
    public static final String packageName = "com.tencent.wework";
    private WorkPageProcessor workPageProcessor = new WorkPageProcessor();
    private MessagePageProcessor messagePageProcessor = new MessagePageProcessor();
    private SiginInProcessor siginInProcessor = new SiginInProcessor();

    @Override
    public void doHandle(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) throws Exception {
        AccessibilityNodeInfo nodeById = AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/hrb", 0);
        if (nodeById != null) {
            AccessibilityHelper.clickButtonByNode(myAccessibilityService, nodeById);
        } else if (this.messagePageProcessor.canParse(event, myAccessibilityService)) {
            this.messagePageProcessor.processPage(event, myAccessibilityService);
        } else if (this.workPageProcessor.canParse(event, myAccessibilityService)) {
            this.workPageProcessor.processPage(event, myAccessibilityService);
        } else if (this.siginInProcessor.canParse(event, myAccessibilityService)) {
            this.siginInProcessor.processPage(event, myAccessibilityService);
        }
    }

    @Override
    public boolean canHandler(String packageName2) {
        return packageName.equals(packageName2);
    }
}