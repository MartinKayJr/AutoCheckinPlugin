package cn.martinkay.checkin.handler;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import cn.martinkay.checkin.handler.pageprocessor.weixin.CompleteProcessor;
import cn.martinkay.checkin.handler.pageprocessor.weixin.MessagePageProcessor;
import cn.martinkay.checkin.handler.pageprocessor.weixin.SiginInProcessor;
import cn.martinkay.checkin.handler.pageprocessor.weixin.WorkPageProcessor;
import cn.martinkay.checkin.service.MyAccessibilityService;
import cn.martinkay.checkin.util.AccessibilityHelper;
import cn.martinkay.checkin.utils.AutoSignPermissionUtils;

public class WeixinHandler implements BaseHandler {
    private static final String TAG = "WeixinHandler";
    public static final String packageName = "com.tencent.wework";
    private WorkPageProcessor workPageProcessor = new WorkPageProcessor();
    private MessagePageProcessor messagePageProcessor = new MessagePageProcessor();
    private SiginInProcessor siginInProcessor = new SiginInProcessor();

    private CompleteProcessor completeProcessor = new CompleteProcessor();

    @Override
    public void doHandle(AccessibilityEvent event, MyAccessibilityService myAccessibilityService)
            throws Exception {
        if (!AutoSignPermissionUtils.INSTANCE.isMobileAutoSignLaunch()) {
            return;
        }
        AccessibilityNodeInfo nodeById =
                AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/hrb",
                        0);
        if (nodeById != null) {
            AccessibilityHelper.clickButtonByNode(myAccessibilityService, nodeById);
        } else if (this.messagePageProcessor.canParse(event, myAccessibilityService)) {
            // 首页
            this.messagePageProcessor.processPage(event, myAccessibilityService);
        } else if (this.workPageProcessor.canParse(event, myAccessibilityService)) {
            // 控制台
            this.workPageProcessor.processPage(event, myAccessibilityService);
        } else if (this.completeProcessor.canParse(event, myAccessibilityService)) {
            // 打卡完成页
            this.completeProcessor.processPage(event, myAccessibilityService);
        } else if (this.siginInProcessor.canParse(event, myAccessibilityService)) {
            // 打卡页
            this.siginInProcessor.processPage(event, myAccessibilityService);
        }
    }

    @Override public boolean canHandler(String packageName2) {
        return packageName.equals(packageName2);
    }
}