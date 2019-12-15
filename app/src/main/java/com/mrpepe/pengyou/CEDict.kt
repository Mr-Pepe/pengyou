package com.mrpepe.pengyou

import androidx.room.*

@Database(entities= arrayOf(Entry::class, Permutation::class), version = 1)
abstract class CEDict : RoomDatabase() {
    abstract fun entryDao() : EntryDAO
}

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "simplified") val simplified: String,
    @ColumnInfo(name = "traditional") val traditional: String,
    @ColumnInfo(name = "pinyin") val pinyin: String,
    @ColumnInfo(name= "priority") val priority: Int,
    @ColumnInfo(name = "word_length") val wordLength: Int,
    @ColumnInfo(name = "definitions") val definitions: String
)

@Entity(tableName = "permutations")
data class Permutation(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "entry_id") val wordID: Int,
    @ColumnInfo(name = "permutation", index = true) val definition: String
)

@Dao
interface EntryDAO {

    @Query("SELECT * FROM entries")
    fun getAllEntries(): List<Entry>

    @Query("SELECT * FROM permutations")
    fun getSearchIndex(): List<Permutation>

    @Query("SELECT DISTINCT * FROM entries JOIN permutations ON permutations.permutation >= :lowerString AND permutations.permutation < :upperString  AND permutations.entry_id = entries.id")
    fun findWords(lowerString: String, upperString: String) : List<Entry>

}