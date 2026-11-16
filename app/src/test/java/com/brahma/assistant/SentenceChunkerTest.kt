package com.brahma.assistant

import com.brahma.assistant.audio.SentenceChunker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SentenceChunkerTest {

    @Test
    fun testChunkerSplitsSentencesOnPunctuation() {
        val chunker = SentenceChunker()

        val tokens1 = chunker.processChunk("Hello there")
        assertEquals(0, tokens1.size)

        val tokens2 = chunker.processChunk("! How are you")
        assertEquals(1, tokens2.size)
        assertEquals("Hello there!", tokens2[0])

        val tokens3 = chunker.processChunk(" today? I am fine.")
        assertEquals(2, tokens3.size)
        assertEquals("How are you today?", tokens3[0])
        assertEquals("I am fine.", tokens3[1])

        assertNull(chunker.flushRemaining())
    }

    @Test
    fun testChunkerFlushRemaining() {
        val chunker = SentenceChunker()
        chunker.processChunk("Unfinished sentence fragment")
        val remaining = chunker.flushRemaining()
        assertEquals("Unfinished sentence fragment", remaining)
    }
}
