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
                        "permutation < :upperString)" +
                "ORDER BY word_length")
    fun findWords(lowerString: String, upperString: String) : List<Entry>

}