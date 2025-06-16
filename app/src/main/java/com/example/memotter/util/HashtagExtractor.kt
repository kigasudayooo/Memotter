package com.example.memotter.util

object HashtagExtractor {
    private val hashtagRegex = Regex("#([\\w\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF]+)")

    fun extractHashtags(text: String): List<String> {
        return hashtagRegex.findAll(text)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    fun highlightHashtags(text: String): String {
        return hashtagRegex.replace(text) { matchResult ->
            "<font color='#1976D2'>#${matchResult.groupValues[1]}</font>"
        }
    }
}