package com.mrpepe.pengyou.dictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mrpepe.pengyou.dictionary.Entry

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var searchResults : MutableLiveData<List<Entry>>
    lateinit var entryDao: EntryDAO
    lateinit var db : RoomDatabase

    fun init() {

        val db = Room.databaseBuilder(getApplication<Application>().applicationContext,
                                        CEDict::class.java,
                                    "cedict")
            .allowMainThreadQueries()
            .createFromAsset("cedict.db")
            .build()

        entryDao = db.entryDao()

        searchResults = MutableLiveData()
        searchResults.value = listOf<Entry>()
    }

    fun searchFor(query: String) {
        searchResults.setValue(entryDao.findWords(query, query+'z'))
    }

    fun resetSearchResults() {
        searchResults.setValue(listOf<Entry>())
    }
}