package cn.martinkay.autocheckinplugin.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import cn.martinkay.autocheckinplugin.IS_FINISH_AFTERNOON_OFF_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_FINISH_AFTERNOON_START_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_FINISH_MORNING_OFF_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_FINISH_MORNING_START_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_OPEN_MORNING_OFF_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_OPEN_MORNING_START_WORK_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_OPEN_SATURDAY_SIGN_TASK
import cn.martinkay.autocheckinplugin.IS_OPEN_SUNDAY_SIGN_TASK
import cn.martinkay.autocheckinplugin.MainActivity
import cn.martinkay.autocheckinplugin.PACKAGE_WECHAT_WORK
import cn.martinkay.autocheckinplugin.SharePrefHelper
import cn.martinkay.autocheckinplugin.SignApplication
import cn.martinkay.autocheckinplugin.formatTime
import cn.martinkay.autocheckinplugin.getAfternoonOffWorkStartTimeStr
import cn.martinkay.autocheckinplugin.getAfternoonOffWorkStopTimeStr
import cn.martinkay.autocheckinplugin.getAfternoonStartWorkStartTimeStr
import cn.martinkay.autocheckinplugin.getAfternoonStartWorkStopTimeStr
import cn.martinkay.autocheckinplugin.getMorningOffWorkStartTimeStr
import cn.martinkay.autocheckinplugin.getMorningStartWorkStartTimeStr
import cn.martinkay.autocheckinplugin.getMorningStartWorkStopTimeStr
import java.util.*

const val TAG: String = "SignService"

class SignService : AccessibilityService() {

    companion object {
        const val ACTION_DO_ALARM_SIGN = "action_do_alarm_sign"

        const val MSG_BACK = 1

        const val SIGN_TEXT = "com.tencent.wework:id/bem"
        const val WORK_SPACE_TEXT = "工作台"
        const val WORK_SIGN_TEXT = "打卡"
        const val STEP_PREPARED = 0
        const val STEP_CLICK_WORKSPACE = 1
        const val STEP_CLICK_SIGN = 2
        const val STEP_CLICK_SIGN_BTN = 3
        const val STEP_ERROR = 4
        const val STEP_BACK_HOME = 5

        const val TIME_OUT = 5000L
        var mStartOpen = false
        var mCurrStep = STEP_PREPARED

        fun goBackMainAct() {
            val context = SignApplication.getApp()
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private val mHandler: Handler = OptionHandler()
    private var isSigningTask = false
    private var mIsStartWorkJob = false
    private var mManualSign = false

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        registerReceiver(timeReceiver, intentFilter)
        Log.e(TAG, " onCreate()")
    }

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                checkToDoSignTask()
            }
        }
    }

    private fun checkToDoSignTask() {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val min = Calendar.getInstance().get(Calendar.MINUTE)
        val isWeekSaturday = SharePrefHelper.getBoolean(IS_OPEN_SATURDAY_SIGN_TASK, false)
        val isWeekSunday = SharePrefHelper.getBoolean(IS_OPEN_SUNDAY_SIGN_TASK, false)

        // 如果勾选了周六或者周日任意一个，都要打卡
        if (day == Calendar.SATURDAY && !isWeekSaturday) {
            Log.e(TAG, " 现在是周六：不打卡")
            return
        }
        if (day == Calendar.SUNDAY && !isWeekSunday) {
            Log.e(TAG, " 现在是周日：不打卡")
            return
        }

        val isMorningOpenStartWork =
            SharePrefHelper.getBoolean(IS_OPEN_MORNING_START_WORK_SIGN_TASK, false)
        val isMorningOpenOffWork =
            SharePrefHelper.getBoolean(IS_OPEN_MORNING_OFF_WORK_SIGN_TASK, false)
        val isAfternoonOpenStartWork =
            SharePrefHelper.getBoolean(IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK, false)
        val isAfternoonOpenOffWork =
            SharePrefHelper.getBoolean(IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK, false)
        Log.e(
            TAG,
            " 现在时间: ${formatTime("$hour:$min")} -> 是否开启自动打卡：${isMorningOpenStartWork || isMorningOpenOffWork || isAfternoonOpenStartWork || isAfternoonOpenOffWork}"
        )
        if (isMorningOpenStartWork || isMorningOpenOffWork || isAfternoonOpenStartWork || isAfternoonOpenOffWork) {
            // 早上上班打卡
            val isMorningStartWork = isMorningStartWorkTime(hour, min)
            // 早上下班打卡
            val isMorningOffWork = isMorningOffWorkTime(hour, min)
            // 下午上班打卡
            val isAfternoonStartWork = isAfternoonStartWorkTime(hour, min)
            // 下午下班打卡
            val isAfternoonOffWork = isAfternoonOffWorkTime(hour, min)

            if (isMorningStartWork || isMorningOffWork || isAfternoonStartWork || isAfternoonOffWork) {
                // 是否结束任务
                var isFinishTask = false
                var logStr = ""
                // 检测是否完成任务
                if (isMorningStartWork) {
                    isFinishTask =
                        SharePrefHelper.getBoolean(IS_FINISH_MORNING_START_WORK_SIGN_TASK, false)
                    logStr = "早上上班打卡"
                } else if (isMorningOffWork) {
                    isFinishTask =
                        SharePrefHelper.getBoolean(IS_FINISH_MORNING_OFF_WORK_SIGN_TASK, false)
                    logStr = "早上下班打卡"
                } else if (isAfternoonStartWork) {
                    isFinishTask =
                        SharePrefHelper.getBoolean(IS_FINISH_AFTERNOON_START_WORK_SIGN_TASK, false)
                    logStr = "下午上班打卡"
                } else if (isAfternoonOffWork) {
                    isFinishTask =
                        SharePrefHelper.getBoolean(IS_FINISH_AFTERNOON_OFF_WORK_SIGN_TASK, false)
                    logStr = "下午下班打卡"
                }
                if (isSigningTask) {
                    logStr += "正在执行，返回"
                    Log.e(TAG, logStr)
                    return
                }
                if (isFinishTask) {
                    logStr += ",已完成"
                    Log.e(TAG, logStr)
                    return
                }
                mIsStartWorkJob = isMorningStartWork || isAfternoonStartWork
                doSignTask()
            } else {
                SharePrefHelper.putBoolean(IS_FINISH_MORNING_START_WORK_SIGN_TASK, false)
                SharePrefHelper.putBoolean(IS_FINISH_MORNING_OFF_WORK_SIGN_TASK, false)
                SharePrefHelper.putBoolean(IS_FINISH_AFTERNOON_START_WORK_SIGN_TASK, false)
                SharePrefHelper.putBoolean(IS_FINISH_AFTERNOON_OFF_WORK_SIGN_TASK, false)
                Log.e(TAG, "现在时间: ${formatTime("$hour:$min")} 不在打卡时间范围内")
            }
        }
    }

    private fun isMorningStartWorkTime(hour: Int, minute: Int): Boolean {
        val startTimeStr = getMorningStartWorkStartTimeStr()
        var startHour = startTimeStr.split(":")[0].toInt()
        val startMinute = startTimeStr.split(":")[1].toInt()
        val stopTimeStr = getMorningStartWorkStopTimeStr()
        var stopHour = stopTimeStr.split(":")[0].toInt()
        val stopMinute = stopTimeStr.split(":")[1].toInt()
        if (hour < startHour || hour > stopHour) {
            return false
        }
        startHour = startHour * 60 + startMinute
        stopHour = stopHour * 60 + stopMinute
        val currHour = hour * 60 + minute
        if (currHour in startHour..stopHour) {
            return true
        }
        return false
    }

    private fun isMorningOffWorkTime(hour: Int, minute: Int): Boolean {
        val startTimeStr = getMorningOffWorkStartTimeStr()
        var startHour = startTimeStr.split(":")[0].toInt()
        val startMinute = startTimeStr.split(":")[1].toInt()
        val stopTimeStr = getMorningStartWorkStopTimeStr()
        var stopHour = stopTimeStr.split(":")[0].toInt()
        val stopMinute = stopTimeStr.split(":")[1].toInt()
        if (hour < startHour || hour > stopHour) {
            return false
        }
        startHour = startHour * 60 + startMinute
        stopHour = stopHour * 60 + stopMinute
        val currHour = hour * 60 + minute
        if (currHour in startHour..stopHour) {
            return true
        }
        return false
    }


    private fun isAfternoonStartWorkTime(hour: Int, minute: Int): Boolean {
        val startTimeStr = getAfternoonStartWorkStartTimeStr()
        var startHour = startTimeStr.split(":")[0].toInt()
        val startMinute = startTimeStr.split(":")[1].toInt()
        val stopTimeStr = getAfternoonStartWorkStopTimeStr()
        var stopHour = stopTimeStr.split(":")[0].toInt()
        val stopMinute = stopTimeStr.split(":")[1].toInt()
        if (hour < startHour || hour > stopHour) {
            return false
        }
        startHour = startHour * 60 + startMinute
        stopHour = stopHour * 60 + stopMinute
        val currHour = hour * 60 + minute
        if (currHour in startHour..stopHour) {
            return true
        }
        return false
    }

    private fun isAfternoonOffWorkTime(hour: Int, minute: Int): Boolean {
        val startTimeStr = getAfternoonOffWorkStartTimeStr()
        var startHour = startTimeStr.split(":")[0].toInt()
        val startMinute = startTimeStr.split(":")[1].toInt()
        val stopTimeStr = getAfternoonOffWorkStopTimeStr()
        var stopHour = stopTimeStr.split(":")[0].toInt()
        val stopMinute = stopTimeStr.split(":")[1].toInt()
        if (hour < startHour || hour > stopHour) {
            return false
        }
        startHour = startHour * 60 + startMinute
        stopHour = stopHour * 60 + stopMinute
        val currHour = hour * 60 + minute
        if (currHour in startHour..stopHour) {
            return true
        }
        return false
    }


    override fun onInterrupt() {

    }

    fun startSign() {
        mCurrStep = STEP_CLICK_WORKSPACE
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.packageName == PACKAGE_WECHAT_WORK) {
                val eventType = event.eventType
                Log.e(
                    TAG,
                    "onAccessibilityEvent -> isSigningTask：$isSigningTask & eventType = $eventType"
                )
//                if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//                        || eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
                if (!isSigningTask)
                    return
                val sourceEvent = event.source
                sourceEvent?.let {
                    when (mCurrStep) {
                        STEP_CLICK_WORKSPACE -> {
                            val msg = mHandler.obtainMessage()
                            msg.obj = it
                            mHandler.sendMessage(msg)
                        }

                        STEP_CLICK_SIGN -> {
                            val msg = mHandler.obtainMessage()
                            msg.obj = it
                            mHandler.sendMessage(msg)
                        }

                        STEP_CLICK_SIGN_BTN -> {
                            val msg = mHandler.obtainMessage()
                            msg.obj = it
                            mHandler.sendMessageDelayed(msg, 2000L)
                        }

                        else -> {
                        }
                    }
                }
//                }
            }
        }
    }

    private fun findSpecialView(nodeInfo: AccessibilityNodeInfo, text: String): Boolean {
        var views = nodeInfo.findAccessibilityNodeInfosByText(text)
        if (views == null || views.isEmpty()) {
            views = nodeInfo.findAccessibilityNodeInfosByViewId(text)
        }
        if (views != null && !views.isEmpty()) {
            val node = views[0]
            if (node.isClickable) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else if (null != node.parent) {
                return node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
        return false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e(TAG, " onServiceConnected()")
        if (mStartOpen) {
            mStartOpen = false
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (ACTION_DO_ALARM_SIGN) {
            action -> {
                mManualSign = true
                doSignTask()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val timeOutRunnable = Runnable {
        mCurrStep = STEP_ERROR
        mHandler.removeCallbacksAndMessages(null)
        Log.e(TAG, "执行任务失败")
        Toast.makeText(this, "打卡失败!!!", Toast.LENGTH_LONG).show()
        mCurrStep = STEP_BACK_HOME
        mHandler.sendEmptyMessage(MSG_BACK)
    }

    private fun doSignTask() {
        if (isSigningTask)
            return
        Log.e(TAG, "开始执行任务")
        isSigningTask = true
        gotoWeWork()
        startSign()
    }

    private fun gotoWeWork() {
        val intent = packageManager.getLaunchIntentForPackage(PACKAGE_WECHAT_WORK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent?.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startActivity(intent)
    }

    open inner class OptionHandler : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            val obj = msg.obj
            if (what == MSG_BACK) {
                if (mCurrStep == STEP_BACK_HOME) {
                    Log.e(TAG, "返回")
                    isSigningTask = false
                    mManualSign = false
                    mCurrStep = STEP_PREPARED
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    postDelayed({
                        removeCallbacksAndMessages(null)
//                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                        goBackMainAct()
                        Log.e(TAG, "回到桌面")
                    }, 1500L)
                    return
                }
            }
            obj?.let { iit ->
                if (iit !is AccessibilityNodeInfo)
                    return@let
                when (mCurrStep) {
                    STEP_CLICK_WORKSPACE -> {
                        removeCallbacks(timeOutRunnable)
                        if (findSpecialView(iit, WORK_SPACE_TEXT)) {
                            mCurrStep = STEP_CLICK_SIGN
                            Log.e(TAG, "找到工作台按钮")
                        } else {
                            postDelayed(timeOutRunnable, TIME_OUT)
                            Log.e(TAG, "没找到工作台按钮")
                        }
                    }

                    STEP_CLICK_SIGN -> {
                        removeCallbacks(timeOutRunnable)
                        if (findSpecialView(iit, WORK_SIGN_TEXT)) {
                            mCurrStep = STEP_CLICK_SIGN_BTN
                            Log.e(TAG, "找到工作台的打卡按钮")
                        } else {
                            Log.e(TAG, "没找到工作台的打卡按钮")
                            postDelayed(timeOutRunnable, TIME_OUT)
                        }
                    }

                    STEP_CLICK_SIGN_BTN -> {
                        removeCallbacks(timeOutRunnable)


                        if (findSpecialView(iit, SIGN_TEXT)) {
                            Log.e(TAG, "找到打卡圆圈")
                        } else {
                            Log.e(TAG, "没找到打卡圆圈")
                        }
                        Toast.makeText(SignApplication.getApp(), "打卡成功!!!", Toast.LENGTH_LONG)
                            .show()
                        if (!mManualSign) {
                            if (mIsStartWorkJob) {
                                SharePrefHelper.putBoolean(
                                    IS_FINISH_MORNING_START_WORK_SIGN_TASK,
                                    true
                                )
                                SharePrefHelper.putBoolean(
                                    IS_FINISH_AFTERNOON_START_WORK_SIGN_TASK,
                                    true
                                )
                            } else {
                                SharePrefHelper.putBoolean(
                                    IS_FINISH_MORNING_OFF_WORK_SIGN_TASK,
                                    true
                                )
                                SharePrefHelper.putBoolean(
                                    IS_FINISH_AFTERNOON_OFF_WORK_SIGN_TASK,
                                    true
                                )
                            }
                        }
                        mCurrStep = STEP_BACK_HOME
                        sendEmptyMessageDelayed(MSG_BACK, 5000L)
//
//                        if (findSpecialView(iit, SIGN_TEXT)) {
//                            mCurrStep = STEP_PREPARED
//                            Log.e(TAG, "找到定位界面的打卡按钮")
//                            Toast.makeText(SignApplication.getApp(), "打卡成功!!!", Toast.LENGTH_LONG).show()
//
//                            mCurrStep = STEP_BACK_HOME
//                            if (mIsStartWorkJob) {
//                                SharePrefHelper.putBoolean(IS_FINISH_START_WORK_SIGN_TASK, true)
//                            } else {
//                                SharePrefHelper.putBoolean(IS_FINISH_OFF_WORK_SIGN_TASK, true)
//                            }
//                            mHandler.sendEmptyMessageDelayed(MSG_BACK, 4000L)
//                        } else {
//                            postDelayed(timeOutRunnable, TIME_OUT)
//                        }
                    }

                    else -> {
                        mCurrStep = STEP_PREPARED
                    }
                }
            }
        }
    }
}