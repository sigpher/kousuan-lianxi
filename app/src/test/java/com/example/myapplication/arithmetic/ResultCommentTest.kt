package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ResultCommentTest {

    @Test
    fun perfectRoundGetsFiveStars() {
        assertEquals(5, ResultComment.forRound(10, 10).stars)
    }

    @Test
    fun nearPerfectionGetsFourStars() {
        assertEquals(4, ResultComment.forRound(10, 9).stars)
    }

    @Test
    fun lowScoreGetsOneStar() {
        assertEquals(1, ResultComment.forRound(10, 1).stars)
    }

    @Test
    fun zeroTotalGetsOneStar() {
        assertEquals(1, ResultComment.forRound(0, 0).stars)
    }

    @Test
    fun titleAndCommentAreNeverEmpty() {
        repeat(100) {
            val c = ResultComment.forRound(50, 42, Random(it))
            assertTrue(c.title.isNotBlank())
            assertTrue(c.comment.isNotBlank())
        }
    }

    @Test
    fun randomDrawIsReproducibleWithSeed() {
        val a = ResultComment.forRound(50, 50, Random(42))
        val b = ResultComment.forRound(50, 50, Random(42))
        assertEquals(a, b)
    }
}