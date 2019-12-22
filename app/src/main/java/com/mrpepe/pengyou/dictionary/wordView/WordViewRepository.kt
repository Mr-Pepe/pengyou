package com.mrpepe.pengyou.dictionary.wordView

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

class WordViewRepository(private val entryDao: EntryDAO, initEntry: Entry) {
    var entry : LiveData<Entry>
    var wordsContaining : LiveData<List<Entry>>

    init {
        entry = entryDao.getEntryById(initEntry.id!!)
        wordsContaining = entryDao.getWordsContaining(initEntry.simplified, "%${initEntry.simplified}%")
    }
}
