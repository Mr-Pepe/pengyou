package com.mrpepe.pengyou

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.italic

fun extractDefinitions(rawDefinitions: String, asList: Boolean) : SpannableStringBuilder {

    val text = SpannableStringBuilder()

    if (rawDefinitions.isBlank()) {
        text.italic { append("No definition found, but this character might be used in other words.") }
    }
    else {
        val definitions = rawDefinitions.split('/')
        var iDefinition = 1

        definitions.forEach {
            when (iDefinition) {
                1 -> text.bold { append("1") }.append(" $it ")

                else -> {
                    if (asList)
                        text.append("\n")

                    text.bold { append("$iDefinition") }.append(" $it ")
                }
            }

            iDefinition++
        }
    }

    return text
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
                            val substitute: Char = substitions.getValue(syllable[iVowel])[tone-1]
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

    private val substitions = mapOf(
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

class HeadWordPainter {
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
                        MainApplication.applicationContext().resources,
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

fun String.countSurrogatePairs() = withIndex().count {
    hasSurrogatePairAt(it.index)
}

// Returns the actual length of a string considering surrogate pairs
fun String.lengthSurrogate() = this.length - this.countSurrogatePairs()

