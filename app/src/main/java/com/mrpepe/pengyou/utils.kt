package com.mrpepe.pengyou

import android.text.SpannableStringBuilder
import androidx.core.text.bold

fun extractDefinitions(rawDefinitions: String, asList: Boolean) : SpannableStringBuilder {
    val definitions = rawDefinitions.split('/')

    var text = SpannableStringBuilder()
    var iDefinition = 1

    definitions.forEach {
        when (iDefinition) {
            1 -> text.bold{ append("1") }.append(" $it ")

            else -> {
                if (asList)
                    text.append("\n")

                text.bold { append("$iDefinition") }.append(" $it ")
            }
        }

        iDefinition++
    }

    return text
}
