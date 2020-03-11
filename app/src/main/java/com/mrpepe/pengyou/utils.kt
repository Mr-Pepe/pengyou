package com.mrpepe.pengyou

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.mrpepe.pengyou.dictionary.AppDatabase
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import com.mrpepe.pengyou.dictionary.wordView.WordViewFragmentDirections
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class DefinitionFormatter {

    fun formatDefinitions(entry: Entry, linkWords: Boolean, context: Context?, view: View, chineseMode: String, pinyinMode: String): List<SpannableStringBuilder> {
        val rawDefinitions = entry.definitions
        val definitions = mutableListOf<SpannableStringBuilder>()

        return if (rawDefinitions.isBlank()) {
            definitions
        }
        else {
            rawDefinitions.split('/').forEachIndexed { iRawDefinition, rawDefinition ->

                definitions.add(
                    formatDefinition(
                        entry,
                        rawDefinition,
                        linkWords,
                        context,
                        view,
                        chineseMode,
                        pinyinMode
                    )
                )
            }
            definitions
        }
    }

    private fun formatDefinition(entry: Entry, rawDefinition: String, linkWords: Boolean, context: Context?, view: View, chineseMode: String, pinyinMode: String) : SpannableStringBuilder {

        val definition = SpannableStringBuilder()

        var chineseWord = StringBuilder()
        var pinyin = StringBuilder()
        var simplified: String
        var traditional = ""

        var inPinyin = false
        var inChinese = false


        if ('§' in rawDefinition || '[' in rawDefinition) {
            rawDefinition.forEachIndexed { iChar, char ->

                // Find Chinese links
                if (inChinese) {
                    if (char == '§') {
                        if (traditional.isBlank()) {
                            simplified = chineseWord.toString()
                            traditional = chineseWord.toString()
                        }
                        else {
                            simplified = chineseWord.toString()
                        }

                        var word = SpannableString("")

                        if (chineseMode == ChineseMode.simplified || chineseMode == ChineseMode.simplifiedTraditional) {
                            word = SpannableString(simplified)
                        }
                        else if (chineseMode == ChineseMode.traditional || chineseMode == ChineseMode.traditionalSimplified){
                            word = SpannableString(traditional)
                        }

                        // Try to find pinyin corresponding to this word
                        val tmpPinyin = StringBuilder()
                        var tmpInPinyin = false

                        for (i in iChar+1 until rawDefinition.length) {
                            if (rawDefinition[i] == ']' || rawDefinition[i] == '§') {
                                break
                            }
                            else if (tmpInPinyin) {
                                tmpPinyin.append(rawDefinition[i])
                            }
                            else if (rawDefinition[i] == '[') {
                                tmpInPinyin = true
                            }
                        }

                        if (linkWords)
                            word.setSpan(
                                WordLink(
                                    entry,
                                    context!!,
                                    view,
                                    simplified,
                                    traditional,
                                    tmpPinyin.toString()
                                ), 0, word.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                        definition.append(word)

                        simplified = ""
                        traditional = ""
                        chineseWord.clear()
                        inChinese = false
                    }
                    else if (char == '|') {
                        traditional = chineseWord.toString()
                        chineseWord.clear()
                    }
                    else {
                        chineseWord.append(char)
                    }
                }
                else if (char == '§'){
                    inChinese = true
                }
                // Find Pinyin
                else if (inPinyin) {
                    if (char == ']') {
                        definition.append(
                            PinyinConverter().getFormattedPinyin(
                                pinyin.toString(),
                                pinyinMode
                            )
                        )
                        definition.append(char)
                        inPinyin = false
                        pinyin.clear()
                    }
                    else {
                        pinyin.append(char)
                    }
                }
                else if (char == '[') {
                    inPinyin = true
                    if (definition.last() != ' ')
                        definition.append(' ')

                    definition.append(char)
                }
                else definition.append(char)


            }
        }
        else {
            definition.append(rawDefinition)
        }

        return definition
    }
}

fun WebView.runJavaScript(string: String) {
    this.evaluateJavascript(
                "javascript: " +
                        string, null
            )
}

class ChineseMode {
    companion object {
        val simplified = MainApplication.getContext().getString(R.string.chinese_mode_simplified)
        val traditional = MainApplication.getContext().getString(R.string.chinese_mode_traditional)
        val simplifiedTraditional = MainApplication.getContext().getString(R.string.chinese_mode_simplified_traditional)
        val traditionalSimplified = MainApplication.getContext().getString(R.string.chinese_mode_traditional_simplified)
    }
}

class PinyinMode {
    companion object {
        val marks = MainApplication.getContext().getString(R.string.pinyin_notation_marks)
        val numbers = MainApplication.getContext().getString(R.string.pinyin_notation_numbers)
    }
}

class WordLink(val entry: Entry,
               val context: Context,
               val view: View,
               private val simplified: String,
               private val traditional: String,
               private val pinyin: String): ClickableSpan() {

    companion object {
        var lastTimeClicked : Long = 0
    }

    override fun onClick(widget: View) {

        if (SystemClock.elapsedRealtime() - lastTimeClicked < 500)
            return

        lastTimeClicked = SystemClock.elapsedRealtime()

        val entryDao : EntryDAO = AppDatabase.getDatabase(MainApplication.getContext()).entryDao()

        val scope = MainScope()

        scope.launch {

            var entries = listOf<Entry>()

            // The search order is:
            // 1. Search by exact simplified, exact traditional, case sensitive pinyin
            // 2. Search by exact simplified, exact traditional, case insensitive pinyin
            // 3. Search by exact simplified, exact traditional
            // 4. Search by simplified, traditional

            if (pinyin.isNotBlank()) {
                // The database query is case insensitive, so the result is checked afterwards
                val tmpEntries =
                    entryDao.getEntryBySimplifiedTraditionalPinyin(simplified, traditional, pinyin)

                if (tmpEntries.size > 1) {
                    for (tmpEntry in tmpEntries) {
                        if (tmpEntry.simplified == simplified &&
                            tmpEntry.traditional == traditional &&
                            tmpEntry.pinyin == pinyin) {

                            entries = listOf(tmpEntry)
                            break
                        }
                    }

                }
                if (entries.isEmpty()) {
                    entries = tmpEntries
                }
            }

            // If no pinyin was provided or no exact match found
            if (entries.isEmpty())
                entries = entryDao.getEntryBySimplifiedTraditional(simplified, traditional)

            // If still nothing found look for entries containing simplified or traditional
            if (entries.isEmpty())
                entries = entryDao.searchChineseBySimplifiedTraditional(simplified,
                                                          simplified + 'z',
                                                                        traditional,
                                                         traditional + 'z')

            when (entries.size) {
                0 -> Toast.makeText(
                    MainApplication.getContext(),
                    "Couldn't find linked word.",
                    Toast.LENGTH_SHORT
                ).show()
                1 -> {
                    Navigation.findNavController(view).navigate(WordViewFragmentDirections.globalOpenWordViewAction(entries[0]))
                }
                else -> Toast
                        .makeText(
                            MainApplication.getContext(),
                            "Too many entries found for the linked word.",
                            Toast.LENGTH_SHORT
                        ).show()

            }
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}

class PinyinConverter {
    fun getFormattedPinyin(pinyin: String, mode: String): String {
        val result = pinyin.replace("u:", "ü")

        return when (mode) {
            PinyinMode.numbers -> {
                return result.replace(",", ", ")
            }

            PinyinMode.marks -> {
                val syllables = result.split(" ").toMutableList()

                syllables.forEachIndexed { iSyllable, syllable ->

                    val tone = Character.getNumericValue(syllable.last())

                    // Leave the syllable as is if it is alphanumeric, has only one character
                    // or the last character is not a digit in the range 1..5
                    if (tone in 1..5 &&
                        syllable.length > 1 &&
                        !syllable.matches("-?\\d+(\\.\\d+)?".toRegex())) {

                        var iVowel = -1
                        var iChar = -1

                        // https://en.wikipedia.org/wiki/Pinyin#Rules_for_placing_the_tone_mark
                        for (char in syllable) {
                            iChar++

                            if (char.toLowerCase() in listOf('a', 'e')) {
                                iVowel = iChar
                                break
                            }
                            else if (char.toLowerCase() == 'o') {
                                iVowel = iChar
                                break
                            }
                            else if (char.toLowerCase() in listOf('i', 'u', 'ü')) {
                                iVowel = iChar
                            }

                        }

                        val out = StringBuilder(syllable)
                        if (iVowel != -1) {
                            val substitute: Char = substitutions.getValue(syllable[iVowel])[tone-1]
                            out.setCharAt(iVowel, substitute)
                        }

                        // Remove trailing tone number
                        out.deleteCharAt(out.length-1)

                        syllables[iSyllable] = out.toString()

                    }
                }

                syllables.joinToString("").replace(",", ", ")

            }

            else -> ""


        }
    }

    private val substitutions = mapOf(
        'a' to listOf('ā', 'á', 'ǎ', 'à', 'a'),
        'e' to listOf('ē', 'é', 'ě', 'è', 'e'),
        'i' to listOf('ī', 'í', 'ǐ', 'ì', 'i'),
        'o' to listOf('ō', 'ó', 'ǒ', 'ò', 'o'),
        'u' to listOf('ū', 'ú', 'ǔ', 'ù', 'u'),
        'ü' to listOf('ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'),

        'A' to listOf('Ā', 'Á', 'Ǎ', 'À', 'A'),
        'E' to listOf('Ē', 'É', 'Ě', 'È', 'E'),
        'I' to listOf('Ī', 'Í', 'Ĭ', 'Ì', 'I'),
        'O' to listOf('Ō', 'Ó', 'Ǒ', 'Ò', 'O'),
        'U' to listOf('Ū', 'Ú', 'Ǔ', 'Ù', 'U'),
        'Ü' to listOf('Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ü')
    )
}

class HeadwordFormatter {

    fun format(entry: Entry, mode: String): SpannableStringBuilder {
        val headwordText = SpannableStringBuilder()
        val simplified = HeadwordFormatter().paintHeadword(entry.simplified, entry.pinyin)
        val traditional = HeadwordFormatter().paintHeadword(entry.traditional, entry.pinyin)

        if (mode == ChineseMode.simplified || mode == ChineseMode.simplifiedTraditional)
            headwordText.append(simplified)
        else if (mode == ChineseMode.traditional || mode == ChineseMode.traditionalSimplified)
            headwordText.append(traditional)
        if (mode == ChineseMode.simplifiedTraditional)
            headwordText.append(" | ").append(traditional)
        else if (mode == ChineseMode.traditionalSimplified)
            headwordText.append(" | ").append(simplified)

        return headwordText
    }

    private fun paintHeadword(headword: String, pinyin: String) : SpannableString {
        val syllables = pinyin.split(' ')

        val output = SpannableString(headword)

        // Kotlin uses UTF-16 encoded strings which have surrogate pairs that need extra handling
        if (syllables.size == headword.lengthSurrogate()) {
            var iSyllable = 0
            var iHeadword = 0

            while (iHeadword < headword.length) {
                val spanStart = iHeadword
                val spanEnd = iHeadword + when (headword.hasSurrogatePairAt(iHeadword)) {
                    true -> 2
                    false -> 1
                }

                val pinyinSyllable = syllables[iSyllable]

                val noToneColor =
                    when (MainApplication.homeActivity.isNightMode()) {
                        true -> MainApplication.getContext().getColor(R.color.general_color_dark_theme)
                        else -> MainApplication.getContext().getColor(R.color.general_color_light_theme)
                    }

                var color = noToneColor
                val preferences = PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext())

                if (pinyinSyllable.length > 1 &&
                    (Character.getNumericValue(pinyinSyllable.last()) in 1..5) &&
                    !headword[iHeadword].isDigit()) {

                    color = when (Character.getNumericValue(pinyinSyllable.last())) {
                        1 -> preferences.getInt("first_tone_color", MainApplication.getContext().getColor(R.color.tone1_default_color))
                        2 -> preferences.getInt("second_tone_color", MainApplication.getContext().getColor(R.color.tone2_default_color))
                        3 -> preferences.getInt("third_tone_color", MainApplication.getContext().getColor(R.color.tone3_default_color))
                        4 -> preferences.getInt("fourth_tone_color", MainApplication.getContext().getColor(R.color.tone4_default_color))
                        5 -> preferences.getInt("fifth_tone_color", MainApplication.getContext().getColor(R.color.tone5_default_color))
                        else -> noToneColor
                    }
                }

                val foreGroundColor = ForegroundColorSpan(color)

                output.setSpan(
                    foreGroundColor,
                    spanStart,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                iSyllable += 1
                iHeadword = spanEnd
            }
        }

        return output
    }

}

fun getControlEnabledColor(): Int {
    return when (MainApplication.homeActivity.isNightMode()) {
        true -> MainApplication.getContext().getColor(R.color.control_color_enabled_dark_theme)
        false -> MainApplication.getContext().getColor(R.color.control_color_enabled_light_theme)
    }
}

fun getControlDisabledColor(): Int {
    return when (MainApplication.homeActivity.isNightMode()) {
        true -> MainApplication.getContext().getColor(R.color.control_color_disabled_dark_theme)
        false -> MainApplication.getContext().getColor(R.color.control_color_disabled_light_theme)
    }
}

fun String.countSurrogatePairs() = withIndex().count {
    hasSurrogatePairAt(it.index)
}

// Returns the actual length of a string considering surrogate pairs
fun String.lengthSurrogate() = this.length - this.countSurrogatePairs()

class CustomViewPager(context: Context, attributeSet: AttributeSet): ViewPager(context, attributeSet) {
    private var pagingEnabled = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return when (this.pagingEnabled) {
            true -> super.onTouchEvent(ev)
            false -> false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return when (this.pagingEnabled) {
            true -> return super.onInterceptTouchEvent(ev)
            false -> false
        }
    }

    fun togglePagingEnabled() {
        this.pagingEnabled = !this.pagingEnabled
    }

}

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard(view!!)
}

fun Activity.hideKeyboard() {
    hideKeyboard(if (currentFocus == null) View(this) else currentFocus!!)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

object SearchHistory {

    val searchPreferences = MainApplication.getContext().getSharedPreferences(MainApplication.getContext().getString(R.string.search_history), Context.MODE_PRIVATE)

    init {
        val searchHistoryIDs = searchPreferences.getString("search_history", "")!!.split(',').toMutableList()
        searchHistoryIDs.remove("")
        MainScope().launch {
            with(searchPreferences.edit()) {
                putString("search_history", searchHistoryIDs.joinToString(","))
                commit()
            }
        }
    }

    fun clear() {
        val searchHistoryIDs = mutableListOf<String>()
        MainScope().launch {
            with(searchPreferences.edit()) {
                putString("search_history", searchHistoryIDs.joinToString(","))
                commit()
            }
        }
    }

    fun getHistoryIds(): List<String> {
        val ids = searchPreferences.getString("search_history", "")!!.split(',')

        return when (ids[0] == "") {
            true -> listOf()
            false -> ids
        }
    }

    fun addToHistory(id: String) {
        val maxLengthHistory = MainApplication.MAX_HISTORY_ENTRIES

        var searchHistoryIDs = searchPreferences.getString("search_history", "")!!.split(',').toMutableList()


        if (searchHistoryIDs.size >= maxLengthHistory) {
            searchHistoryIDs = searchHistoryIDs.slice(1 until maxLengthHistory).toMutableList()
        }

        searchHistoryIDs.remove(id)
        searchHistoryIDs.add(id)
        searchHistoryIDs.remove("")


        MainScope().launch {
            with(searchPreferences.edit()) {
                putString("search_history", searchHistoryIDs.joinToString(","))
                commit()
            }
        }
    }
}
