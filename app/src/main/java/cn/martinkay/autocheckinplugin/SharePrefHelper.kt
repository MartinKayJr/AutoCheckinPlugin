package cn.martinkay.autocheckinplugin

import android.content.Context
import android.content.SharedPreferences

const val IS_OPEN_START_WORK_SIGN_TASK = "is_open_start_work_sign_task"
const val IS_OPEN_STOP_WORK_SIGN_TASK = "is_open_stop_work_sign_task"
const val IS_OPEN_WEEKEND_SIGN_TASK = "is_open_weekend_sign_task"

const val SIGN_TASK_START_WORK_START_TIME = "sign_task_start_work_start_time"
const val SIGN_TASK_START_WORK_STOP_TIME = "sign_task_start_work_stop_time"
const val SIGN_TASK_STOP_WORK_START_TIME = "sign_task_stop_work_start_time"
const val SIGN_TASK_STOP_WORK_STOP_TIME = "sign_task_stop_work_stop_time"

const val IS_FINISH_START_WORK_SIGN_TASK = "is_finish_start_work_sign_task"
const val IS_FINISH_OFF_WORK_SIGN_TASK = "is_finish_off_work_sign_task"

object SharePrefHelper {
    private var mShare: SharedPreferences? = null

    init {
        getSharePref()
    }

    private fun initSharePref(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(ctx.packageName, Context.MODE_PRIVATE)
    }

    fun getSharePref(): SharedPreferences {
        if (null == mShare) {
            mShare = initSharePref(SignApplication.getApp())
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

    fun putBoolean(key: String, value: Boolean = false) {
        val editor = getSharePref().edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return getSharePref().getBoolean(key, default)
    }
}

fun getStartWorkStartTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_START_WORK_START_TIME, "8:00")
}

fun getStartWorkStopTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_START_WORK_STOP_TIME, "11:00")
}

fun getOffWorkStartTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_STOP_WORK_START_TIME, "17:00")
}

fun getOffWorkStopTimeStr(): String {
    return SharePrefHelper.getString(SIGN_TASK_STOP_WORK_STOP_TIME, "20:00")
}