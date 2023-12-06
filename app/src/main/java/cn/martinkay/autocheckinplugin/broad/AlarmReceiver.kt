package cn.martinkay.autocheckinplugin.broad

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import cn.martinkay.autocheckinplugin.MainActivity
import cn.martinkay.autocheckinplugin.SIGN_OPEN_INTENT_START_TIME
import cn.martinkay.autocheckinplugin.SharePrefHelper
import cn.martinkay.autocheckinplugin.constant.Constant
import cn.martinkay.autocheckinplugin.util.ShellUtils
import cn.martinkay.autocheckinplugin.utils.AlarManagerUtil
import cn.martinkay.autocheckinplugin.utils.AutoSignPermissionUtils
import cn.martinkay.autocheckinplugin.utils.HShizuku.execute
import cn.martinkay.autocheckinplugin.utils.HShizuku.isEnable
import cn.martinkay.autocheckinplugin.utils.HShizuku.lockScreen
import com.topjohnwu.superuser.Shell

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("ContentValues", "接收闹钟事件")
        try {
            // 1. 初始化信息，例如ROOT权限
            initEnv(context)
            val bundleExtra = intent.getBundleExtra("timer") ?: return
            val hour = bundleExtra.getInt("hour")
            val minute = bundleExtra.getInt("minute")
            val requestCode = bundleExtra.getInt("requestCode")
            wakeUpAndUnlock(context)
            // 2. 先启动自己到主界面
            val intent2 = Intent(context, MainActivity::class.java)
            intent2.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent2)
            // 3. 打开企业微信
            val autoSignAllowed = AutoSignPermissionUtils.isTodayAutoSignAllowed()
            if (autoSignAllowed) {
                Log.i("ContentValues", "今天是要打卡的星期")
                val launchIntentForPackage = context.packageManager.getLaunchIntentForPackage(
                    Constant.getActiveApp().packageName
                )
                if (launchIntentForPackage != null) {
                    SharePrefHelper.putLong(
                        SIGN_OPEN_INTENT_START_TIME, System.currentTimeMillis()
                    )
                    Log.i("ContentValues", "启动打卡程序 by launchIntentForPackage")
                    context.startActivity(launchIntentForPackage)
                } else {
                    val componentIntent = Intent("android.intent.action.MAIN").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        component = ComponentName(
                            Constant.getActiveApp().packageName,
                            Constant.getActiveApp().activityName
                        )
                    }
                    SharePrefHelper.putLong(
                        SIGN_OPEN_INTENT_START_TIME, System.currentTimeMillis()
                    )
                    Log.i(
                        "ContentValues",
                        "启动打卡程序 by openAppByPackageName ${Constant.getActiveApp().packageName},${Constant.getActiveApp().activityName}"
                    )
                    context.startActivity(componentIntent)
                }
            } else {
                Log.i("ContentValues", "今天不是要打卡的星期")
            }
            when (requestCode) {
                0 -> {
                    Log.i("ContentValues", "重新注册上午上班打卡闹钟")
                    AlarManagerUtil.timedTackMonWork(context, hour, minute, requestCode)
                }

                1 -> {
                    Log.i("ContentValues", "重新注册上午下班打卡闹钟")
                    AlarManagerUtil.timedTackMonOffWork(context, hour, minute, requestCode)
                }

                2 -> {
                    Log.i("ContentValues", "重新注册下午上班打卡闹钟")
                    AlarManagerUtil.timedTackAfWork(context, hour, minute, requestCode)
                }

                3 -> {
                    Log.i("ContentValues", "重新注册下午下班打卡闹钟")
                    AlarManagerUtil.timedTackAfOffWork(context, hour, minute, requestCode)
                }
            }
            Log.e("ContentValues", "定时成功！")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ContentValues", "定时失败！")
        }
    }

    companion object {
        fun wakeUpAndUnlock(context: Context) {
            if (Constant.isRoot) {
                if (Constant.isRoot) {
                    try {
                        if (Shell.su("input keyevent 26").exec().isSuccess) {
                            Log.i("AlarmReceiver", "ROOT shell亮屏成功")
                        }
                        if (Shell.su("input swipe 300 1000 300 500").exec().isSuccess) {
                            Log.i("AlarmReceiver", "ROOT shell向上滑动解锁成功")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else if (Constant.isShizuku) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val lockScreen = lockScreen
                    if (lockScreen) {
                        Log.i("AlarmReceiver", "Shizuku ibinder亮屏成功")
                    } else {
                        Log.i("AlarmReceiver", "Shizuku ibinder亮屏失败")
                    }
                    val (first) = execute("input swipe 300 1000 300 500", Constant.isRoot)
                    if (first == 0) {
                        Log.i("AlarmReceiver", "Shizuku ibinder向上滑动解锁成功")
                    } else {
                        Log.i("AlarmReceiver", "Shizuku ibinder向上滑动解锁失败")
                    }
                }
            } else {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "autocheckinplugin:WakeLockTag"
                )
                wakeLock.acquire()
                (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).newKeyguardLock(
                    "unLock"
                ).disableKeyguard()
            }
        }

        fun initEnv(context: Context) {
            val isRoot = isRoot
            Constant.isRoot = isRoot == 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val isShizuku = isEnable(context)
                Constant.isShizuku = isShizuku
            }
            if (Constant.isRoot) {
                Log.i("AlarmReceiver", "基于ROOT开启辅助功能")
                // 如果有ROOT并且没有开启辅助功能，就基于ROOT开启辅助功能
                if (Constant.isRoot && !isAccessibility) {
                    enableAccessibility()
                }
            } else {
                Log.i("AlarmReceiver", "基于Shizuku开启辅助功能")
                // 如果有Shizuku并且没有开启辅助功能，就基于Shizuku开启辅助功能
                if (Constant.isShizuku && !isAccessibilityByShizuku(Constant.isRoot)) {
                    enableAccessibilityByShizuku(Constant.isRoot)
                }
            }
        }

        val isRoot: Int
            get() {
                try {
                    val cmds: MutableList<String> = ArrayList()
                    cmds.add("ls /data/data")
                    val result = ShellUtils.execCommand(cmds, true, true)
                    return result.result
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return -1
            }
        val isAccessibility: Boolean
            get() {
                val cmds: MutableList<String> = ArrayList()
                cmds.add("settings get secure enabled_accessibility_services")
                val result = ShellUtils.execCommand(cmds, true, true)
                // 注意：如果开启了多个辅助功能，这里的successMsg会有多个，所以不能用equals，而是用contains
                if ("cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService".contains(
                        result.successMsg
                    )
                ) {
                    val cmds2: MutableList<String> = ArrayList()
                    cmds2.add("settings get secure accessibility_enabled")
                    val result2 = ShellUtils.execCommand(cmds2, true, true)
                    if (result2.result == 0 && "1" == result2.successMsg) {
                        return true
                    }
                }
                return false
            }

        fun isAccessibilityByShizuku(isRoot: Boolean): Boolean {
            val (first, second) = execute(
                "settings get secure enabled_accessibility_services", isRoot
            )
            // 注意：如果开启了多个辅助功能，这里的successMsg会有多个，所以不能用equals，而是用contains
            Log.i(
                "AlarmReceiver", "isAccessibilityByShizuku: $first--$second"
            )
            if ("cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService".contains(
                    second
                )
            ) {
                val (first1, second1) = execute("settings get secure accessibility_enabled", isRoot)
                if (first1 == 0 && "1" == second1) {
                    return true
                }
            }
            return false
        }

        fun enableAccessibility(): Int {
            val cmds: MutableList<String> = ArrayList()
            cmds.add(
                "settings put secure enabled_accessibility_services cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService\n"
            )
            cmds.add("settings put secure accessibility_enabled 1\n")
            val result = ShellUtils.execCommand(cmds, true, true)
            return result.result
        }

        fun enableAccessibilityByShizuku(isRoot: Boolean): Int {
            val (first, second) = execute(
                "settings put secure enabled_accessibility_services cn.martinkay.autocheckinplugin/cn.martinkay.autocheckinplugin.service.MyAccessibilityService\n",
                true
            )
            val (first1, second1) = execute("settings put secure accessibility_enabled 1\n", true)
            Log.i(
                "AlarmReceiver",
                "enableAccessibilityByShizuku: " + first + "--" + second + "--" + first1 + "--" + second1
            )
            return first1
        }
    }
}