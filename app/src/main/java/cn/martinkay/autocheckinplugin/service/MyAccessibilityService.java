package cn.martinkay.autocheckinplugin.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.core.app.NotificationCompat;

import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.martinkay.autocheckinplugin.MainActivity;
import cn.martinkay.autocheckinplugin.R;
import cn.martinkay.autocheckinplugin.SignApplication;
import cn.martinkay.autocheckinplugin.constant.Constant;
import cn.martinkay.autocheckinplugin.handler.BaseHandler;
import cn.martinkay.autocheckinplugin.handler.WeixinHandler;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private final static int NOTIFICATION_ID = android.os.Process.myPid();
    private AssistServiceConnection mServiceConnection;

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
    @SuppressLint("WrongConstant")
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationChannel channel = new NotificationChannel("im_channel_id", "System", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, "im_channel_id")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setContentText("自动签到服务正在运行")
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            startForeground(NOTIFICATION_ID, notification);
        } else
            setForeground();
        return START_STICKY;
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setForeground() {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(NOTIFICATION_ID, getNotification());
            return;
        }
        if (mServiceConnection == null)
            mServiceConnection = new AssistServiceConnection();
        bindService(new Intent(this, AssistService.class), mServiceConnection,
                Service.BIND_AUTO_CREATE);
    }

    private class AssistServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Service assistService = ((AssistService.LocalBinder) service).getService();
            MyAccessibilityService.this.startForeground(NOTIFICATION_ID, getNotification());
            assistService.startForeground(NOTIFICATION_ID, getNotification());
            assistService.stopForeground(true);
            MyAccessibilityService.this.unbindService(mServiceConnection);
            mServiceConnection = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setContentTitle("自动签到服务运行于前台")
                .setContentText("service被设为前台进程")
                .setTicker("service正在后台运行...")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        return notification;
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

    public Boolean closeApp(String packageName) {
        if (Constant.isRoot) {
            try {
                if (Shell.su("am force-stop " + packageName).exec().isSuccess()) {
                    Log.i("MyAccessibilityService", "息屏成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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