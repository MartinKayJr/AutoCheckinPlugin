package cn.martinkay.autocheckinplugin.utils;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.RequiresApi;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import rikka.shizuku.Shizuku;

public class Exec {
    Process p;
    Thread h1, h2, h3;
    boolean br = false;

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void closeApp(String packageName) {
        // int userId = UserHandle.USER_ALL;
        int userId = -1;
        try {
            HiddenApiBypass.invoke(ActivityManager.class, "forceStopPackage", packageName, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public void ShizukuExec(String cmd) {
//        try {
//
//            //记录执行开始的时间
//            long time = System.currentTimeMillis();
//
//            //使用Shizuku执行命令
//            p = Shizuku.newProcess(new String[]{"sh"}, null, null);
//            OutputStream out = p.getOutputStream();
//            out.write((cmd + "\nexit\n").getBytes());
//            out.flush();
//            out.close();
//
//            //开启新线程，实时读取命令输出
//            h2 = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                        String inline;
//                        while ((inline = mReader.readLine()) != null) {
//
//                            //如果TextView的字符太多了（会使得软件非常卡顿），或者用户退出了执行界面（br为true），则停止读取
//                            if (br) break;
//                            System.out.println(inline.equals("") ? "\n" : inline + "\n");
//                        }
//                        mReader.close();
//                    } catch (Exception ignored) {
//                    }
//                }
//            });
//            h2.start();
//
//            //开启新线程，实时读取命令报错信息
//            h3 = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//                        String inline;
//                        while ((inline = mReader.readLine()) != null) {
//
//                            //如果TextView的字符太多了（会使得软件非常卡顿），或者用户退出了执行界面（br为true），则停止读取
//                            if (br) break;
//
//                            SpannableString ss = new SpannableString(inline+"\n");
//                            ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            System.out.println(ss);
//
//                        }
//                        mReader.close();
//                    } catch (Exception ignored) {
//                    }
//                }
//            });
//            h3.start();
//
//            //等待命令运行完毕
//            p.waitFor();
//
//            //获取命令返回值
//            String exitValue = String.valueOf(p.exitValue());
//
//            //显示命令返回值和命令执行时长
//            System.out.println(String.format("返回值：%s\n执行用时：%.2f秒", exitValue, (System.currentTimeMillis() - time) / 1000f));
//            System.out.println("执行完毕");
//        } catch (Exception ignored) {
//        }
//    }
}
