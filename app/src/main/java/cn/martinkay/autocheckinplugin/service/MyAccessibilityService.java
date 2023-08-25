package cn.martinkay.autocheckinplugin.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.martinkay.autocheckinplugin.SignApplication;
import cn.martinkay.autocheckinplugin.constant.Constant;
import cn.martinkay.autocheckinplugin.handler.BaseHandler;
import cn.martinkay.autocheckinplugin.handler.WeixinHandler;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    public static int height = 1000;
    public static int width = 1000;
    String className;
    public List<BaseHandler> list;
    private String nowPackageName;
    String p_c;

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;
        System.out.println("屏幕尺寸：" + width + ":" + height);
        super.onServiceConnected();
        this.list = new LinkedList<>();
        this.list.add(new WeixinHandler());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() != null) {
            this.nowPackageName = event.getPackageName().toString();
            String charSequence = event.getClassName().toString();
            this.className = charSequence;
            if (charSequence.contains("com.")) {
//                if (this.className.contains(this.nowPackageName)) {
//                    String replace = this.className.replace(this.nowPackageName, BuildConfig.FLAVOR);
//                    this.className = replace;
//                    String replace2 = replace.replace("..", BuildConfig.FLAVOR);
//                    this.className = replace2;
//                    if (replace2.charAt(0) == '.') {
//                        this.className = this.className.substring(1);
//                    }
//                }
                this.p_c = this.nowPackageName + this.className;
                Log.i(TAG, "包名：" + this.nowPackageName + "，类名：" + this.className);
            }
        }
        if (this.nowPackageName == null || !SignApplication.Companion.getInstance().getFlag()) {
            return;
        }
        Log.i(TAG, "收到事件类型ID:" + event.getEventType());
        try {
            for (BaseHandler baseHandler : this.list) {
                if (baseHandler.canHandler(this.nowPackageName)) {
                    baseHandler.doHandle(event, this);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "事件处理失败", e);
        }
    }

    public ArrayList<AccessibilityNodeInfo> getNodesFromWindows() {
        List<AccessibilityWindowInfo> windows = getWindows();
        ArrayList<AccessibilityNodeInfo> arrayList = new ArrayList<>();
        if (windows.size() > 0) {
            for (AccessibilityWindowInfo accessibilityWindowInfo : windows) {
                arrayList.add(accessibilityWindowInfo.getRoot());
            }
        }
        return arrayList;
    }

    public Boolean clickHomeKey() {
        return Boolean.valueOf(performGlobalAction(2));
    }

    /**
     * 自动息屏
     *
     * @return
     */
    public Boolean autoLock() {
        if (Constant.isRoot) {
            try {
                if (Shell.su("input keyevent 26").exec().isSuccess()) {
                    Log.i("MyAccessibilityService", "息屏成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected boolean onGesture(int gestureId) {
        return super.onGesture(gestureId);
    }
}