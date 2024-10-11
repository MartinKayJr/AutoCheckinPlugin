package cn.martinkay.randompkg.utils;

import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class ShizukuUtil implements Shizuku.OnBinderReceivedListener, Shizuku.OnBinderDeadListener, Shizuku.OnRequestPermissionResultListener {

    private static final String TAG = "ShizukuUtil";

    public static String installApk(String appPath) {
        if (!checkPermission()) {
            return "没有Shizuku权限";
        }
        File file = new File(appPath);
        if (file.exists()) {
            return ShizukuUtil.shizukuListener.doInstallApk(file);
        }
        return "安装包不存在";
    }


    private Context context;

    private boolean onBinderReceived = false;

    public ShizukuUtil(Context context) {
        this.context = context;
    }

    @Override
    public void onBinderReceived() {
        if (this.onBinderReceived) {
            return;
        }
        this.onBinderReceived = true;
        //Shizuku建立连接后，立即开始检查权限并申请
        Log.d(TAG, "onBinderReceived[已建立连接]");
    }

    @Override
    public void onBinderDead() {
        this.onBinderReceived = false;
        //Shizuku 服务死了
        Log.d(TAG, "onBinderDead[Shizuku 服务死了]");
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (!this.onBinderReceived
                && grantResult == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Shizuku授权成功", Toast.LENGTH_SHORT).show();
        }
    }

    public static final int defaultRequestCode = 0x100;

    private static ShizukuUtil shizukuListener;
    //添加监听器
    public static void initialized(Context context) {
//        if( !ContextUtil.isMainProcess()){
//            return;
//        }
        Log.d(TAG, "Shizuku初始化");
        if (ShizukuUtil.shizukuListener != null) {
            //ShizukuUtil.shizukuListener.removeBinderListener();
            //ShizukuUtil.shizukuListener.addBinderListener();
            return;
        }

        ShizukuUtil.shizukuListener = new ShizukuUtil(context);
        ShizukuUtil.shizukuListener.addBinderListener();
    }

    private void addBinderListener() {
        //监听是否连接
        Shizuku.addBinderReceivedListenerSticky(this);
        //监听Binder死亡
        Shizuku.addBinderDeadListener(this);
        //添加申请权限回调
        Shizuku.addRequestPermissionResultListener(this);
    }

    //移除监听器
    public void removeBinderListener() {
        Shizuku.removeBinderReceivedListener(this);
        Shizuku.removeBinderDeadListener(this);
        Shizuku.removeRequestPermissionResultListener(this);
    }

    public static boolean checkPermission() {
        return ShizukuUtil.shizukuListener.onBinderReceived || ShizukuUtil.shizukuListener.checkPermission(defaultRequestCode);
    }

    private boolean checkPermission(int code) {
        if (Shizuku.isPreV11()) {
            return false;
        }
        try {
            if( !Shizuku.pingBinder()){
                return false;
            }
            //检查自己是否有权限。
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            if (Shizuku.shouldShowRequestPermissionRationale()) {
                return false;
            }
            //申请权限
            //结果异步返回 addRequestPermissionResultListener中添加的回调
            Shizuku.requestPermission(code);

            return false;
        }
        catch (Throwable e) {
            Log.e(TAG, "checkPermission", e);
        }

        return false;
    }




    private String doInstallApk(File apkFile) {

        PackageInstaller packageInstaller;
        PackageInstaller.Session session = null;

        String installerAttributionTag = null;
        int userId = 0;

        int status = -1;

        StringBuilder infoBuilder = new StringBuilder();

        try {
            // the reason for use "com.android.shell" as installer package under adb is that getMySessions will check installer package's owner
            //使用“com.android.shell”作为adb下的安装程序包的原因是getMySessions将检查安装程序包所有者
            String installerPackageName = "com.android.shell";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                installerAttributionTag =  context.getAttributionTag();
            }

            IPackageInstaller _packageInstaller = getPackageInstaller();
            packageInstaller = createPackageInstaller(this.context, _packageInstaller, installerPackageName, installerAttributionTag, userId);


            // 利用 PackageInstaller openSession安装apk
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int installFlags = getInstallFlags(params);
            installFlags |= 0x00000004/*PackageManager.INSTALL_ALLOW_TEST*/ | 0x00000002/*PackageManager.INSTALL_REPLACE_EXISTING*/;
            setInstallFlags(params, installFlags);

            int sessionId = packageInstaller.createSession(params);

            IPackageInstallerSession _session = IPackageInstallerSession.Stub.asInterface(new ShizukuBinderWrapper(_packageInstaller.openSession(sessionId).asBinder()));
            session = createSession(_session);
            // 会检查uid uid不对
            //packageInstaller.openSession(sessionId);

            String name = apkFile.getName();
            // 利用 Session写入
            InputStream is = new FileInputStream(apkFile);
            OutputStream os = session.openWrite(name, 0, -1);
            writeApk(is, os, session);
            // 等待0.1s
            Thread.sleep(100);

            final Intent[] results = new Intent[1];
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            session.commit(newInstance(new IIntentSenderAdaptor() {
                @Override
                public void send(Intent intent) {
                    results[0] = intent;
                    // -1
                    countDownLatch.countDown();
                }
            }));
            // 等待安装完毕
            countDownLatch.await();

            Intent result = results[0];

            status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);

            String message = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

            infoBuilder.append("status: ")
                    .append(status)
                    .append(" (")
                    .append(message)
                    .append(")");

        }
        catch (Throwable tr) {
            infoBuilder.append(tr);
            Log.e(TAG, "doInstallApk", tr);
        }
        finally {
            session.close();
            Log.d(TAG, infoBuilder.toString().trim());
        }

        if (status == 0) {
            //安装成功
            return null;
        }
        return infoBuilder.toString().trim();
    }

    private void writeApk(InputStream is, OutputStream os, PackageInstaller.Session session) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        try {
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
                os.flush();
                session.fsync(os);
            }
        }
        finally {
            is.close();
            os.close();
        }
    }

    public static IntentSender newInstance(IIntentSender binder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return IntentSender.class.getConstructor(IIntentSender.class).newInstance(binder);
    }

    public static IPackageInstaller getPackageInstaller() throws RemoteException {
        // 系统服务[package]
        IBinder systemService = SystemServiceHelper.getSystemService("package");

        IPackageManager packageManager = IPackageManager.Stub.asInterface(new ShizukuBinderWrapper(systemService));

        IPackageInstaller packageInstaller = packageManager.getPackageInstaller();

        return IPackageInstaller.Stub.asInterface(new ShizukuBinderWrapper(packageInstaller.asBinder()));
    }


    /**
     * 为不同的系统版本 反射构造 PackageInstaller
     */
    public static PackageInstaller createPackageInstaller(Context context, IPackageInstaller installer, String installerPackageName, String installerAttributionTag, int userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PackageInstaller.class.getConstructor(IPackageInstaller.class, String.class, String.class, int.class)
                    .newInstance(installer, installerPackageName, installerAttributionTag, userId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PackageInstaller.class.getConstructor(IPackageInstaller.class, String.class, int.class)
                    .newInstance(installer, installerPackageName, userId);
        } else {
            return PackageInstaller.class.getConstructor(Context.class, PackageManager.class, IPackageInstaller.class, String.class, int.class)
                    .newInstance(context, context.getPackageManager(), installer, installerPackageName, userId);
        }
    }

    public static PackageInstaller.Session createSession(IPackageInstallerSession session) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return PackageInstaller.Session.class.getConstructor(IPackageInstallerSession.class)
                .newInstance(session);

    }

    /**
     * 反射获取 installFlags字段值
     */
    public static int getInstallFlags(PackageInstaller.SessionParams params) throws NoSuchFieldException, IllegalAccessException {
        return (int) PackageInstaller.SessionParams.class.getDeclaredField("installFlags").get(params);
    }
    /**
     * 反射设置 installFlags字段值
     */
    public static void setInstallFlags(PackageInstaller.SessionParams params, int newValue) throws NoSuchFieldException, IllegalAccessException {
        PackageInstaller.SessionParams.class.getDeclaredField("installFlags").set(params, newValue);
    }

    public static abstract class IIntentSenderAdaptor extends IIntentSender.Stub {

        public abstract void send(Intent intent);
        @Override
        public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
            send(intent);
        }

        @Override
        public int send(int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
            send(intent);
            return 0;
        }
    }
}