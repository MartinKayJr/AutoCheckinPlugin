package cn.martinkay.autocheckinplugin.utils

import android.os.Build

object HTarget {
    private fun target(api: Int): Boolean = Build.VERSION.SDK_INT >= api

    val N = target(Build.VERSION_CODES.N)
    val O = target(Build.VERSION_CODES.O)
    val P = target(Build.VERSION_CODES.P)
    val Q = target(Build.VERSION_CODES.Q)
    val T = target(Build.VERSION_CODES.TIRAMISU)
}