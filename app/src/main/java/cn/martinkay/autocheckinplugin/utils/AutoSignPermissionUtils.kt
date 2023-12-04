package cn.martinkay.autocheckinplugin.utils

import android.util.Log
import cn.martinkay.autocheckinplugin.SIGN_CALENDAR_SCHEME_CACHE
import cn.martinkay.autocheckinplugin.SIGN_OPEN_INTENT_START_TIME
import cn.martinkay.autocheckinplugin.SharePrefHelper
import cn.martinkay.autocheckinplugin.model.CalendarScheme
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

object AutoSignPermissionUtils {

    val notifyCalendarSchemeEvent: MutableStateFlow<Unit?> = MutableStateFlow(null)

    private val workingDays = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    fun isTodayAutoSignAllowed(): Boolean {
        val calendarSchemeMap = kotlin.runCatching {
            JSON.parseArray(
                SharePrefHelper.getString(SIGN_CALENDAR_SCHEME_CACHE, ""),
                CalendarScheme::class.java
            ).associateBy { it.date }
        }.onFailure { it.printStackTrace() }.getOrNull()
        val calendar = Calendar.getInstance()
        val workDay = workingDays.contains(calendar[Calendar.DAY_OF_WEEK])
        if (calendarSchemeMap == null) {
            return workDay
        }
        val date = com.haibin.calendarview.Calendar().apply {
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH) + 1
            day = calendar.get(Calendar.DAY_OF_MONTH)
        }.toString()
        val value = calendarSchemeMap[date] ?: return workDay
        return value.scheme != CalendarScheme.AUTO_SIGN_DAY_FORBIDDEN
    }

    fun increaseTodayAutoSignCount() {
        val calendarSchemeMap = kotlin.runCatching {
            JSON.parseArray(
                SharePrefHelper.getString(SIGN_CALENDAR_SCHEME_CACHE, ""),
                CalendarScheme::class.java
            ).associateBy { it.date }.toMutableMap()
        }.onFailure { it.printStackTrace() }.getOrElse { mutableMapOf() }
        val calendar = Calendar.getInstance()
        val date = com.haibin.calendarview.Calendar().apply {
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH) + 1
            day = calendar.get(Calendar.DAY_OF_MONTH)
        }.toString()
        val value = calendarSchemeMap[date] ?: CalendarScheme().apply {
            this.date = date
        }
        value.nextScheme()
        calendarSchemeMap[date] = value
        SharePrefHelper.putString(
            SIGN_CALENDAR_SCHEME_CACHE,
            JSON.toJSONString(calendarSchemeMap.map { it.value }.toList())
        )
        GlobalScope.launch { notifyCalendarSchemeEvent.emit(Unit) }
    }

    fun isMobileAutoSignLaunch(): Boolean {
        val startTime = SharePrefHelper.getLong(SIGN_OPEN_INTENT_START_TIME, 0)
        if (System.currentTimeMillis() - startTime > 5000) {
            Log.i("CompleteProcessor", "不是由程序打开的，忽略")
            return false
        }
        return true
    }
}