package cn.martinkay.autocheckinplugin.os

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}

inline fun <T : ViewBinding> AppCompatActivity.viewBindingRes(
    crossinline bindingView: (view: android.view.View) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    val rootView = this.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        ?: throw IllegalArgumentException("viewBindingRes 需要用 Activity(@LayoutRes int contentLayoutId) 构造方法初始化")
    bindingView.invoke(rootView)
}