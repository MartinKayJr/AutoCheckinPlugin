package cn.martinkay.checkin

import android.app.Application

class SignApplication : Application() {
    private var mFlag = false

    companion object {
        private lateinit var mApp: SignApplication

        fun getInstance(): SignApplication {
            return mApp
        }
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this
    }

    fun getFlag(): Boolean {
        return mFlag
    }

    fun setFlag(mFlag: Boolean) {
        this.mFlag = mFlag
    }



}