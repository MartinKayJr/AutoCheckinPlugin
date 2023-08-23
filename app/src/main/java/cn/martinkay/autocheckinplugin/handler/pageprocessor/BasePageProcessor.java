package cn.martinkay.autocheckinplugin.handler.pageprocessor;

import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cn.martinkay.autocheckinplugin.service.MyAccessibilityService;

/* loaded from: classes.dex */
public abstract class BasePageProcessor {
    Random random = new Random();

    public abstract boolean canParse(AccessibilityEvent event, MyAccessibilityService myAccessibilityService);

    public abstract void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService);

    public void printAllNodes(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        ArrayList<AccessibilityNodeInfo> nodesFromWindows = myAccessibilityService.getNodesFromWindows();
        if (nodesFromWindows != null) {
            Iterator<AccessibilityNodeInfo> it = nodesFromWindows.iterator();
            while (it.hasNext()) {
                executeOperation(it.next());
            }
        }
    }

    private boolean executeOperation(AccessibilityNodeInfo info) {
        if (info == null) {
            return false;
        }
        if (info.getChildCount() == 0) {
            System.out.println(info.toString());
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    executeOperation(info.getChild(i));
                }
            }
        }
        return false;
    }

    public AccessibilityNodeInfo findNodesByText(MyAccessibilityService myAccessibilityService, String text) {
        ArrayList<AccessibilityNodeInfo> nodesFromWindows = myAccessibilityService.getNodesFromWindows();
        LinkedList<AccessibilityNodeInfo> linkedList = new LinkedList();
        if (nodesFromWindows != null) {
            Iterator<AccessibilityNodeInfo> it = nodesFromWindows.iterator();
            while (it.hasNext()) {
                listAllNodes(it.next(), linkedList);
            }
        }
        for (AccessibilityNodeInfo accessibilityNodeInfo : linkedList) {
            if (accessibilityNodeInfo.getText() != null) {
                System.out.println(accessibilityNodeInfo.getText());
                if (text.equals(accessibilityNodeInfo.getText().toString())) {
                    return accessibilityNodeInfo;
                }
            }
            if (Build.VERSION.SDK_INT >= 26 && accessibilityNodeInfo.getHintText() != null && text.equals(accessibilityNodeInfo.getHintText().toString())) {
                return accessibilityNodeInfo;
            }
        }
        return null;
    }

    private void listAllNodes(AccessibilityNodeInfo info, List<AccessibilityNodeInfo> allNodes) {
        if (info == null) {
            return;
        }
        if (info.getChildCount() == 0) {
            allNodes.add(info);
            return;
        }
        for (int i = 0; i < info.getChildCount(); i++) {
            if (info.getChild(i) != null) {
                listAllNodes(info.getChild(i), allNodes);
            }
        }
    }
}