package cn.martinkay.autocheckinplugin

import android.content.Context
import android.content.SharedPreferences

const val IS_ENABLE_AUTO_SIGN = "is_enable_auto_sign"

const val IS_ENABLE_TIME_JITTER = "is_enable_time_jitter"

const val TIME_JITTER_VALUE = "time_jitter_value"

const val IS_OPEN_MORNING_START_WORK_SIGN_TASK = "is_open_morning_start_work_sign_task"
const val IS_OPEN_MORNING_OFF_WORK_SIGN_TASK = "is_open_morning_off_work_sign_task"

const val IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK = "is_open_afternoon_start_work_sign_task"
const val IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK = "is_open_afternoon_off_work_sign_task"

const val IS_OPEN_SATURDAY_SIGN_TASK = "is_open_saturday_sign_task"
const val IS_OPEN_SUNDAY_SIGN_TASK = "is_open_sunday_sign_task"

const val SIGN_TASK_MORNING_START_WORK_START_TIME = "sign_task_morning_start_work_start_time"

const val SIGN_TASK_MORNING_OFF_WORK_START_TIME = "sign_task_morning_off_work_start_time"

const val SIGN_TASK_AFTERNOON_START_WORK_START_TIME = "sign_task_start_work_start_time"

const val SIGN_TASK_AFTERNOON_OFF_WORK_START_TIME = "sign_task_stop_work_start_time"

const val SIGN_OPEN_INTENT_START_TIME = "sign_open_intent_start_time"

const val SIGN_CALENDAR_SCHEME_CACHE = "sign_calendar_scheme_cache"

const val VERSION_UPDATE_FLAG = "version_update_flag"

const val ENABLE_SMART_RECOGNITION_JUMP = "enable_smart_recognition_jump"

const val ENABLE_START_QUICK_SIGN = "enable_start_quick_sign"

object SharePrefHelper {

    private var mShare: SharedPreferences? = null

    init {
        getSharePref()
        if (!getBoolean(VERSION_UPDATE_FLAG, false)) {
            // 抖动变为long类型，版本更新清除旧存储值，否则会崩溃
            remove(TIME_JITTER_VALUE)
            putBoolean(VERSION_UPDATE_FLAG, true)
        }
    }

    private fun initSharePref(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(ctx.packageName, Context.MODE_PRIVATE)
    }

    fun getSharePref(): SharedPreferences {
        if (null == mShare) {
            mShare = initSharePref(SignApplication.getInstance())
        }
        return mShare!!
    }

    fun putString(key: String, value: String?) {
        val editor = getSharePref().edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, default: String?): String {
        return getSharePref().getString(key, default).toString()
    }

    fun putLong(key: String, value: Long) {
        val editor = getSharePref().edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String, default: Long): Long {
        return getSharePref().getLong(key, default)
    }

    fun putBoolean(key: String, value: Boolean = false) {
        val editor = getSharePref().edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return getSharePref().getBoolean(key, default)
    }

    fun remove(key: String) {
        getSharePref().edit().remove(key).apply()
    }
}

// 获取 早上上班的开始时间范围
fun getMorningStartWorkStartTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_MORNING_START_WORK_START_TIME, "8:50")
}

// 获取 早上下班的开始时间范围
fun getMorningOffWorkStartTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_MORNING_OFF_WORK_START_TIME, "12:00")
}

/**
 * 获取 下午下班的开始时间范围
 */
fun getAfternoonStartWorkStartTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_AFTERNOON_START_WORK_START_TIME, "12:50")
}

/**
 * 获取 下午下班的开始时间范围
 */
fun getAfternoonOffWorkStartTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_AFTERNOON_OFF_WORK_START_TIME, "18:00")
}