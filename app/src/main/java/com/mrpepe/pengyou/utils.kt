package com.mrpepe.pengyou

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity
import com.mrpepe.pengyou.dictionary.wordView.WordViewViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class DefinitionFormatter {

    fun formatDefinitions(entry: Entry, linkWords: Boolean, context: Context?, mode: ChineseMode): List<SpannableStringBuilder> {
        val rawDefinitions = entry.definitions
        val definitions = mutableListOf<SpannableStringBuilder>()

        return if (rawDefinitions.isBlank()) {
            definitions
        }
        else {
            rawDefinitions.split('/').forEach { rawDefinition ->
                definitions.add(formatDefinition(entry, rawDefinition, linkWords, context, mode))
            }

            definitions
        }
    }

    private fun formatDefinition(entry: Entry, rawDefinition: String, linkWords: Boolean, context: Context?, mode: ChineseMode) : SpannableStringBuilder {

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

                        if (mode == ChineseMode.SIMPLIFIED || mode == ChineseMode.BOTH) {
                            word = SpannableString(simplified)
                        }
                        else if (mode == ChineseMode.TRADITIONAL){
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
                                PinyinMode.MARKS
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

class WordLink(val entry: Entry,
               val context: Context,
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

        val entryDao : EntryDAO = CEDict.getDatabase(MainApplication.getContext()).entryDao()

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
                    val intent =
                        Intent(MainApplication.getCurrentActivity(), WordViewActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("entry", entries[0])
                    startActivity(context, intent, null)
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
        ds.color = Color.BLUE
        ds.isUnderlineText = false
    }
}

class PinyinConverter {
    fun getFormattedPinyin(pinyin: String, mode: PinyinMode): String {
        val result = pinyin.replace("u:", "ü")

        return when (mode) {
            PinyinMode.NUMBERS -> {
                return result.replace(",", ", ")
            }

            PinyinMode.MARKS -> {
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

enum class PinyinMode {
    NUMBERS,
    MARKS
}

class HeadwordFormatter {

    fun format(entry: Entry, mode: ChineseMode): SpannableStringBuilder {
        val headwordText = SpannableStringBuilder()
        val simplified = HeadwordFormatter().paintHeadword(entry.simplified, entry.pinyin)
        val traditional = HeadwordFormatter().paintHeadword(entry.traditional, entry.pinyin)

        if (mode == ChineseMode.SIMPLIFIED || mode == ChineseMode.BOTH)
            headwordText.append(simplified)
        if (mode == ChineseMode.BOTH)
            headwordText.append(" | ")
        if (mode == ChineseMode.BOTH || mode == ChineseMode.TRADITIONAL)
            headwordText.append(traditional)

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

                var color = R.color.notone

                if (pinyinSyllable.length > 1 &&
                    (Character.getNumericValue(pinyinSyllable.last()) in 1..5) &&
                    !headword[iHeadword].isDigit()) {

                    color = when (Character.getNumericValue(pinyinSyllable.last())) {
                        1 -> R.color.tone1
                        2 -> R.color.tone2
                        3 -> R.color.tone3
                        4 -> R.color.tone4
                        5 -> R.color.tone5
                        else -> R.color.notone
                    }
                }

                val foreGroundColor = ForegroundColorSpan(
                    ResourcesCompat.getColor(
                        MainApplication.getContext().resources,
                        color, null
                    )
                )

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


enum class ChineseMode {
    SIMPLIFIED,
    TRADITIONAL,
    BOTH
}


fun String.countSurrogatePairs() = withIndex().count {
    hasSurrogatePairAt(it.index)
}

// Returns the actual length of a string considering surrogate pairs
fun String.lengthSurrogate() = this.length - this.countSurrogatePairs()

class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: WordViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        pageViewModel = ViewModelProviders.of(this).get(WordViewViewModel::class.java).apply {
//            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_word_view, container, false)
//        val textView: TextView = root.findViewById(R.id.section_label)
//        pageViewModel.text.observe(this, Observer<String> {
//            textView.text = it
//        })
        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}

enum class UpdateSearchResultsMode {
    SNAPTOTOP,
    DONTSNAPTOTOP
}

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
