package cn.martinkay.autocheckinplugin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import cn.martinkay.autocheckinplugin.broad.AlarmReceiver
import cn.martinkay.autocheckinplugin.constant.Constant
import cn.martinkay.autocheckinplugin.databinding.ActivityMainBinding
import cn.martinkay.autocheckinplugin.model.CalendarScheme
import cn.martinkay.autocheckinplugin.os.viewBindingRes
import cn.martinkay.autocheckinplugin.service.BackgroundAccess
import cn.martinkay.autocheckinplugin.service.MyAccessibilityService
import cn.martinkay.autocheckinplugin.service.WifiLockService
import cn.martinkay.autocheckinplugin.utils.AlarManagerUtil
import cn.martinkay.autocheckinplugin.utils.AutoSignPermissionUtils
import cn.martinkay.autocheckinplugin.utils.HShizuku
import cn.martinkay.autocheckinplugin.utils.IsServiceRunningUtil
import cn.martinkay.autocheckinplugin.utils.JumpPermissionManagement
import com.alibaba.fastjson.JSON
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBindingRes(ActivityMainBinding::bind)
    private val autoSignConfig = AutoSignConfig()

    private val permissionResultListener = OnRequestPermissionResultListener { _: Int, _: Int ->
        onRequestPermissionsResult()
    }

    private fun onRequestPermissionsResult() {
        check()
    }

    private fun check() {
        //本函数用于检查shizuku状态，shizukuIsRun代表shizuk是否运行，shizukuIsAccept代表shizuku是否授权
        autoSignConfig.shizukuIsRun = true
        autoSignConfig.shizukuIsAccept = false
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(
                0
            ) else autoSignConfig.shizukuIsAccept = true
        } catch (e: Exception) {
            if (checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED) autoSignConfig.shizukuIsAccept =
                true
            if (e.javaClass == IllegalStateException::class.java) {
                autoSignConfig.shizukuIsRun = false
                Toast.makeText(this, "Shizuku未运行", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shizukuIsRunBtn.text =
            if (autoSignConfig.shizukuIsRun) "Shizuku\n已运行" else "Shizuku\n未运行"
        binding.shizukuIsRunBtn.setTextColor(if (autoSignConfig.shizukuIsRun) autoSignConfig.initShizukuTextColor else 0x77ff0000)
        binding.shizukuIsAcceptBtn.text =
            if (autoSignConfig.shizukuIsAccept) "Shizuku\n已授权" else "Shizuku\n未授权"
        binding.shizukuIsAcceptBtn.setTextColor(if (autoSignConfig.shizukuIsAccept) autoSignConfig.initShizukuTextColor else 0x77ff0000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        autoSignConfig.readSharePreference()

        initCheckinViews()
        initCheckinCalendar()

        initViewModel()
        isOpenService()
        isCanBackground()
        initSetting()
        initShizuku()
        isIgnoreBatteryOption(this)
        lockWifiService()
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            AutoSignPermissionUtils.notifyCalendarSchemeEvent.collect {
                if (it == null) {
                    return@collect
                }
                AutoSignPermissionUtils.notifyCalendarSchemeEvent.emit(null)
                autoSignConfig.readCalendarSchemeData()
                binding.calendarView.setSchemeDate(autoSignConfig.compositeSchemeData())
            }
        }
    }

    private fun lockWifiService() {
        // 启动WifiLockService
        val intent = Intent(this, WifiLockService::class.java)
        startService(intent)
        // 启动MyAccessibilityService
        val intent2 = Intent(this, MyAccessibilityService::class.java)
        startService(intent2)
    }

    private fun initShizuku() {
        //shizuku返回授权结果时将执行RL函数
        Shizuku.addRequestPermissionResultListener(permissionResultListener);

        //m用于保存shizuku状态显示按钮的初始颜色（int类型哦），为的是适配安卓12的莫奈取色，方便以后恢复颜色时用
        autoSignConfig.initShizukuTextColor = binding.shizukuIsRunBtn.currentTextColor

        //检查Shizuk是否运行，并申请Shizuku权限
        check()
    }

    override fun onResume() {
        isOpenService()
        isCanBackground()
        super.onResume()
    }

    private fun isIgnoreBatteryOption(context: Context) {
        try {
            val intent = Intent()
            val packageName: String = context.packageName
            val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                context.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun initSetting() {
        if (Constant.isRoot) {
            autoSignConfig.accessblity = AlarmReceiver.isAccessibility
        } else if (Constant.isShizuku) {
            autoSignConfig.accessblity = AlarmReceiver.isAccessibilityByShizuku(Constant.isRoot)
        }
        if (autoSignConfig.accessblity) {
            SignApplication.getInstance().setFlag(true)
        } else {
            // 如果有root权限 则自动开启无障碍服务
            if (AlarmReceiver.isRoot == 0) {
                binding.enableRootSwitch.isChecked = true
                if (AlarmReceiver.enableAccessibility() == 0) {
                    Toast.makeText(this, "ROOT已为您开启无障碍服务", Toast.LENGTH_SHORT).show()
                    binding.accessbilitySwitch.isChecked = true
                    autoSignConfig.accessblity = true
                    SignApplication.getInstance().setFlag(true)
                    Log.i("MainActivity", "无障碍返回" + AlarmReceiver.isAccessibility)
                } else {
                    Toast.makeText(this, "ROOT为您开启无障碍服务失败", Toast.LENGTH_SHORT).show()
                    binding.accessbilitySwitch.isChecked = false
                    autoSignConfig.accessblity = false
                }
            } else if (HShizuku.isEnable(this)) {
                binding.enableShizukuSwitch.isChecked = true
                if (AlarmReceiver.enableAccessibilityByShizuku(Constant.isRoot) == 0) {
                    Toast.makeText(this, "Shizuku已为您开启无障碍服务", Toast.LENGTH_SHORT).show()
                    binding.accessbilitySwitch.isChecked = true
                    autoSignConfig.accessblity = true
                    SignApplication.getInstance().setFlag(true)
                    Log.i(
                        "MainActivity",
                        "无障碍返回" + AlarmReceiver.isAccessibilityByShizuku(Constant.isRoot)
                    )
                } else {
                    Toast.makeText(this, "Shizuku为您开启无障碍服务失败", Toast.LENGTH_SHORT).show()
                    binding.accessbilitySwitch.isChecked = false
                    autoSignConfig.accessblity = false
                }
            }
            Toast.makeText(this, "请手动打开无障碍服务", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOpenService() {
        if (!IsServiceRunningUtil.isAccessibilitySettingsOn(
                this, "cn.martinkay.autocheckinplugin.service.MyAccessibilityService"
            )
        ) {
            binding.accessbilitySwitch.isChecked = false
            autoSignConfig.accessblity = false
            return
        }
        binding.accessbilitySwitch.isChecked = true
        autoSignConfig.accessblity = true
    }

    private fun isCanBackground() {
        if (BackgroundAccess.canBackgroundStart(this)) {
            binding.canBackgroundSwitch.isChecked = true
            return
        }
        binding.canBackgroundSwitch.isChecked = false
        Toast.makeText(this, "请打开后台弹出界面权限", Toast.LENGTH_SHORT).show()
    }

    private fun initCheckinCalendar() {
        autoSignConfig.year = binding.calendarView.curYear
        binding.tvMonthDay.setOnClickListener(View.OnClickListener {
            if (binding.calendarLayout.isExpand) {
                binding.calendarLayout.expand()
                return@OnClickListener
            }

            binding.calendarView.showYearSelectLayout(autoSignConfig.year)

            binding.tvLunar.isGone = true
            binding.tvYear.isGone = true
            binding.tvMonthDay.text = autoSignConfig.year.toString()
        })

        binding.currentCalendarView.setOnClickListener {
            binding.calendarView.scrollToCurrent()
        }

        binding.tvMonthDay.text =
            "${binding.calendarView.curMonth}月${binding.calendarView.curDay}日"
        binding.tvCurrentYearMonth.text =
            "<${binding.calendarView.curYear}年${binding.calendarView.curMonth}月>"
        binding.tvLunar.text = "今日"
        binding.tvCurrentDay.text = binding.calendarView.curDay.toString()

        binding.clearCalendarView.setOnClickListener {
            showClearCalendarDialog()
        }

        binding.calendarView.setOnCalendarInterceptListener(object :
            CalendarView.OnCalendarInterceptListener {
            override fun onCalendarIntercept(calendar: Calendar): Boolean {
                val calendarInstance = java.util.Calendar.getInstance()
                val currentCalendar = Calendar().apply {
                    year = calendarInstance[java.util.Calendar.YEAR]
                    month = calendarInstance[java.util.Calendar.MONTH] + 1
                    day = calendarInstance[java.util.Calendar.DAY_OF_MONTH]
                }
                val compare = calendar.compareTo(currentCalendar)
                return compare < 0
            }

            override fun onCalendarInterceptClick(calendar: Calendar, isClick: Boolean) {
                if (isClick) {
                    Toast.makeText(this@MainActivity, "过去时间不支持设置", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
        binding.calendarView.setOnCalendarSelectListener(object :
            CalendarView.OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar?) {
            }

            override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
                if (!isClick) return
                val oldScheme = calendar.scheme
                val newScheme = if (oldScheme != CalendarScheme.AUTO_SIGN_DAY_ALLOW) {
                    CalendarScheme.AUTO_SIGN_DAY_ALLOW
                } else {
                    null
                }
                calendar.scheme = newScheme
                autoSignConfig.updateScheme(calendar.toString(), newScheme)
            }
        })
        binding.calendarView.setOnCalendarLongClickListener(object :
            CalendarView.OnCalendarLongClickListener {
            override fun onCalendarLongClickOutOfRange(calendar: Calendar?) {
            }

            override fun onCalendarLongClick(calendar: Calendar) {
                val oldScheme = calendar.scheme
                val newScheme = if (oldScheme != CalendarScheme.AUTO_SIGN_DAY_FORBIDDEN) {
                    CalendarScheme.AUTO_SIGN_DAY_FORBIDDEN
                } else {
                    null
                }
                calendar.scheme = newScheme
                autoSignConfig.updateScheme(calendar.toString(), newScheme)
            }
        }, true)

        binding.calendarView.setSchemeDate(autoSignConfig.compositeSchemeData())

        binding.calendarView.setOnMonthChangeListener(object : CalendarView.OnMonthChangeListener {
            override fun onMonthChange(year: Int, month: Int) {
                binding.tvCurrentYearMonth.text =
                    "<${year}年${month}月>"
            }
        })
    }

    private fun initCheckinViews() {
        var morningStartWorkStartTimeStr = getMorningStartWorkStartTimeStr()
        var morningOffWorkStartTimeStr = getMorningOffWorkStartTimeStr()
        var afternoonStartWorkOffStartTimeStr = getAfternoonStartWorkStartTimeStr()
        var afternoonOffWorkOffStartTimeStr = getAfternoonOffWorkStartTimeStr()

        binding.enableAutoSign.isChecked = autoSignConfig.isEnableAutoSign

        val readTimeJitterValue = SharePrefHelper.getLong(TIME_JITTER_VALUE, 3)
        binding.timeJitterSwitch.isChecked = autoSignConfig.isEnableTimeJitter
        binding.timeJitterEditView.setText(readTimeJitterValue.toString())

        // 早上上班打卡
        val isMorningStartOpen = SharePrefHelper.getBoolean(
            IS_OPEN_MORNING_START_WORK_SIGN_TASK, false
        )
        // 早上下班打卡
        val isMorningOffOpen = SharePrefHelper.getBoolean(IS_OPEN_MORNING_OFF_WORK_SIGN_TASK, false)
        // 下午上班打卡
        val isAfternoonStartOpen = SharePrefHelper.getBoolean(
            IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK, false
        )
        // 下午下班打卡
        val isAfternoonOffOpen = SharePrefHelper.getBoolean(
            IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK, false
        )
        binding.morningWorkStartTimeTv.text = formatTime(morningStartWorkStartTimeStr)
        binding.morningOffworkStartTimeTv.text = formatTime(morningOffWorkStartTimeStr)


        binding.afternoonWorkStartTimeTv.text = formatTime(afternoonStartWorkOffStartTimeStr)
        binding.afternoonOffworkStartTimeTv.text = formatTime(afternoonOffWorkOffStartTimeStr)

        // 早上初始化

        binding.morningStartWorkSwitch.isChecked = isMorningStartOpen
        binding.morningOffWorkSwitch.isChecked = isMorningOffOpen

        // 下午初始化
        binding.afternoonStartWorkSwitch.isChecked = isAfternoonStartOpen
        binding.afternoonOffWorkSwitch.isChecked = isAfternoonOffOpen

        val commonOnCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                when (buttonView.id) {
                    // 开启ROOT
                    R.id.enable_root_switch -> {
                        if (buttonView.isPressed) {
                            val result = AlarmReceiver.isRoot
                            if (result == 0) {
                                Constant.isRoot = true
                                binding.enableRootSwitch.isChecked = true
                                Toast.makeText(this, "ROOT权限已开启", Toast.LENGTH_SHORT).show()
                            } else {
                                Constant.isRoot = false
                                binding.enableRootSwitch.isChecked = false
                                Toast.makeText(this, "ROOT权限未开启", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    R.id.enable_shizuku_switch -> {
                        if (buttonView.isPressed) {
                            // 检查Shizuk是否运行，并申请Shizuku权限
                            check()
                            if (autoSignConfig.shizukuIsRun) {
                                if (autoSignConfig.shizukuIsAccept) {
                                    Toast.makeText(this, "Shizuku已授权", Toast.LENGTH_SHORT).show()
                                    Constant.isShizuku = true
                                    binding.enableShizukuSwitch.isChecked = true
                                } else {
                                    Toast.makeText(this, "Shizuku未授权", Toast.LENGTH_SHORT).show()
                                    Constant.isShizuku = false
                                    binding.enableShizukuSwitch.isChecked = false
                                }
                            } else {
                                Toast.makeText(this, "Shizuku未运行", Toast.LENGTH_SHORT).show()
                                Constant.isShizuku = false
                                binding.enableShizukuSwitch.isChecked = false
                            }
                        }
                    }

                    R.id.accessbility_switch -> {
                        if (buttonView.isPressed) {
                            if (autoSignConfig.accessblity) {
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

                    // 开启时间抖动
                    R.id.time_jitter_switch -> {
                        autoSignConfig.isEnableTimeJitter = isChecked
                        SharePrefHelper.putBoolean(IS_ENABLE_TIME_JITTER, isChecked)
                        if (isChecked) {
                            val timeJitterValue =
                                binding.timeJitterEditView.text.toString().toLong()
                            // 判断是否为数字
                            if (!timeJitterValue.toString().matches(Regex("[0-9]+"))) {
                                Toast.makeText(
                                    this, "时间抖动值必须为数字", Toast.LENGTH_SHORT
                                ).show()
                                binding.timeJitterSwitch.isChecked = false
                                autoSignConfig.isEnableTimeJitter = false
                                return@OnCheckedChangeListener
                            }
                            SharePrefHelper.putLong(TIME_JITTER_VALUE, timeJitterValue)
                            Toast.makeText(
                                this, "已开启时间抖动，抖动值为$timeJitterValue", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this, "已关闭时间抖动", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // 开启自动签到
                    R.id.enable_auto_sign -> {
                        autoSignConfig.isEnableAutoSign = isChecked;
                        SharePrefHelper.putBoolean(IS_ENABLE_AUTO_SIGN, isChecked)
                        if (isChecked) {
                            if (autoSignConfig.accessblity) {
                                // 早上上班打卡
                                val isMorningStartOpenCondition = SharePrefHelper.getBoolean(
                                    IS_OPEN_MORNING_START_WORK_SIGN_TASK, false
                                )
                                // 早上下班打卡
                                val isMorningOffOpenCondition = SharePrefHelper.getBoolean(
                                    IS_OPEN_MORNING_OFF_WORK_SIGN_TASK,
                                    false
                                )
                                // 下午上班打卡
                                val isAfternoonStartOpenCondition = SharePrefHelper.getBoolean(
                                    IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK, false
                                )
                                // 下午下班打卡
                                val isAfternoonOffOpenCondition = SharePrefHelper.getBoolean(
                                    IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK, false
                                )
                                SignApplication.getInstance().setFlag(true)
                                // 早上上班打卡
                                // morningStartWorkStartTimeStr分割为小时和分钟
                                // 判断是否勾选开启早上上班自动打卡
                                val tips = StringBuilder();
                                if (isMorningStartOpenCondition) {
                                    morningStartWorkStartTimeStr = getMorningStartWorkStartTimeStr()
                                    val morningStartWorkStartTimeStrArr =
                                        morningStartWorkStartTimeStr.split(
                                            ":"
                                        )
                                    AlarManagerUtil.timedTackMonWork(
                                        this,
                                        Integer.valueOf(morningStartWorkStartTimeStrArr[0]),
                                        Integer.valueOf(morningStartWorkStartTimeStrArr[1]),
                                        0
                                    )
                                    tips.append("开启A,")
                                } else {
                                    tips.append("关闭A,")
                                }

                                // 早上下班打卡
                                if (isMorningOffOpenCondition) {
                                    morningOffWorkStartTimeStr = getMorningOffWorkStartTimeStr()
                                    val morningOffWorkStartTimeStrArr =
                                        morningOffWorkStartTimeStr.split(":")
                                    AlarManagerUtil.timedTackMonOffWork(
                                        this,
                                        Integer.valueOf(morningOffWorkStartTimeStrArr[0]),
                                        Integer.valueOf(morningOffWorkStartTimeStrArr[1]),
                                        1
                                    )
                                    tips.append("开启B,")
                                } else {
                                    tips.append("关闭B,")
                                }

                                // 下午上班打卡
                                if (isAfternoonStartOpenCondition) {
                                    afternoonStartWorkOffStartTimeStr =
                                        getAfternoonStartWorkStartTimeStr()
                                    val afternoonStartWorkOffStartTimeStrArr =
                                        afternoonStartWorkOffStartTimeStr.split(
                                            ":"
                                        )
                                    AlarManagerUtil.timedTackAfWork(
                                        this,
                                        Integer.valueOf(afternoonStartWorkOffStartTimeStrArr[0]),
                                        Integer.valueOf(afternoonStartWorkOffStartTimeStrArr[1]),
                                        2
                                    )
                                    tips.append("开启C,")
                                } else {
                                    tips.append("关闭C,")
                                }

                                // 下午下班打卡
                                if (isAfternoonOffOpenCondition) {
                                    afternoonOffWorkOffStartTimeStr =
                                        getAfternoonOffWorkStartTimeStr()
                                    val afternoonOffWorkOffStartTimeStrArr =
                                        afternoonOffWorkOffStartTimeStr.split(
                                            ":"
                                        )
                                    AlarManagerUtil.timedTackAfOffWork(
                                        this,
                                        Integer.valueOf(afternoonOffWorkOffStartTimeStrArr[0]),
                                        Integer.valueOf(afternoonOffWorkOffStartTimeStrArr[1]),
                                        3
                                    )
                                    tips.append("开启D")
                                } else {
                                    tips.append("关闭D")
                                }

                                Toast.makeText(
                                    this, "已开启自动打卡：$tips", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // 未开启辅助功能
                                Toast.makeText(
                                    this, "请开启辅助功能", Toast.LENGTH_SHORT
                                ).show()
                                this.binding.enableAutoSign.isChecked = false
                                autoSignConfig.isEnableAutoSign = false
                            }
                        } else {
                            AlarManagerUtil.cancelTimetacker(this, true)
                            SignApplication.getInstance().setFlag(false)
                        }
                    }

                    R.id.morning_start_work_switch -> {
                        SharePrefHelper.putBoolean(IS_OPEN_MORNING_START_WORK_SIGN_TASK, isChecked)
                        Toast.makeText(
                            this,
                            "修改后注意重新开启，当前状态$isChecked",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    R.id.morning_off_work_switch -> {
                        SharePrefHelper.putBoolean(IS_OPEN_MORNING_OFF_WORK_SIGN_TASK, isChecked)
                        Toast.makeText(
                            this,
                            "修改后注意重新开启，当前状态$isChecked",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    R.id.afternoon_start_work_switch -> {
                        SharePrefHelper.putBoolean(
                            IS_OPEN_AFTERNOON_START_WORK_SIGN_TASK,
                            isChecked
                        )
                        Toast.makeText(
                            this,
                            "修改后注意重新开启，当前状态$isChecked",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    R.id.afternoon_off_work_switch -> {
                        SharePrefHelper.putBoolean(IS_OPEN_AFTERNOON_OFF_WORK_SIGN_TASK, isChecked)
                        Toast.makeText(
                            this,
                            "修改后注意重新开启，当前状态$isChecked",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        // 修改事件
        binding.morningStartWorkSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.morningOffWorkSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.afternoonStartWorkSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.afternoonOffWorkSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.enableAutoSign.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.accessbilitySwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.canBackgroundSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.timeJitterSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)

        binding.enableRootSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
        binding.enableShizukuSwitch.setOnCheckedChangeListener(commonOnCheckedChangeListener)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.test_close_app_btn -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    HShizuku.forceStopApp("com.tencent.wework")
                }
            }

            R.id.test_home_app_btn -> {
                // root执行 adb shell input keyevent 3
                if (Shell.su("input keyevent 3").exec().isSuccess) {
                    Log.i("MyAccessibilityService", "点击Home键成功")
                }
            }

            R.id.start_sign -> {
                gotoWeWork()
            }

            R.id.morning_work_start_time_tv -> {
                val startTimeStr = getMorningStartWorkStartTimeStr()
                val startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(true, startHour, startMinute)
            }

            R.id.morning_offwork_start_time_tv -> {
                val startTimeStr = getMorningOffWorkStartTimeStr()
                val startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showMorningDateTimePicker(false, startHour, startMinute)
            }

            R.id.afternoon_work_start_time_tv -> {
                val startTimeStr = getAfternoonStartWorkStartTimeStr()
                val startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(true, startHour, startMinute)
            }

            R.id.afternoon_offwork_start_time_tv -> {
                val startTimeStr = getAfternoonOffWorkStartTimeStr()
                val startHour = startTimeStr.split(":")[0].toInt()
                val startMinute = startTimeStr.split(":")[1].toInt()
                showAfternoonDateTimePicker(false, startHour, startMinute)
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
        if (intent == null) {
            Toast.makeText(this, "请安装企业微信 或 允许获取已安装应用权限", Toast.LENGTH_SHORT)
                .show()
            return
        }
        startActivity(intent)
    }

    private var mTimePickerDialog: TimePickerDialog? = null

    private fun showMorningDateTimePicker(
        isStartWork: Boolean, hour: Int, min: Int
    ) {
        closeDateTimePicker()
        mTimePickerDialog = TimePickerDialog(
            this, { _, hourOfDay, minute ->
                closeDateTimePicker()
                val timeStr = formatTime("$hourOfDay:$minute")
                if (isStartWork) {
                    SharePrefHelper.putString(SIGN_TASK_MORNING_START_WORK_START_TIME, timeStr)
                    Toast.makeText(this, "早上上班打卡开始时间:$timeStr", Toast.LENGTH_SHORT).show()
                    binding.morningWorkStartTimeTv.text = timeStr
                } else {
                    SharePrefHelper.putString(SIGN_TASK_MORNING_OFF_WORK_START_TIME, timeStr)
                    Toast.makeText(this, "早上下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT).show()
                    binding.morningOffworkStartTimeTv.text = timeStr
                }
                changeTimeAfter()
            }, hour, min, true
        )
        mTimePickerDialog?.show()
    }

    private fun showAfternoonDateTimePicker(
        isStartWork: Boolean, hour: Int, min: Int
    ) {
        closeDateTimePicker()
        mTimePickerDialog = TimePickerDialog(
            this, { _, hourOfDay, minute ->
                closeDateTimePicker()
                val timeStr = formatTime("$hourOfDay:$minute")
                if (isStartWork) {
                    SharePrefHelper.putString(
                        SIGN_TASK_AFTERNOON_START_WORK_START_TIME, timeStr
                    )
                    Toast.makeText(this, "下午上班打卡开始时间:$timeStr", Toast.LENGTH_SHORT).show()
                    binding.afternoonWorkStartTimeTv.text = timeStr
                } else {
                    SharePrefHelper.putString(SIGN_TASK_AFTERNOON_OFF_WORK_START_TIME, timeStr)
                    Toast.makeText(this, "下午下班打卡开始时间:$timeStr", Toast.LENGTH_SHORT).show()
                    binding.afternoonOffworkStartTimeTv.text = timeStr
                }
                changeTimeAfter()
            }, hour, min, true
        )
        mTimePickerDialog?.show()
    }

    private fun showClearCalendarDialog() {
        if (autoSignConfig.calendarSchemeMap.isEmpty()) {
            Toast.makeText(this, "暂无历史记录和打卡任务", Toast.LENGTH_SHORT).show()
            return
        }
        val animals = arrayOf("历史记录及打卡任务", "打卡任务")
        var checkIndex = 0
        AlertDialog.Builder(this).setTitle("请选择清除类型")
            .setSingleChoiceItems(animals, checkIndex) { _, which ->
                checkIndex = which
            }.setPositiveButton("确定") { _, _ ->
                if (checkIndex == 0) {
                    autoSignConfig.clearAllScheme()
                    binding.calendarView.setSchemeDate(emptyMap())
                } else if (checkIndex == 1) {
                    autoSignConfig.clearAutoSignTask()
                    binding.calendarView.setSchemeDate(autoSignConfig.compositeSchemeData())
                }
            }.setNegativeButton("取消", null).show()
    }

    /**
     * 修改时间之后的处理
     */
    private fun changeTimeAfter() {
        if (autoSignConfig.isEnableAutoSign) {
            Toast.makeText(this, "修改时间后，需要重新开启自动打卡", Toast.LENGTH_SHORT).show()
            autoSignConfig.isEnableAutoSign = false
            binding.enableAutoSign.isChecked = false
            SharePrefHelper.putBoolean(IS_ENABLE_AUTO_SIGN, autoSignConfig.isEnableAutoSign)
        }
    }

    private fun closeDateTimePicker() {
        mTimePickerDialog?.cancel()
        mTimePickerDialog = null
    }

    fun ch(view: View?) {
        //本函数绑定了主界面两个显示Shizuk状态的按钮的点击事件
        check()
    }

    override fun onDestroy() {
        //在APP退出时，取消注册Shizuku授权结果监听，这是Shizuku的要求
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        super.onDestroy()
    }

    data class AutoSignConfig(
        var shizukuIsRun: Boolean = false,
        var shizukuIsAccept: Boolean = false,
        var isEnableAutoSign: Boolean = false,
        var isEnableTimeJitter: Boolean = false,
        var accessblity: Boolean = false,
        var year: Int = 2023,
        @ColorInt var initShizukuTextColor: Int = 0
    ) {
        val calendarSchemeMap: MutableMap<String, CalendarScheme> = mutableMapOf()

        fun readSharePreference() {
            isEnableAutoSign = SharePrefHelper.getBoolean(IS_ENABLE_AUTO_SIGN, false)
            isEnableTimeJitter = SharePrefHelper.getBoolean(IS_ENABLE_TIME_JITTER, false)
            readCalendarSchemeData()
        }

        fun readCalendarSchemeData() {
            calendarSchemeMap.clear()
            kotlin.runCatching {
                JSON.parseArray(
                    SharePrefHelper.getString(SIGN_CALENDAR_SCHEME_CACHE, ""),
                    CalendarScheme::class.java
                ).associateBy { it.date }
            }.onFailure { it.printStackTrace() }.getOrNull()?.let {
                calendarSchemeMap.putAll(it)
            }
        }

        fun compositeSchemeData(): Map<String, Calendar> {
            return calendarSchemeMap.mapValues {
                Calendar().apply { scheme = it.value.scheme }
            }
        }

        fun updateScheme(date: String, newScheme: String?) {
            if (newScheme.isNullOrEmpty()) {
                calendarSchemeMap.remove(date)
            } else {
                val value = calendarSchemeMap[date] ?: CalendarScheme().apply {
                    this.date = date
                }
                value.scheme = newScheme
                calendarSchemeMap[date] = value
            }
            SharePrefHelper.putString(
                SIGN_CALENDAR_SCHEME_CACHE,
                JSON.toJSONString(calendarSchemeMap.map { it.value }.toList())
            )
        }

        fun clearAllScheme() {
            calendarSchemeMap.clear()
            SharePrefHelper.putString(
                SIGN_CALENDAR_SCHEME_CACHE,
                JSON.toJSONString(calendarSchemeMap.map { it.value }.toList())
            )
        }

        fun clearAutoSignTask() {
            val iterator = calendarSchemeMap.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.value.isFutureTask) {
                    iterator.remove()
                }
            }
            SharePrefHelper.putString(
                SIGN_CALENDAR_SCHEME_CACHE,
                JSON.toJSONString(calendarSchemeMap.map { it.value }.toList())
            )
        }
    }

    companion object {
        const val PACKAGE_WECHAT_WORK = "com.tencent.wework"
    }
}
