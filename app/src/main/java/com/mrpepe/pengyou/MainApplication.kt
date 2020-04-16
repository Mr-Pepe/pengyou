package com.mrpepe.pengyou

import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.webkit.WebView

class MainApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        // This should be fixed with AppCompat 1.2.0 which is in beta01 at the moment
        try {
            WebView(applicationContext)
        } catch (e : Exception) {
            Log.e(TAG,
                  "Got exception while trying to instantiate WebView to avoid night mode issue. Ignoring problem.",
                  e)
        }
    }

    companion object {
        // Global definitions
        const val MAX_HISTORY_ENTRIES = 1000
        const val MAX_SEARCH_RESULTS = 201

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
