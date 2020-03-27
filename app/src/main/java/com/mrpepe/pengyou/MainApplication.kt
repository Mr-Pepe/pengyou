package com.mrpepe.pengyou

import android.app.Activity
import android.app.Application
import android.content.Context

class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        // Global definitions
        const val MAX_HISTORY_ENTRIES = 1000
        const val MAX_SEARCH_RESULTS = 1000

        var pinyinMode = ""
        var chineseMode = ""

        lateinit var homeActivity: HomeActivity

        private var instance: MainApplication? = null
        private var currentActivity: Activity? = null

        var keyboardVisible = false

        fun getContext() : Context {
            return instance!!.applicationContext
        }

        fun setCurrentActivity(currentActivity: Activity?) {
            this.currentActivity = currentActivity
        }

        fun getCurrentActivity(): Activity? {
            return currentActivity
        }
    }



}
