package cn.martinkay.autocheckinplugin

import android.app.Application

class SignApplication : Application() {

    companion object {
        private lateinit var mApp: Application

        fun getApp(): Application {
            return mApp
        }
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this
    }

}