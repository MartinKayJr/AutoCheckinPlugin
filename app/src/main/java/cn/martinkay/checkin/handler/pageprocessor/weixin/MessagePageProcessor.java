package cn.martinkay.checkin.handler.pageprocessor.weixin;

import static cn.martinkay.checkin.SharePrefHelperKt.ENABLE_START_QUICK_SIGN;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.martinkay.checkin.SharePrefHelper;
import cn.martinkay.checkin.handler.pageprocessor.BasePageProcessor;
import cn.martinkay.checkin.service.MyAccessibilityService;
import cn.martinkay.checkin.util.AccessibilityHelper;
import cn.martinkay.checkin.utils.AutoSignPermissionUtils;

public class MessagePageProcessor extends BasePageProcessor {
    @Override
    public void processPage(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        try {
            boolean enableStartQuickSign = SharePrefHelper.INSTANCE.getBoolean(ENABLE_START_QUICK_SIGN, false);
            if (enableStartQuickSign) {
                // 如果开启了兼容启动快捷打卡，则查找 打卡这个应用的消息
                /**
                 * 需要根据时间的上下5分钟的范围来确定是否是最近的一次打卡消息，否则可能会误判
                 */
                // 获取当前时间的上下5分钟的范围
                long fiveMinutes = 5 * 60 * 1000;
                // 获取消息列表
                AccessibilityNodeInfo offWork = AccessibilityHelper.getNodeByText(myAccessibilityService, "下班自动打卡", 0);
                // xx:xx下班自动打卡 取xx:xx
                if (offWork != null) {
                    String offWorkText = offWork.getText().toString();
                    String time = offWorkText.substring(0, offWorkText.indexOf("下班自动打卡"));
                    // time是xx:xx，转换为时间戳
                    // 定义时间格式
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    // 将时间字符串解析为Date对象
                    Date date = sdf.parse(time);

                    // 获取当前时间
                    Date currentTime = new Date();

                    // 将当前时间转换为时分格式
                    String currentTimeString = sdf.format(currentTime);
                    Date currentDate = sdf.parse(currentTimeString);

                    // 计算时间差值
                    long diff = Math.abs(date.getTime() - currentDate.getTime());

                    // 如果时间差值在5分钟内，则认为是最近的一次打卡消息
                    if (diff <= fiveMinutes) {
                        Log.w("CompleteProcessor", "打卡成功 关闭");
                        AutoSignPermissionUtils.INSTANCE.increaseTodayAutoSignCount();
                        myAccessibilityService.clickHomeKey();
                        myAccessibilityService.closeApp("com.tencent.wework");
                        myAccessibilityService.autoLock();
                        return;
                    }
                } else {
                    AccessibilityNodeInfo work = AccessibilityHelper.getNodeByText(myAccessibilityService, "上班自动打卡", 0);
                    if (work != null) {

                        String workText = work.getText().toString();
                        String time = workText.substring(0, workText.indexOf("上班自动打卡"));
                        // time是xx:xx，转换为时间戳
                        // 定义时间格式
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        // 将时间字符串解析为Date对象
                        Date date = sdf.parse(time);

                        // 获取当前时间
                        Date currentTime = new Date();

                        // 将当前时间转换为时分格式
                        String currentTimeString = sdf.format(currentTime);
                        Date currentDate = sdf.parse(currentTimeString);

                        // 计算时间差值
                        long diff = Math.abs(date.getTime() - currentDate.getTime());

                        // 如果时间差值在5分钟内，则认为是最近的一次打卡消息
                        if (diff <= fiveMinutes) {
                            Log.w("CompleteProcessor", "打卡成功 关闭");
                            AutoSignPermissionUtils.INSTANCE.increaseTodayAutoSignCount();
                            myAccessibilityService.clickHomeKey();
                            myAccessibilityService.closeApp("com.tencent.wework");
                            myAccessibilityService.autoLock();
                            return;
                        }
                    }
                }
                AccessibilityHelper.clickButtonByNode(myAccessibilityService, findNodesByText(myAccessibilityService, "工作台"));
            } else {
                AccessibilityHelper.clickButtonByNode(myAccessibilityService, findNodesByText(myAccessibilityService, "工作台"));
            }
        } catch (Exception unused) {
            Log.e("Weixin", "首页处理失败");
        }
    }

    @Override
    public boolean canParse(AccessibilityEvent event, MyAccessibilityService myAccessibilityService) {
        // 顶部的文字
        AccessibilityNodeInfo topTextView = AccessibilityHelper.getNodeById(myAccessibilityService, "com.tencent.wework:id/lkw", 0);
        if (topTextView != null) {
            return topTextView.getText().toString().contains("消息");
        } else {
            return false;
        }
    }
}