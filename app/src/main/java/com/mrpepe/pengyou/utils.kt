package com.mrpepe.pengyou

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.italic
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


fun extractDefinitions(entry: Entry, asList: Boolean, linkWords: Boolean, context: Context?) : SpannableStringBuilder {

    val rawDefinitions = entry.definitions

    val text = SpannableStringBuilder()

    if (rawDefinitions.isBlank()) {
        text.italic { append("No definition found, but this character might be used in other words.") }
    }
    else {
        var iDefinition = 1

        rawDefinitions.split('/').forEach {definition ->

            if (iDefinition == 1) {
                text.bold { append("1 ") }
            }
            else {
                if (asList)
                    text.append("\n")
                else
                    text.append(" ")

                text.bold { append("$iDefinition ") }
            }

            // Definition of measure words
            if ("CL:" in definition) {
                val measureWords = definition.replace("CL:", "").split(',')

                if (measureWords.size > 1)
                    text.append("Measure words: ")
                else
                    text.append("Measure word: ")

                measureWords.forEachIndexed { iMeasureWord, measureWord ->
                    val fields = measureWord.split('[')
                    val rawPinyin = fields[1].replace("]", "")
                    val pinyin = PinyinConverter().getFormattedPinyin(
                        rawPinyin,
                        PinyinConverter.PinyinMode.MARKS
                    )

                    var simplified = ""
                    var traditional = ""

                    val headword = SpannableString(
                        when (fields[0].split('|').size) {
                            1 -> fields[0].split('|')[0]
                            2 -> fields[0].split('|')[1]
                            else -> ""
                        }
                    )

                    val headwords = fields[0].split('|')

                    when (headwords.size) {
                        1 -> {
                            simplified = headwords[0]
                            traditional = headwords[0]
                        }
                        2 -> {
                            simplified = headwords[1]
                            traditional = headwords[0]
                        }
                    }

                    if (linkWords)
                        headword.setSpan(
                            WordLink(
                                entry,
                                context!!,
                                simplified,
                                traditional,
                                rawPinyin
                            ), 0, headword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                    text.append(headword)
                    text.append(" [$pinyin]")

                    if (iMeasureWord < measureWords.size - 1)
                        text.append(", ")
                }
            }
            else {
                text.append(definition)
            }


            iDefinition++
        }
    }

    return text
}

class WordLink(val entry: Entry,
               val context: Context,
               private val simplified: String,
               private val traditional: String,
               private val pinyin: String): ClickableSpan() {

    override fun onClick(widget: View) {


        val entryDao : EntryDAO = CEDict.getDatabase(MainApplication.getContext()).entryDao()

        val scope = MainScope()

        scope.launch {

            val entries = entryDao.getEntryBySimplifiedTraditionalPinyin(simplified, traditional, pinyin)

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



    enum class PinyinMode {
        NUMBERS,
        MARKS
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

    fun format(entry: Entry, mode: FormatMode): SpannableStringBuilder {
        val headwordText = SpannableStringBuilder()
        val simplified = HeadwordFormatter().paintHeadword(entry.simplified, entry.pinyin)
        val traditional = HeadwordFormatter().paintHeadword(entry.traditional, entry.pinyin)

        if (mode == FormatMode.SIMPLIFIED || mode == FormatMode.BOTH)
            headwordText.append(simplified)
        if (mode == FormatMode.BOTH)
            headwordText.append(" | ")
        if (mode == FormatMode.BOTH || mode == FormatMode.TRADITIONAL)
            headwordText.append(traditional)

        return headwordText
    }

    fun paintHeadword(headword: String, pinyin: String) : SpannableString {
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

    enum class FormatMode {
        SIMPLIFIED,
        TRADITIONAL,
        BOTH
    }
}

fun String.countSurrogatePairs() = withIndex().count {
    hasSurrogatePairAt(it.index)
}

// Returns the actual length of a string considering surrogate pairs
fun String.lengthSurrogate() = this.length - this.countSurrogatePairs()

