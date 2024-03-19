package cn.martinkay.autocheckinplugin.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.InputEvent
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import cn.martinkay.autocheckinplugin.utils.HPackages.myUserId
import moe.shizuku.server.IShizukuService
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object HShizuku {
    private val isRoot get() = Shizuku.getUid() == 0
    private val userId get() = if (isRoot) myUserId else 0

    @RequiresApi(Build.VERSION_CODES.P)
    private fun asInterface(className: String, serviceName: String): Any = ShizukuBinderWrapper(
        SystemServiceHelper.getSystemService(serviceName)
    ).let {
        Class.forName("$className\$Stub").run {
            if (HTarget.P) HiddenApiBypass.invoke(this, null, "asInterface", it)
            else getMethod("asInterface", IBinder::class.java).invoke(null, it)
        }
    }

    fun isEnable(ctx: Context): Boolean {
        //本函数用于检查shizuku状态，shizukuIsRun代表shizuk是否运行，shizukuIsAccept代表shizuku是否授权
        var shizukuIsRun = true
        var shizukuIsAccept = false
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(
                0
            ) else shizukuIsAccept = true
        } catch (e: Exception) {
            if (ContextCompat.checkSelfPermission(
                    ctx, "moe.shizuku.manager.permission.API_V23"
                ) == PackageManager.PERMISSION_GRANTED
            ) shizukuIsAccept = true
            if (e.javaClass == IllegalStateException::class.java) {
                shizukuIsRun = false
            }
        }
        return shizukuIsRun && shizukuIsAccept
    }

    val lockScreen
        @RequiresApi(Build.VERSION_CODES.P) get() = runCatching {
            val input = asInterface("android.hardware.input.IInputManager", "input")
            val inject = input::class.java.getMethod(
                "injectInputEvent", InputEvent::class.java, Int::class.java
            )
            val now = SystemClock.uptimeMillis()
            inject.invoke(
                input, KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 0), 0
            )
            inject.invoke(
                input, KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_POWER, 0), 0
            )
            true
        }.getOrElse {
            HLog.e(it)
            false
        }

    @RequiresApi(Build.VERSION_CODES.P)
    fun forceStopApp(packageName: String) = runCatching {
        asInterface("android.app.IActivityManager", "activity").let {
            if (HTarget.P) HiddenApiBypass.invoke(
                it::class.java, it, "forceStopPackage", packageName, userId
            ) else it::class.java.getMethod(
                "forceStopPackage", String::class.java, Int::class.java
            ).invoke(
                it, packageName, userId
            )
        }
        true
    }.getOrElse {
        HLog.e(it)
        false
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun clickHome() = runCatching {
        val input = asInterface("android.hardware.input.IInputManager", "input")
        val inject = input::class.java.getMethod(
            "injectInputEvent", InputEvent::class.java, Int::class.java
        )
        val now = SystemClock.uptimeMillis()
        inject.invoke(
            input, KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME, 0), 0
        )
        inject.invoke(
            input, KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME, 0), 0
        )
        true
    }.getOrElse {
        HLog.e(it)
        false
    }



    fun execute(command: String, root: Boolean = isRoot): Pair<Int, String> = runCatching {
        IShizukuService.Stub.asInterface(Shizuku.getBinder())
            .newProcess(arrayOf(if (root) "su" else "sh"), null, null).run {
                ParcelFileDescriptor.AutoCloseOutputStream(outputStream).use {
                    it.write(command.toByteArray())
                }
                waitFor() to inputStream.text.ifBlank { errorStream.text }.also { destroy() }
            }
    }.getOrElse { 0 to it.stackTraceToString() }

    private val ParcelFileDescriptor.text
        get() = ParcelFileDescriptor.AutoCloseInputStream(this)
            .use { it.bufferedReader().readText() }
}