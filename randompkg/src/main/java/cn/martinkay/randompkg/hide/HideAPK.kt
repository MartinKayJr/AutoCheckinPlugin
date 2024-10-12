package cn.martinkay.randompkg.hide

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
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
//            getEnv(activity)
//            if (Constant.shizukuIsRun && Constant.shizukuIsAccept) {
//                val installApkResult = ShizukuUtil.installApk(repack.absolutePath)
//                UiThreadHandler.run {
//                    Toast.makeText(
//                        activity,
//                        "随机包名Shizuku安装返回:${installApkResult}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//                return true
//            }
            return customInstaller(repack.absolutePath, activity, pkg)
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


    //安装App
    fun customInstaller(appPath: String, activity: Activity, packageName: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //分屏
            //intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            Constant.pkg = activity.packageName
            val apkUri: Uri = FileProvider.getUriForFile(
                activity.applicationContext,
                "${Constant.pkg}.fileProvider",
                File(appPath)
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")

            //默认系统安装器
            val apkInstall: String = packageName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val canRequestPackageInstalls =
                    activity.packageManager.canRequestPackageInstalls()
                if (!canRequestPackageInstalls) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES), 666)
                }
            }


            val queryIntentActivities: List<ResolveInfo> =
                activity.packageManager
                    .queryIntentActivities(intent, 0)
            if (queryIntentActivities.isNotEmpty()) {
                for (resolveInfo in queryIntentActivities) {
                    val activityInfo = resolveInfo.activityInfo
                    if (apkInstall == activityInfo.applicationInfo.packageName) {
                        intent.setComponent(
                            ComponentName(
                                activityInfo.applicationInfo.packageName,
                                activityInfo.name
                            )
                        )
                        break
                    }
                }
            }
            activity.startActivity(intent)

            Log.d("HandleEventInstalApp", intent.toString())
            return true
        } catch (th: Throwable) {
            throw Error(th)
        }
        return false
    }
}
