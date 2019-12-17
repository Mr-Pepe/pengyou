package com.mrpepe.pengyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mrpepe.pengyou.dictionary.DictionaryActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    fun openDict(view: View){
        val intent = Intent(this, DictionaryActivity::class.java)
        startActivity(intent)
    }
}
