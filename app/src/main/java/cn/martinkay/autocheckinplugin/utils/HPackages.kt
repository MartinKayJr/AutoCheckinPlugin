package cn.martinkay.autocheckinplugin.utils

object HPackages {
    val myUserId get() = android.os.Process.myUserHandle().hashCode()
}