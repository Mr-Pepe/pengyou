package com.mrpepe.pengyou

import android.app.Activity
import android.app.Application
import android.content.Context
import android.database.CursorIndexOutOfBoundsException

class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null
        private var currentActivity: Activity? = null

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
