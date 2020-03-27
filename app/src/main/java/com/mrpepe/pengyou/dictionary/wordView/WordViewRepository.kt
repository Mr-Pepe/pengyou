package com.mrpepe.pengyou.dictionary.wordView

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import com.mrpepe.pengyou.dictionary.StrokeOrder

class WordViewRepository(private val entryDao: EntryDAO, initEntry: Entry) {
    var entry : LiveData<Entry> = entryDao.getEntryById(initEntry.id!!)
    var wordsContaining : LiveData<List<Entry>> =
        entryDao.getWordsContaining(initEntry.simplified, "%${initEntry.simplified}%")

    var decompositions : MutableList<Decomposition> = mutableListOf()
    var strokeOrders : MutableList<StrokeOrder> = mutableListOf()


    suspend fun decompose(word: String) {
        word.forEach { character ->
            val decompositionList = entryDao.getDecomposition(character.toString())

            if (decompositionList.isNotEmpty()) {

                val componentChars = decompositionList[0].components.split(',')
                val componentList = mutableListOf<Component>()

                componentChars.forEach { componentChar ->
                    val tmpEntry = entryDao.getEntryBySimplified(componentChar)

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
        var iCharacter = 0
        var strokeOrderList: List<StrokeOrder>
        var character = ""

        while (iCharacter < word.length) {
            if (word.hasSurrogatePairAt(iCharacter)) {
                character += word[iCharacter]
                character += word[iCharacter+1]
                iCharacter += 2
            }
            else {
                character = word[iCharacter].toString()
                iCharacter += 1
            }

            strokeOrderList = entryDao.getStrokeOrder(character)

            if (strokeOrderList.isNotEmpty()) {
                strokeOrders.add(strokeOrderList[0])
            } else {
                strokeOrders.add(StrokeOrder(-1, character, ""))
            }
        }
    }
}

class Decomposition(val character : String,
                    val decompositionType : String,
                    val components : List<Component>)

class Component(val character : String,
                val meaning : String
                )
