package cn.martinkay.autocheckinplugin.util;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.Random;

import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;

public class AccessibilityHelper {
    private static String TAG = "AccessibilityHelper";
    private static Random random = new Random();

    public static boolean clickButtonById(MyAccessibilityService myAccessibilityService, String id, int index) {
        AccessibilityNodeInfo nodeById = getNodeById(myAccessibilityService, id, 0);
        if (nodeById != null) {
            while (!nodeById.isClickable()) {
                nodeById = nodeById.getParent();
            }
            nodeById.performAction(16);
            return true;
        }
        return false;
    }

    public static boolean clickButtonByText(MyAccessibilityService myAccessibilityService, String text, int index) {
        AccessibilityNodeInfo nodeByText = getNodeByText(myAccessibilityService, text, 0);
        if (nodeByText != null) {
            while (!nodeByText.isClickable()) {
                nodeByText = nodeByText.getParent();
            }
            nodeByText.performAction(16);
            return true;
        }
        return false;
    }

    public static boolean clickButtonByNode(MyAccessibilityService myAccessibilityService, AccessibilityNodeInfo node) {
        if (node != null) {
            while (!node.isClickable()) {
                node = node.getParent();
            }
            node.performAction(16);
            return true;
        }
        return false;
    }

    public static boolean touchViewByText(MyAccessibilityService myAccessibilityService, String text, int index) {
        AccessibilityNodeInfo nodeByText = getNodeByText(myAccessibilityService, text, index);
        if (nodeByText != null) {
            Rect rect = new Rect();
            nodeByText.getBoundsInScreen(rect);
            Path path = new Path();
            float nextInt = rect.left + random.nextInt(10);
            float nextInt2 = rect.top + random.nextInt(10);
            path.moveTo(nextInt, nextInt2);
            path.lineTo(nextInt, nextInt2);
            dispatchGestrue(path, myAccessibilityService);
            return true;
        }
        return false;
    }

    public static boolean touchViewByNode(MyAccessibilityService myAccessibilityService, AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            Rect rect = new Rect();
            nodeInfo.getBoundsInScreen(rect);
            Path path = new Path();
            float nextInt = rect.left + random.nextInt(10);
            float nextInt2 = rect.top + random.nextInt(10);
            path.moveTo(nextInt, nextInt2);
            path.lineTo(nextInt, nextInt2);
            dispatchGestrue(path, myAccessibilityService);
            return true;
        }
        return false;
    }

    public static AccessibilityNodeInfo getNodeById(MyAccessibilityService myAccessibilityService, String id, int index) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId;
        AccessibilityNodeInfo rootInActiveWindow = myAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null || (findAccessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(id)) == null || findAccessibilityNodeInfosByViewId.size() <= 0) {
            return null;
        }
        return index > findAccessibilityNodeInfosByViewId.size() ? findAccessibilityNodeInfosByViewId.get(findAccessibilityNodeInfosByViewId.size()) : findAccessibilityNodeInfosByViewId.get(index);
    }

    public static AccessibilityNodeInfo getNodeByText(MyAccessibilityService myAccessibilityService, String text, int index) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText;
        AccessibilityNodeInfo rootInActiveWindow = myAccessibilityService.getRootInActiveWindow();
        if (rootInActiveWindow == null || (findAccessibilityNodeInfosByText = rootInActiveWindow.findAccessibilityNodeInfosByText(text)) == null || findAccessibilityNodeInfosByText.size() <= 0) {
            return null;
        }
        return index > findAccessibilityNodeInfosByText.size() ? findAccessibilityNodeInfosByText.get(findAccessibilityNodeInfosByText.size()) : findAccessibilityNodeInfosByText.get(index);
    }

    public static void dispatchGestrue(Path path, MyAccessibilityService myAccessibilityService) {
        Log.d(TAG, "滑动:" + myAccessibilityService.dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, 20L, 300L)).build(), new AccessibilityService.GestureResultCallback() { // from class: com.ycy.accessibilityservicetest.Util.AccessibilityHelper.1
            @Override // android.accessibilityservice.AccessibilityService.GestureResultCallback
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override // android.accessibilityservice.AccessibilityService.GestureResultCallback
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null));
    }

    public static void dispatchGestrueByRect(Rect rect, MyAccessibilityService myAccessibilityService) {
        Path path = new Path();
        float nextInt = rect.left + random.nextInt(10);
        float nextInt2 = rect.top + random.nextInt(10);
        path.moveTo(nextInt, nextInt2);
        path.lineTo(nextInt, nextInt2);
        dispatchGestrue(path, myAccessibilityService);
    }

    public static void goBack(MyAccessibilityService myAccessibilityService) {
        myAccessibilityService.performGlobalAction(1);
        Log.i(TAG, "执行全局返回动作");
    }

    public static void moveToUp(MyAccessibilityService myAccessibilityService) {
        Path path = new Path();
        path.moveTo((MyAccessibilityService.width / 2) + random.nextInt((int) (MyAccessibilityService.width * 0.1d)), (MyAccessibilityService.height * 0.8f) + random.nextInt((int) (MyAccessibilityService.height * 0.1d)));
        path.lineTo((MyAccessibilityService.width / 2) + random.nextInt((int) (MyAccessibilityService.width * 0.1d)), (MyAccessibilityService.height * 0.2f) + random.nextInt((int) (MyAccessibilityService.height * 0.1d)));
        dispatchGestrue(path, myAccessibilityService);
    }
}