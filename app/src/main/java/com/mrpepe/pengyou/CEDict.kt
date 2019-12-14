package com.mrpepe.pengyou

import androidx.room.*

@Database(entities= arrayOf(Entry::class, Definition::class, Permutation::class), version = 1)
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
    @ColumnInfo(name = "word_length") val wordLength: Int
)

@Entity(tableName = "search_index")
data class Permutation(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "entry_id") val wordID: Int,
    @ColumnInfo(name = "permutation") val definition: String
)

@Entity(tableName = "definitions")
data class Definition(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "entry_id") val wordID: Int,
    @ColumnInfo(name = "definition") val definition: String
)

@Dao
interface EntryDAO {
    @Query("SELECT * FROM definitions")
    fun getAllDefinitions(): List<Definition>

    @Query("SELECT * FROM entries")
    fun getAllEntries(): List<Entry>

    @Query("SELECT * FROM search_index")
    fun getSearchIndex(): List<Permutation>

    @Query("SELECT DISTINCT simplified FROM entries JOIN search_index ON search_index.permutation LIKE :input AND search_index.entry_id = entries.id")
    fun findWords(input: String) : List<String>

}