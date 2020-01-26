package com.mrpepe.pengyou

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        MainApplication.setCurrentActivity(this)
    }

    override fun onPause() {
        clearReferences()
        super.onPause()
    }

    override fun onDestroy() {
        clearReferences()
        super.onDestroy()
    }

    private fun clearReferences() {
        if (this == MainApplication.getCurrentActivity())
            MainApplication.setCurrentActivity(null)
    }
}
