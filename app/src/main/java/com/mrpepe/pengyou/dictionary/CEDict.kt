package com.mrpepe.pengyou.dictionary

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import java.io.Serializable

@Database(entities= arrayOf(Entry::class, Permutation::class), version = 1)
abstract class CEDict : RoomDatabase() {
    abstract fun entryDao() : EntryDAO

    companion object {
        @Volatile
        private var INSTANCE: CEDict? = null

        fun getDatabase(context: Context): CEDict {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CEDict::class.java,
                    "cedict"
                ).createFromAsset("cedict.db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

@Entity(tableName = "entries")
data class Entry (
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "simplified") val simplified: String,
    @ColumnInfo(name = "traditional") val traditional: String,
    @ColumnInfo(name = "pinyin") val pinyin: String,
    @ColumnInfo(name= "priority") val priority: Int,
    @ColumnInfo(name = "word_length") val wordLength: Int,
    @ColumnInfo(name = "definitions") val definitions: String
) : Serializable

@Entity(tableName = "permutations", indices = arrayOf(Index(value = ["permutation"], name = "search_index")))
data class Permutation(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "entry_id") val wordID: Int,
    @ColumnInfo(name = "permutation") val definition: String
)

@Dao
interface EntryDAO {

    @Query("SELECT * FROM entries")
    fun getAllEntries(): List<Entry>

    @Query("SELECT * FROM permutations")
    fun getSearchIndex(): List<Permutation>

    @Query("SELECT * " +
                "FROM entries " +
                "WHERE id IN (" +
                    "SELECT DISTINCT entry_id " +
                    "FROM permutations " +
                    "WHERE " +
                        "permutation >= :lowerString AND " +
                        "permutation < :upperString " +
                    "LIMIT 1000)" +
                "ORDER BY word_length")
    fun findWords(lowerString: String, upperString: String) : LiveData<List<Entry>>

}