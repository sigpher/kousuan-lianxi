package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class ReviewSelectorTest {

    private fun problems(n: Int): List<WrongProblem> =
        (1..n).map {
            WrongProblem(ArithmeticQuestion(Operation.ADD, it, it, it + it), "0", it.toLong())
        }

    @Test
    fun pickUsesAllWhenCountBelowLimit() {
        val list = problems(50)
        assertEquals(list, ReviewSelector.pick(list, 100))
    }

    @Test
    fun pickUsesAllWhenCountEqualsLimit() {
        val list = problems(100)
        assertEquals(list, ReviewSelector.pick(list, 100))
    }

    @Test
    fun pickTakesRandomSampleWhenCountAboveLimit() {
        val list = problems(150)
        val picked = ReviewSelector.pick(list, 100, Random(7))
        assertEquals(100, picked.size)
        assertEquals("应无重复错题", 100, picked.distinct().size)
    }
}