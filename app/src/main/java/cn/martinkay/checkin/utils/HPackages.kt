package cn.martinkay.checkin.utils

object HPackages {
    val myUserId get() = android.os.Process.myUserHandle().hashCode()
}