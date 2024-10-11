package cn.martinkay.randompkg.hide

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import cn.martinkay.randompkg.signing.JarMap
import cn.martinkay.randompkg.signing.SignApk
import cn.martinkay.randompkg.utils.AXML
import cn.martinkay.randompkg.utils.Constant
import cn.martinkay.randompkg.utils.Keygen
import cn.martinkay.randompkg.utils.ShellUtils
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.UiThreadHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.SecureRandom

object HideAPK {

    private const val ALPHA = "abcdefghijklmnopqrstuvwxyz"
    private const val ALPHADOTS = "$ALPHA....."
    private const val ANDROID_MANIFEST = "AndroidManifest.xml"
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


    private fun genPackageName(): String {
        val random = SecureRandom()
        val len = 5 + random.nextInt(15)
        val builder = StringBuilder(len)
        var next: Char
        var prev = 0.toChar()
        for (i in 0 until len) {
            next = if (prev == '.' || i == 0 || i == len - 1) {
                ALPHA[random.nextInt(ALPHA.length)]
            } else {
                ALPHADOTS[random.nextInt(ALPHADOTS.length)]
            }
            builder.append(next)
            prev = next
        }
        if (!builder.contains('.')) {
            // Pick a random index and set it as dot
            val idx = random.nextInt(len - 2)
            builder[idx + 1] = '.'
        }
        return builder.toString()
    }

    fun patch(
        context: Context,
        apk: File, out: OutputStream,
        pkg: String, label: CharSequence,
        applicationId: String
    ): Boolean {
        val info = context.packageManager.getPackageArchiveInfo(apk.path, 0) ?: return false
        val nonLocalizedLabel = info.applicationInfo.nonLocalizedLabel
        val name = nonLocalizedLabel?.toString() ?: "AutoCheckinPlugin"
        try {
            JarMap.open(apk, true).use { jar ->
                val je = jar.getJarEntry(ANDROID_MANIFEST)
                val xml = AXML(jar.getRawData(je))

                if (!xml.findAndPatch(applicationId to pkg, name to label.toString()))
                    return false

                // Write apk changes
                jar.getOutputStream(je).use { it.write(xml.bytes) }
                val keys = Keygen(context)
                SignApk.sign(keys.cert, keys.key, jar, out)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
//            Timber.e(e)
            return false
        }
    }

    private fun getEnv(activity: Activity) {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(
                0
            ) else Constant.shizukuIsAccept = true
        } catch (e: Exception) {
            if (activity.checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED) Constant.shizukuIsAccept =
                true
            if (e.javaClass == IllegalStateException::class.java) {
                Constant.shizukuIsRun = false
            }
        }
    }

    private suspend fun patchAndHide(
        activity: Activity,
        label: String,
        onFailure: Runnable,
        path: String,
        applicationId: String
    ): Boolean {
//        val stub = File(activity.cacheDir, "stub.apk")
        val stub = File(path)

        // Generate a new random package name and signature
        val repack = File(activity.cacheDir, "patched.apk")

        val pkg = genPackageName()

        if (!patch(activity, stub, FileOutputStream(repack), pkg, label, applicationId))
            return false

        // Install
        val cmd = "pm install -t ${repack.absolutePath}"

        if (isRoot == 0) {
            val exec = Shell.su(cmd).exec()
            if (exec.isSuccess) {
                UiThreadHandler.run {
                    Toast.makeText(
                        activity,
                        "随机包名安装成功,应用名:${label}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return true
            } else {
                UiThreadHandler.run {
                    Toast.makeText(activity, exec.out[0], Toast.LENGTH_LONG).show()
                }
                return false
            }
        } else {
            getEnv(activity)
            if (Constant.shizukuIsRun && Constant.shizukuIsAccept) {
                return true
            }
            return false
        }
    }

    //label:应用的名称
    //path:原apk安装包路径
    @Suppress("DEPRECATION")
    suspend fun hide(activity: Activity, label: String, path: String, applicationId: String) {
        val onFailure = Runnable {

        }
        val success = withContext(Dispatchers.IO) {
            patchAndHide(activity, label, onFailure, path, applicationId)
        }
        if (!success) onFailure.run()
    }

}
