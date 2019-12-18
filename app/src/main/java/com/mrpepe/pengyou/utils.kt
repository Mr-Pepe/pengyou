package com.mrpepe.pengyou

fun extractDefinitions(rawDefinitions: String) : String {
    val definitions = rawDefinitions.split('/')

    var text = ""
    var iDefinition = 1

    definitions.forEach {
        text += when (iDefinition) {
            1 -> "1. $it"
            else -> "\n$iDefinition. $it"
        }

        iDefinition++
    }

    return text
}