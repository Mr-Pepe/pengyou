package com.mrpepe.pengyou

import android.content.res.Resources
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.core.text.set
import com.mrpepe.pengyou.MainApplication

fun extractDefinitions(rawDefinitions: String, asList: Boolean) : SpannableStringBuilder {

    var text = SpannableStringBuilder()

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

class PinyinConverter() {
    fun getFormattedPinyin(pinyin: String, mode: PinyinMode): String {
        var result = pinyin.replace("u:", "ü")

        return when (mode) {
            PinyinMode.NUMBERS -> {
                return result.replace(",", ", ")
            }

            PinyinMode.MARKS -> {
                var syllables = result.split(" ").toMutableList()

                syllables.forEachIndexed { iSyllable, syllable ->

                    var tone = Character.getNumericValue(syllable.last())

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

                        var out = StringBuilder(syllable)
                        if (iVowel != -1) {
                            var substitute: Char = substitions.getValue(syllable[iVowel])[tone-1]
                            out.setCharAt(iVowel, substitute)
                        }

                        // Remove trailing tone number
                        out.deleteCharAt(out.length-1)

                        syllables[iSyllable] = out.toString()

                    }
                    else {
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
        'a' to listOf<Char>('ā', 'á', 'ǎ', 'à', 'a'),
        'e' to listOf<Char>('ē', 'é', 'ě', 'è', 'e'),
        'i' to listOf<Char>('ī', 'í', 'ǐ', 'ì', 'i'),
        'o' to listOf<Char>('ō', 'ó', 'ǒ', 'ò', 'o'),
        'u' to listOf<Char>('ū', 'ú', 'ǔ', 'ù', 'u'),
        'ü' to listOf<Char>('ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'),

        'A' to listOf<Char>('Ā', 'Á', 'Ǎ', 'À', 'A'),
        'E' to listOf<Char>('Ē', 'É', 'Ě', 'È', 'E'),
        'I' to listOf<Char>('Ī', 'Í', 'Ĭ', 'Ì', 'I'),
        'O' to listOf<Char>('Ō', 'Ó', 'Ǒ', 'Ò', 'O'),
        'U' to listOf<Char>('Ū', 'Ú', 'Ǔ', 'Ù', 'U'),
        'Ü' to listOf<Char>('Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ü')
    )
}

class HeadWordPainter() {
    fun paintHeadword(headword: String, pinyin: String) : SpannableString {
        var syllables = pinyin.split(' ')

        var output = SpannableString(headword)

        headword.forEachIndexed { i_Headword, character ->
            var syllable = syllables[i_Headword]

            var color =  R.color.notone

            if (syllable.length > 1 &&
                (Character.getNumericValue(syllable.last()) in 1..5) &&
                !(Character.getNumericValue(character) in 0..9)) {

                color = when (Character.getNumericValue(syllable.last())) {
                    1 -> R.color.tone1
                    2 -> R.color.tone2
                    3 -> R.color.tone3
                    4 -> R.color.tone4
                    5 -> R.color.tone5
                    else -> R.color.notone
                }
            }

            var foreGroundColor = ForegroundColorSpan(ResourcesCompat.getColor(
                                                            MainApplication.applicationContext().resources,
                                                            color, null))

            output.setSpan(foreGroundColor, i_Headword, i_Headword+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return output
    }
}
