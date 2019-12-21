package com.mrpepe.pengyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mrpepe.pengyou.dictionary.searchView.SearchViewActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    fun openDict(view: View){
        val intent = Intent(this, SearchViewActivity::class.java)
        startActivity(intent)
    }
}
