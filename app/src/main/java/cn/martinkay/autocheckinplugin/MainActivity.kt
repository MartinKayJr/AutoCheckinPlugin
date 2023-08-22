package cn.martinkay.autocheckinplugin

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.martinkay.autocheckinplugin.service.SignService


const val PACKAGE_WECHAT_WORK = "com.tencent.wework"

class MainActivity : AppCompatActivity() {

    /**
     * 范围
     */
    // 早上上班开始时间
    private lateinit var morningWorkStartTimeTv: TextView

    // 早上上班结束时间
    private lateinit var morningWorkStopTimeTv: TextView

    /**
     * 范围
     */
    // 早上下班开始时间
    private lateinit var morningOffWorkStartTimeTv: TextView

    // 早上下班结束时间
    private lateinit var morningOffWorkStopTimeTv: TextView

    /**
     * 范围
     */
    // 下午上班开始时间
    private lateinit var afternoonWorkStartTimeTv: TextView

    // 下午上班借宿时间
    private lateinit var afternoonWorkStopTimeTv: TextView


    /**
     * 范围
     */
    // 下午下班结束时间
    private lateinit var afternoonOffWorkStartTimeTv: TextView
    private lateinit var afternoonOffWorkStopTimeTv: TextView


    private lateinit var mMorningStartWorkSwitch: CheckBox
    private lateinit var mMorningOffWorkSwitch: CheckBox

    private lateinit var mAfternoonStartWorkSwitch: CheckBox
    private lateinit var mAfternoonOffWorkSwitch: CheckBox

    private lateinit var mWeekSaturdaySwitch: CheckBox
    private lateinit var mWeekSundaySwitch: CheckBox

    /**
     * 是否已经完成打卡状态
     */
    private lateinit var mMorningStartCheckinSatus: CheckBox
    private lateinit var mMorningEndCheckinSatus: CheckBox
    private lateinit var mAfternoonStartCheckinSatus: CheckBox
    private lateinit var mAfternoonEndCheckinSatus: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        // 早上上班 时间范围
        morningWorkStartTimeTv = findViewById(R.id.morning_work_start_time_tv)
        morningWorkStopTimeTv = findViewById(R.id.morning_work_stop_time_tv)
        // 早上下班 时间范围
        morningOffWorkStartTimeTv = findViewById(R.id.morning_offwork_start_time_tv)
        morningOffWorkStopTimeTv = findViewById(R.id.morning_offwork_stop_time_tv)
        // 下午上班 时间范围
        afternoonWorkStartTimeTv = findViewById(R.id.afternoon_work_start_time_tv)
        afternoonWorkStopTimeTv = findViewById(R.id.afternoon_work_stop_time_tv)
        // 下午下班 时间范围
        afternoonOffWorkStartTimeTv = findViewById(R.id.afternoon_offwork_start_time_tv)
        afternoonOffWorkStopTimeTv = findViewById(R.id.afternoon_offwork_stop_time_tv)

        // 早上开始上班 switch
        mMorningStartWorkSwitch = findViewById(R.id.morning_start_work_cb)
        // 早上结束上班 switch
        mMorningOffWorkSwitch = findViewById(R.id.morning_off_work_cb)
        // 下午开始上班 switch
        mAfternoonStartWorkSwitch = findViewById(R.id.afternoon_start_work_cb)
        // 下午结束上班switch
        mAfternoonOffWorkSwitch = findViewById(R.id.afternoon_off_work_cb)

        mWeekSaturdaySwitch = findViewById(R.id.saturday_ck)
        mWeekSundaySwitch = findViewById(R.id.sunday_ck)


        // 初始化状态
        mMorningStartCheckinSatus = findViewById(R.id.morning_start_checkin_status)
        mMorningEndCheckinSatus = findViewById(R.id.morning_end_checkin_status)
        mAfternoonStartCheckinSatus = findViewById(R.id.afternoon_start_checkin_status)
        mAfternoonEndCheckinSatus = findViewById(R.id.afternoon_end_checkin_status)

        val morningStartWorkStartTimeStr = getMorningStartWorkStartTimeStr()
        val morningStartWorkStopTimeStr = getMorningStartWorkStopTimeStr()

        val morningOffWorkStartTimeStr = getMorningOffWorkStartTimeStr()
        val morningOffWorkStopTimeStr = getMorningOffWorkStopTimeStr()


        val afternoonStartWorkOffStartTimeStr = getAfternoonStartWorkStartTimeStr()
        val afternoonStartWorkOffStopTimeStr = getAfternoonStartWorkStopTimeStr()

        val afternoonOffWorkOffStartTimeStr = getAfternoonOffWorkStartTimeStr()
        val afternoonOffWorkOffStopTimeStr = getAfternoonOffWorkStopTimeStr()


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
        morningWorkStopTimeTv.text = formatTime(morningStartWorkStopTimeStr)
        morningOffWorkStartTimeTv.text = formatTime(morningOffWorkStartTimeStr)
        morningOffWorkStopTimeTv.text = formatTime(morningOffWorkStopTimeStr)

        afternoonWorkStartTimeTv.text = formatTime(afternoonStartWorkOffStartTimeStr)
        afternoonWorkStopTimeTv.text = formatTime(afternoonStartWorkOffStopTimeStr)
        afternoonOffWorkStartTimeTv.text = formatTime(afternoonOffWorkOffStartTimeStr)
        afternoonOffWorkStopTimeTv.text = formatTime(afternoonOffWorkOffStopTimeStr)


        // 早上初始化
        mMorningStartWorkSwitch.isChecked = isMorningStartOpen
        mMorningOffWorkSwitch.isChecked = isMorningOffOpen

        // 下午初始化
        mAfternoonStartWorkSwitch.isChecked = isAfternoonStartOpen
        mAfternoonOffWorkSwitch.isChecked = isAfternoonOffOpen


        mWeekSaturdaySwitch.isChecked = isWeekSaturdayOpen
        mWeekSundaySwitch.isChecked = isWeekSundayOpen

        // 初始化状态的指
        mMorningStartCheckinSatus.isChecked = SharePrefHelper.getBoolean(
            IS_FINISH_MORNING_START_WORK_SIGN_TASK,
            false
        )
        mMorningEndCheckinSatus.isChecked = SharePrefHelper.getBoolean(
            IS_FINISH_MORNING_OFF_WORK_SIGN_TASK,
            false
        )
        mAfternoonStartCheckinSatus.isChecked = SharePrefHelper.getBoolean(
            IS_FINISH_AFTERNOON_START_WORK_SIGN_TASK,
            false
        )
        mAfternoonEndCheckinSatus.isChecked = SharePrefHelper.getBoolean(
            IS_FINISH_AFTERNOON_OFF_WORK_SIGN_TASK,
            false
        )

        val cbCheckChange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {
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

                R.id.saturday_ck -> {
                    SharePrefHelper.putBoolean(IS_OPEN_SATURDAY_SIGN_TASK, isChecked)
                }

                R.id.sunday_ck -> {
                    SharePrefHelper.putBoolean(IS_OPEN_SUNDAY_SIGN_TASK, isChecked)

                }
            }
        }
        // 修改事件
        mMorningStartWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mMorningOffWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mAfternoonStartWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mAfternoonOffWorkSwitch.setOnCheckedChangeListener(cbCheckChange)
        mWeekSaturdaySwitch.setOnCheckedChangeListener(cbCheckChange)
        mWeekSundaySwitch.setOnCheckedChangeListener(cbCheckChange)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.open_acc -> {
                gotoAccessibilityAct()
            }

            R.id.start_sign -> {
                startSign()
                gotoWeWork()
            }

            R.id.morning_work_start_time_tv -> {
                val startTimeStr = getMorningStartWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(true, true, startHour, startMinute)
            }

            R.id.morning_work_stop_time_tv -> {
                val stopTimeStr = getMorningStartWorkStopTimeStr()
                var stopHour = stopTimeStr.split(":")[0].toInt()
                val stopMinute = stopTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(false, true, stopHour, stopMinute)
            }

            R.id.morning_offwork_start_time_tv -> {
                val startTimeStr = getMorningOffWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(true, true, startHour, startMinute)
            }

            R.id.morning_offwork_stop_time_tv -> {
                val stopTimeStr = getMorningOffWorkStopTimeStr()
                var stopHour = stopTimeStr.split(":")[0].toInt()
                val stopMinute = stopTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(false, true, stopHour, stopMinute)
            }

            R.id.afternoon_work_start_time_tv -> {
                val startTimeStr = getAfternoonStartWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(true, false, startHour, startMinute)
            }

            R.id.afternoon_work_stop_time_tv -> {
                val stopTimeStr = getAfternoonStartWorkStopTimeStr()
                var stopHour = stopTimeStr.split(":")[0].toInt()
                val stopMinute = stopTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(false, false, stopHour, stopMinute)
            }

            R.id.afternoon_offwork_start_time_tv -> {
                val startTimeStr = getAfternoonOffWorkStartTimeStr()
                var startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(true, false, startHour, startMinute)
            }

            R.id.afternoon_offwork_stop_time_tv -> {
                val stopTimeStr = getAfternoonOffWorkStopTimeStr()
                var stopHour = stopTimeStr.split(":")[0].toInt()
                val stopMinute = stopTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(false, false, stopHour, stopMinute)
            }
        }
    }

    private fun gotoAccessibilityAct() {
        SignService.mStartOpen = true
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

    private fun startSign() {
        val intent = Intent(this, SignService::class.java)
        intent.action = SignService.ACTION_DO_ALARM_SIGN
        startService(intent)
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
                    } else {
                        SharePrefHelper.putString(
                            SIGN_TASK_MORNING_START_WORK_STOP_TIME,
                            "$timeStr"
                        )
                        morningWorkStopTimeTv.text = "$timeStr"
                        Toast.makeText(this, "早上上班打卡结束时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    if (isStart) {
                        SharePrefHelper.putString(SIGN_TASK_MORNING_OFF_WORK_START_TIME, timeStr)
                        Toast.makeText(this, "早上下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                        morningOffWorkStartTimeTv.text = "$timeStr"
                    } else {
                        SharePrefHelper.putString(SIGN_TASK_MORNING_OFF_WORK_STOP_TIME, "$timeStr")
                        morningOffWorkStopTimeTv.text = "$timeStr"
                        Toast.makeText(this, "早上下班打卡结束时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
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
                    } else {
                        SharePrefHelper.putString(
                            SIGN_TASK_AFTERNOON_START_WORK_STOP_TIME,
                            "$timeStr"
                        )
                        morningWorkStopTimeTv.text = "$timeStr"
                        Toast.makeText(this, "下午上班打卡结束时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    if (isStart) {
                        SharePrefHelper.putString(SIGN_TASK_AFTERNOON_OFF_WORK_START_TIME, timeStr)
                        Toast.makeText(this, "下午下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                        morningOffWorkStartTimeTv.text = "$timeStr"
                    } else {
                        SharePrefHelper.putString(
                            SIGN_TASK_AFTERNOON_OFF_WORK_STOP_TIME,
                            "$timeStr"
                        )
                        morningOffWorkStopTimeTv.text = "$timeStr"
                        Toast.makeText(this, "下午下班打卡结束时间:$timeStr", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }, hour, min, true)
        mTimePickerDialog?.let {
            it.show()
        }
    }


    private fun closeDateTimePicker() {
        mTimePickerDialog?.cancel()
        mTimePickerDialog = null
    }
}
