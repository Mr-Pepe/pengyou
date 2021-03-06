package com.mrpepe.pengyou.dictionary

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.mrpepe.pengyou.MainApplication
import java.io.Serializable

@Database(entities= [Entry::class, EntryFts::class, Permutation::class, DbDecomposition::class,
    StrokeOrder::class, TraditionalToSimplifiedCharacters::class, TraditionalToSimplifiedPhrases::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao() : EntryDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase"
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
    @ColumnInfo(name= "priority") val priority: Float,
    @ColumnInfo(name = "hsk") val hsk: Int,
    @ColumnInfo(name = "word_length") val wordLength: Int,
    @ColumnInfo(name = "pinyin_length") val pinyinLength: Int,
    @ColumnInfo(name = "definitions") val definitions: String
) : Serializable

@Fts4(contentEntity = Entry::class)
@Entity(tableName = "entriesFts")
data class EntryFts(
    val definitions : String
)

@Entity(tableName = "permutations", indices = [Index(value = ["permutation"], name = "search_index")])
data class Permutation(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "entry_id") val wordID: Int,
    @ColumnInfo(name = "permutation") val definition: String
)

@Entity(tableName = "trad_to_simpl_characters")
data class TraditionalToSimplifiedCharacters(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "traditional") val traditional: String,
    @ColumnInfo(name = "simplified") val simplified: String
)

@Entity(tableName = "trad_to_simpl_phrases")
data class TraditionalToSimplifiedPhrases(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "traditional") val traditional: String,
    @ColumnInfo(name = "simplified") val simplified: String
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

    @Query("SELECT * FROM entries WHERE id=:id")
    suspend fun getEntryByIdAsync(id: Int) : Entry

    @Query("SELECT * " +
                "FROM entries " +
                "WHERE " +
                    "simplified LIKE :wildcardQuery " +
                    "AND " +
                    "simplified != :query ")
    fun getWordsContaining(query: String, wildcardQuery: String) : LiveData<List<Entry>>

    @Query("SELECT * " +
                "FROM entries " +
                "WHERE id IN (" +
                    "SELECT DISTINCT entry_id " +
                    "FROM permutations " +
                    "WHERE " +
                        "permutation >= :lowerString AND " +
                        "permutation < :upperString " +
                    "LIMIT " + MainApplication.MAX_SEARCH_RESULTS + ") ")
    fun searchInDictByChinese(lowerString: String, upperString: String) : LiveData<List<Entry>>

    @Query("SELECT * " +
            "FROM entries " +
            "WHERE " +
                "traditional >= :lowerString AND " +
                "traditional < :upperString " +
            "LIMIT " + MainApplication.MAX_SEARCH_RESULTS)
    fun searchInDictByTraditional(lowerString: String, upperString: String) : LiveData<List<Entry>>

    @Query("SELECT * FROM decompositions WHERE character = :query")
    suspend fun getDecomposition(query: String): List<DbDecomposition>

    @Query("SELECT * FROM stroke_orders WHERE character = :query")
    suspend fun getStrokeOrder(query: String): List<StrokeOrder>

    @Query("SELECT * FROM entries WHERE simplified = :query")
    suspend fun getEntryBySimplified(query: String): List<Entry>

    @Query("SELECT * FROM entries WHERE simplified = :simplified AND traditional = :traditional AND pinyin = :pinyin")
    suspend fun getEntryBySimplifiedTraditionalPinyin(simplified: String,
                                                      traditional: String,
                                                      pinyin: String): List<Entry>

    @Query("SELECT * FROM entries WHERE simplified = :simplified AND traditional = :traditional")
    suspend fun getEntryBySimplifiedTraditional(simplified: String,
                                                traditional: String): List<Entry>

    @Query("SELECT simplified FROM trad_to_simpl_characters WHERE traditional = :query")
    suspend fun getTraditionalToSimplifiedCharacters(query: String): List<String>

    @Query("SELECT simplified FROM trad_to_simpl_phrases WHERE traditional = :query")
    suspend fun getTraditionalToSimplifiedPhrases(query: String): List<String>

    @Query("SELECT * " +
                "FROM entries " +
                "WHERE " +
                    "simplified >= :lowerSimplified AND " +
                    "simplified < :upperSimplified " +
                    " OR " +
                    "traditional >= :lowerTraditional AND " +
                    "traditional < :upperTraditional ")
    suspend fun searchChineseBySimplifiedTraditional(lowerSimplified: String,
                                                     upperSimplified: String,
                                                     lowerTraditional: String,
                                                     upperTraditional: String) : List<Entry>

    @Query("SELECT * " +
                   "FROM entries " +
                        "JOIN entriesFts " +
                            "ON entries.id == entriesFts.docId " +
                   "WHERE entriesFts.definitions MATCH :query")
    fun searchInDictByEnglish(query: String): LiveData<List<Entry>>
}
