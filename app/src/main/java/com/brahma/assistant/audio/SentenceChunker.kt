package com.brahma.assistant.audio

class SentenceChunker {
    private val buffer = StringBuilder()
    private val sentenceDelimiters = charArrayOf('.', '!', '?', '\n', ';', ':')

    /**
     * Appends streaming token chunks and yields complete sentences as soon as punctuation is detected.
     */
    fun processChunk(token: String): List<String> {
        buffer.append(token)
        val readySentences = mutableListOf<String>()

        var searchIndex = 0
        while (searchIndex < buffer.length) {
            val char = buffer[searchIndex]
            if (sentenceDelimiters.contains(char)) {
                val sentence = buffer.substring(0, searchIndex + 1).trim()
                if (sentence.isNotBlank()) {
                    readySentences.add(sentence)
                }
                buffer.delete(0, searchIndex + 1)
                searchIndex = 0
            } else {
                searchIndex++
            }
        }

        return readySentences
    }

    fun flushRemaining(): String? {
        val remaining = buffer.toString().trim()
        buffer.clear()
        return if (remaining.isNotBlank()) remaining else null
    }
}
