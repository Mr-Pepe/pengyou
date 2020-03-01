package com.mrpepe.pengyou.dictionary.wordView

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class WordViewRepository(private val entryDao: EntryDAO, val initEntry: Entry) {
    var entry : LiveData<Entry>
    var wordsContaining : LiveData<List<Entry>>
    var decompositions : MutableList<Decomposition> = mutableListOf()
    var strokeOrders : MutableList<String> = mutableListOf()


    init {
        entry = entryDao.getEntryById(initEntry.id!!)
        wordsContaining = entryDao.getWordsContaining(initEntry.simplified, "%${initEntry.simplified}%")
    }

    suspend fun decompose(word: String) {
        word.forEach { character ->
            var decompositionList = entryDao.getDecomposition(character.toString())

            if (decompositionList.isNotEmpty()) {

                var componentChars = decompositionList[0].components.split(',')
                var componentList = mutableListOf<Component>()

                componentChars.forEach { componentChar ->
                    var tmpEntry = entryDao.getEntryBySimplified(componentChar)

                    if (tmpEntry.isNotEmpty()) {
                        componentList.add(Component(componentChar, tmpEntry[0].definitions))
                    } else {
                        componentList.add(Component(componentChar, ""))
                    }
                }

                decompositions.add(
                    Decomposition(
                        character.toString(),
                        decompositionList[0].decompositionType,
                        componentList
                    )
                )
            } else {
                decompositions.add(
                    Decomposition(
                        character.toString(),
                        "",
                        listOf()
                    )
                )
            }
        }
    }

    suspend fun getStrokeOrder(word: String) {
        word.forEach { character ->
            val strokeOrderList = entryDao.getStrokeOrder(character.toString())

            if (strokeOrderList.isNotEmpty()) {
                strokeOrders.add(strokeOrderList[0].json)
            } else {
                strokeOrders.add("")
            }
        }
    }
}

class Decomposition(val character : String,
                    val decompositionType : String,
                    val components : List<Component>) {

//        lateinit var childDecompositions : MutableList<Decomposition>
}

class Component(val character : String,
                val meaning : String
                )
