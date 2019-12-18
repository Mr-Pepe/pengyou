package com.mrpepe.pengyou.dictionary.ui.wordViewUI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mrpepe.pengyou.dictionary.Entry
import java.security.KeyStore

class WordViewViewModel : ViewModel() {
    lateinit var entry : Entry
}