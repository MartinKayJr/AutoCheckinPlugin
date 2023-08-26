package cn.martinkay.autocheckinplugin

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.martinkay.autocheckinplugin.constant.Constant
import cn.martinkay.autocheckinplugin.service.BackgroundAccess
import cn.martinkay.autocheckinplugin.util.ShellUtils
import cn.martinkay.autocheckinplugin.utils.AlarManagerUtil
import cn.martinkay.autocheckinplugin.utils.IsServiceRunningUtil
import cn.martinkay.autocheckinplugin.utils.JumpPermissionManagement
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.haibin.calendarview.CalendarView.OnCalendarInterceptListener
import com.haibin.calendarview.CalendarView.OnCalendarSelectListener


const val PACKAGE_WECHAT_WORK = "com.tencent.wework"

class MainActivity : AppCompatActivity() {

    private var isEnableAutoSign = false

    private lateinit var mEnableAutoSignSwitch: CheckBox

    private var accessblity = false

    /**
     * 范围
     */
    // 早上上班开始时间
    private lateinit var morningWorkStartTimeTv: TextView

    /**
     * 范围
     */
    // 早上下班开始时间
    private lateinit var morningOffWorkStartTimeTv: TextView

    /**
     * 范围
     */
    // 下午上班开始时间
    private lateinit var afternoonWorkStartTimeTv: TextView


    /**
     * 范围
     */
    // 下午下班结束时间
    private lateinit var afternoonOffWorkStartTimeTv: TextView


    private lateinit var mMorningStartWorkSwitch: CheckBox
    private lateinit var mMorningOffWorkSwitch: CheckBox

    private lateinit var mAfternoonStartWorkSwitch: CheckBox
    private lateinit var mAfternoonOffWorkSwitch: CheckBox


    private lateinit var accessbilitySwitch: CheckBox
    private lateinit var canBackgroundSwitch: CheckBox

    private lateinit var enableRootSwitch: CheckBox

    private lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

        isOpenService()
        isCanBackground()
        initSetting()
    }

    override fun onResume() {
        isOpenService()
        isCanBackground()
        super.onResume()
    }

    private fun initSetting() {
        if (this.accessblity) {
            SignApplication.getInstance().setFlag(true)
        } else {
            Toast.makeText(this, "请打开无障碍服务", Toast.LENGTH_SHORT).show()
//            this.cpdailySwitch.setChecked(false)
//            this.isEnableAutoSign = false
        }

    }

    private fun isOpenService() {
        if (!IsServiceRunningUtil.isAccessibilitySettingsOn(
                this,
                "cn.martinkay.autocheckinplugin.service.MyAccessibilityService"
            )
        ) {
            this.accessbilitySwitch.isChecked = false
            this.accessblity = false
            return
        }
        this.accessbilitySwitch.isChecked = true
        this.accessblity = true
    }


    private fun isCanBackground() {
        if (BackgroundAccess.canBackgroundStart(this)) {
            this.canBackgroundSwitch.isChecked = true
            return
        }
        this.canBackgroundSwitch.isChecked = false
        Toast.makeText(this, "请打开后台弹出界面权限", Toast.LENGTH_SHORT).show()
    }


    private fun initViews() {
        mEnableAutoSignSwitch = findViewById(R.id.enable_auto_sign)
        // 早上上班 时间范围
        morningWorkStartTimeTv = findViewById(R.id.morning_work_start_time_tv)
        // 早上下班 时间范围
        morningOffWorkStartTimeTv = findViewById(R.id.morning_offwork_start_time_tv)
        // 下午上班 时间范围
        afternoonWorkStartTimeTv = findViewById(R.id.afternoon_work_start_time_tv)
        // 下午下班 时间范围
        afternoonOffWorkStartTimeTv = findViewById(R.id.afternoon_offwork_start_time_tv)

        // 早上开始上班 switch
        mMorningStartWorkSwitch = findViewById(R.id.morning_start_work_cb)
        // 早上结束上班 switch
        mMorningOffWorkSwitch = findViewById(R.id.morning_off_work_cb)
        // 下午开始上班 switch
        mAfternoonStartWorkSwitch = findViewById(R.id.afternoon_start_work_cb)
        // 下午结束上班switch
        mAfternoonOffWorkSwitch = findViewById(R.id.afternoon_off_work_cb)

        val morningStartWorkStartTimeStr = getMorningStartWorkStartTimeStr()

        val morningOffWorkStartTimeStr = getMorningOffWorkStartTimeStr()


        val afternoonStartWorkOffStartTimeStr = getAfternoonStartWorkStartTimeStr()

        val afternoonOffWorkOffStartTimeStr = getAfternoonOffWorkStartTimeStr()

        // 早上上班打卡
        isEnableAutoSign = SharePrefHelper.getBoolean(IS_ENABLE_AUTO_SIGN, false)
        mEnableAutoSignSwitch.isChecked = isEnableAutoSign

        accessbilitySwitch = findViewById(R.id.accessbility_switch)
        canBackgroundSwitch = findViewById(R.id.can_background_switch)

        enableRootSwitch = findViewById(R.id.enable_root_switch)

        calendarView = findViewById(R.id.calendarView)


        // 早上上班打卡
        val isMorningStartOpen =
            SharePrefHelper.getBoolean(IS_OPEN_MORNING_START_WORK_SIGN_TASK, false)
        // 早上下班打卡
        val isMorningOffOpen = SharePrefHelper.getBoolean(IS_OPEN_MORNING_OFF_WORK_SIGN_TASK, false)

        // 下午上班打卡
        val isAfternoonStartOpen =
            SharePrefHelper.getBoolean(IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK, false)
        // 下午下班打卡
        val isAfternoonOffOpen =
            SharePrefHelper.getBoolean(IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK, false)
        // 周六打卡
        val isWeekSaturdayOpen = SharePrefHelper.getBoolean(IS_OPEN_SATURDAY_SIGN_TASK, false)
        // 周日打卡
        val isWeekSundayOpen = SharePrefHelper.getBoolean(IS_OPEN_SATURDAY_SIGN_TASK, false)


        morningWorkStartTimeTv.text = formatTime(morningStartWorkStartTimeStr)
        morningOffWorkStartTimeTv.text = formatTime(morningOffWorkStartTimeStr)

        afternoonWorkStartTimeTv.text = formatTime(afternoonStartWorkOffStartTimeStr)
        afternoonOffWorkStartTimeTv.text = formatTime(afternoonOffWorkOffStartTimeStr)


        // 早上初始化
        mMorningStartWorkSwitch.isChecked = isMorningStartOpen
        mMorningOffWorkSwitch.isChecked = isMorningOffOpen

        // 下午初始化
        mAfternoonStartWorkSwitch.isChecked = isAfternoonStartOpen
        mAfternoonOffWorkSwitch.isChecked = isAfternoonOffOpen


        val cbCheckChange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {

                // 开启ROOT
                R.id.enable_root_switch -> {
                    if (buttonView.isPressed) {
                        val cmds = ArrayList<String>()
                        cmds.add("ls /data/data")
                        val result: ShellUtils.CommandResult =
                            ShellUtils.execCommand(cmds, true, true)
                        if (result.result == 0) {
                            Constant.isRoot = true
                            enableRootSwitch.isChecked = true
                            Toast.makeText(this, "ROOT权限已开启", Toast.LENGTH_SHORT).show()
                        } else {
                            Constant.isRoot = false
                            enableRootSwitch.isChecked = false
                            Toast.makeText(this, "ROOT权限未开启", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                R.id.accessbility_switch -> {
                    if (buttonView.isPressed) {
                        if (this.accessblity) {
                            gotoAccessibilityAct()
                        } else {
                            gotoAccessibilityAct()
                        }
                    }
                }

                R.id.can_background_switch -> {
                    if (buttonView.isPressed) {
                        JumpPermissionManagement.GoToSetting(this)
                    }
                }

                // 开启自动签到
                R.id.enable_auto_sign -> {
                    isEnableAutoSign = isChecked;
                    SharePrefHelper.putBoolean(IS_ENABLE_AUTO_SIGN, isChecked)
                    if (isChecked) {
                        if (this.accessblity) {
                            SignApplication.getInstance().setFlag(true)
                            // 早上上班打卡
                            // morningStartWorkStartTimeStr分割为小时和分钟
                            val morningStartWorkStartTimeStr = getMorningStartWorkStartTimeStr()
                            val morningStartWorkStartTimeStrArr =
                                morningStartWorkStartTimeStr.split(":")
                            AlarManagerUtil.timedTackMonWork(
                                this,
                                Integer.valueOf(morningStartWorkStartTimeStrArr[0]),
                                Integer.valueOf(morningStartWorkStartTimeStrArr[1]),
                                0
                            )

                            // 早上下班打卡
                            val morningOffWorkStartTimeStr = getMorningOffWorkStartTimeStr()
                            val morningOffWorkStartTimeStrArr =
                                morningOffWorkStartTimeStr.split(":")
                            AlarManagerUtil.timedTackMonOffWork(
                                this,
                                Integer.valueOf(morningOffWorkStartTimeStrArr[0]),
                                Integer.valueOf(morningOffWorkStartTimeStrArr[1]),
                                1
                            )

                            // 下午上班打卡
                            val afternoonStartWorkOffStartTimeStr =
                                getAfternoonStartWorkStartTimeStr()
                            val afternoonStartWorkOffStartTimeStrArr =
                                afternoonStartWorkOffStartTimeStr.split(":")
                            AlarManagerUtil.timedTackAfWork(
                                this,
                                Integer.valueOf(afternoonStartWorkOffStartTimeStrArr[0]),
                                Integer.valueOf(afternoonStartWorkOffStartTimeStrArr[1]),
                                2
                            )

                            // 下午下班打卡
                            val afternoonOffWorkOffStartTimeStr = getAfternoonOffWorkStartTimeStr()
                            val afternoonOffWorkOffStartTimeStrArr =
                                afternoonOffWorkOffStartTimeStr.split(":")
                            AlarManagerUtil.timedTackAfOffWork(
                                this,
                                Integer.valueOf(afternoonOffWorkOffStartTimeStrArr[0]),
                                Integer.valueOf(afternoonOffWorkOffStartTimeStrArr[1]),
                                3
                            )

                            Toast.makeText(
                                this,
                                "已开启自动打卡",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // 未开启辅助功能
                            Toast.makeText(
                                this,
                                "请开启辅助功能",
                                Toast.LENGTH_SHORT
                            ).show()
                            this.mEnableAutoSignSwitch.isChecked = false
                            this.isEnableAutoSign = false
                        }
                    } else {
                        AlarManagerUtil.cancelTimetacker(this, true)
                        SignApplication.getInstance().setFlag(false)
                    }
                }

                R.id.morning_start_work_cb -> {
                    SharePrefHelper.putBoolean(IS_OPEN_MORNING_START_WORK_SIGN_TASK, isChecked)
                }

                R.id.morning_off_work_cb -> {
                    SharePrefHelper.putBoolean(IS_OPEN_MORNING_OFF_WORK_SIGN_TASK, isChecked)
                }

                R.id.afternoon_start_work_cb -> {
                    SharePrefHelper.putBoolean(IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK, isChecked)
                }

                R.id.afternoon_off_work_cb -> {
                    SharePrefHelper.putBoolean(IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK, isChecked)
                }
            }
        }
        // 修改事件
        mMorningStartWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mMorningOffWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mAfternoonStartWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mAfternoonOffWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mEnableAutoSignSwitch.setOnCheckedChangeListener(cbCheckChange)
        accessbilitySwitch.setOnCheckedChangeListener(cbCheckChange)
        canBackgroundSwitch.setOnCheckedChangeListener(cbCheckChange)

        enableRootSwitch.setOnCheckedChangeListener(cbCheckChange)

        calendarView.setOnCalendarMultiSelectListener(object :
            CalendarView.OnCalendarMultiSelectListener {
            override fun onCalendarMultiSelectOutOfRange(calendar: Calendar?) {

            }

            override fun onMultiSelectOutOfSize(calendar: Calendar?, maxSize: Int) {

            }

            override fun onCalendarMultiSelect(calendar: Calendar?, curSize: Int, maxSize: Int) {
                calendar?.scheme = "25"
            }

        })
    }

    fun onClick(v: View) {
        when (v.id) {

            R.id.start_sign -> {
                gotoWeWork()
            }

            R.id.morning_work_start_time_tv -> {
                val startTimeStr = getMorningStartWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(true, true, startHour, startMinute)
            }

            R.id.morning_offwork_start_time_tv -> {
                val startTimeStr = getMorningOffWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(true, true, startHour, startMinute)
            }

            R.id.afternoon_work_start_time_tv -> {
                val startTimeStr = getAfternoonStartWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(true, false, startHour, startMinute)
            }

            R.id.afternoon_offwork_start_time_tv -> {
                val startTimeStr = getAfternoonOffWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(true, false, startHour, startMinute)
            }

        }
    }

    private fun gotoAccessibilityAct() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        closeDateTimePicker()
    }

    private fun gotoWeWork() {
        val intent = packageManager.getLaunchIntentForPackage(PACKAGE_WECHAT_WORK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    private var mTimePickerDialog: TimePickerDialog? = null

    private fun showMorningDateTimePicker(
        isStart: Boolean,
        isStartWork: Boolean,
        hour: Int,
        min: Int
    ) {
        closeDateTimePicker()
        mTimePickerDialog =
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                closeDateTimePicker()
                val timeStr = formatTime("$hourOfDay:$minute")
                if (isStartWork) {
                    if (isStart) {
                        SharePrefHelper.putString(SIGN_TASK_MORNING_START_WORK_START_TIME, timeStr)
                        Toast.makeText(this, "早上上班打卡开始时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                        morningWorkStartTimeTv.text = "$timeStr"
                    }
                } else {
                    if (isStart) {
                        SharePrefHelper.putString(SIGN_TASK_MORNING_OFF_WORK_START_TIME, timeStr)
                        Toast.makeText(this, "早上下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                        morningOffWorkStartTimeTv.text = "$timeStr"
                    }
                }
                changeTimeAfter(view, hourOfDay, minute)
            }, hour, min, true)
        mTimePickerDialog?.let {
            it.show()
        }
    }

    private fun showAfternoonDateTimePicker(
        isStart: Boolean,
        isStartWork: Boolean,
        hour: Int,
        min: Int
    ) {
        closeDateTimePicker()
        mTimePickerDialog =
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                closeDateTimePicker()
                val timeStr = formatTime("$hourOfDay:$minute")
                if (isStartWork) {
                    if (isStart) {
                        SharePrefHelper.putString(
                            SIGN_TASK_AFTERNOON_START_WORK_START_TIME,
                            timeStr
                        )
                        Toast.makeText(this, "下午上班打卡开始时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                        morningWorkStartTimeTv.text = "$timeStr"
                    }
                } else {
                    if (isStart) {
                        SharePrefHelper.putString(SIGN_TASK_AFTERNOON_OFF_WORK_START_TIME, timeStr)
                        Toast.makeText(this, "下午下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                        morningOffWorkStartTimeTv.text = "$timeStr"
                    }
                }
                changeTimeAfter(view, hourOfDay, minute)
            }, hour, min, true)
        mTimePickerDialog?.let {
            it.show()
        }
    }

    /**
     * 修改时间之后的处理
     */
    private fun changeTimeAfter(view: TimePicker?, hourOfDay: Int, minute: Int) {
        if (isEnableAutoSign) {
            Toast.makeText(this, "修改时间后，需要重新开启自动打卡", Toast.LENGTH_SHORT).show()
            isEnableAutoSign = false
            mEnableAutoSignSwitch.isChecked = false
            SharePrefHelper.putBoolean(IS_ENABLE_AUTO_SIGN, isEnableAutoSign)
        } else {

        }
    }


    private fun closeDateTimePicker() {
        mTimePickerDialog?.cancel()
        mTimePickerDialog = null
    }
}
