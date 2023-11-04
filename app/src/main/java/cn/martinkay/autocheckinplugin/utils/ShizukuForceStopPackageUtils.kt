package cn.martinkay.autocheckinplugin.utils

import android.app.IActivityManager
import android.content.Context
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.InvocationTargetException

@RequiresApi(Build.VERSION_CODES.P)
object ShizukuForceStopPackageUtils {
    private val PACKAGE_MANAGER: IActivityManager by lazy {
        // This is needed to access hidden methods in IPackageManager
        HiddenApiBypass.addHiddenApiExemptions(
            "Landroid/app"
        )

        IActivityManager.Stub.asInterface(
            ShizukuBinderWrapper(
                SystemServiceHelper.getSystemService(
                    Context.ACTIVITY_SERVICE
                )
            )
        )
    }

    fun forceStopPackage(packageName: String) {
        PACKAGE_MANAGER.forceStopPackage(packageName, -1);
    }

}