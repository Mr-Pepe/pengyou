package com.mrpepe.pengyou.dictionary

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable

@Database(entities= arrayOf(Entry::class,
                            Permutation::class,
                            DbDecomposition::class,
                            StrokeOrder::class), version = 1)
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
                ).createFromAsset("data.db")
//                    .allowMainThreadQueries()
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
    @ColumnInfo(name = "hsk") val hsk: Int,
    @ColumnInfo(name = "word_length") val wordLength: Int,
    @ColumnInfo(name = "definitions") val definitions: String
) : Serializable

@Entity(tableName = "permutations", indices = arrayOf(Index(value = ["permutation"], name = "search_index")))
data class Permutation(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "entry_id") val wordID: Int,
    @ColumnInfo(name = "permutation") val definition: String
)

@Entity(tableName = "decompositions")
data class DbDecomposition (
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "character") val character: String,
    @ColumnInfo(name = "decomposition_type") val decompositionType: String,
    @ColumnInfo(name = "components") val components: String
)

@Entity(tableName = "stroke_orders")
data class StrokeOrder (
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "character") val character: String,
    @ColumnInfo(name = "json") val json: String
)

@Dao
interface EntryDAO {

    @Query("SELECT * FROM entries WHERE id=:id")
    fun getEntryById(id: Int) : LiveData<Entry>

    @Query("SELECT * " +
                "FROM entries " +
                "WHERE " +
                    "simplified LIKE :wildcardQuery " +
                    "AND " +
                    "simplified != :query " +
                "ORDER BY word_length")
    fun getWordsContaining(query: String, wildcardQuery: String) : LiveData<List<Entry>>

    @Query("SELECT * " +
                "FROM entries " +
                "WHERE id IN (" +
                    "SELECT DISTINCT entry_id " +
                    "FROM permutations " +
                    "WHERE " +
                        "permutation >= :lowerString AND " +
                        "permutation < :upperString " +
                    "LIMIT 1000)" +
                "ORDER BY word_length, hsk")
    fun findWords(lowerString: String, upperString: String) : LiveData<List<Entry>>

    @Query("SELECT * FROM decompositions WHERE character = :query")
    suspend fun getDecomposition(query: String): List<DbDecomposition>

    @Query("SELECT * FROM stroke_orders WHERE character = :query")
    suspend fun getStrokeOrder(query: String): List<StrokeOrder>

    @Query("SELECT * FROM entries WHERE simplified = :query")
    suspend fun getEntryBySimplified(query: String): List<Entry>
}
