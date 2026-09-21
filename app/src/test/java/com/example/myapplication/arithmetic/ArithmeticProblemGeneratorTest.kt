package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArithmeticProblemGeneratorTest {

    private val generator = ArithmeticProblemGenerator()

    private fun assertCorrect(q: ArithmeticQuestion) {
        assertEquals("${q.text} 答案错误", q.answer, q.expected)
    }

    @Test
    fun addition_bothOperandsAtMost100() {
        val list = generator.generate(Operation.ADD, 1000)
        for (q in list) {
            assertCorrect(q)
            assertTrue("加数 ${q.operandA} 超过 100", q.operandA in 1..100)
            assertTrue("加数 ${q.operandB} 超过 100", q.operandB in 1..100)
            assertTrue("和 $q 超出 100", q.answer <= 100)
        }
    }

    @Test
    fun subtraction_bothOperandsAtMost100() {
        val list = generator.generate(Operation.SUB, 1000)
        for (q in list) {
            assertCorrect(q)
            assertTrue("被减数 ${q.operandA} 超过 100", q.operandA in 1..100)
            assertTrue("减数 ${q.operandB} 超过 100", q.operandB in 1..100)
            assertTrue(q.answer >= 0)
        }
    }

    @Test
    fun multiplication_isCorrectAndRanges() {
        val list = generator.generate(Operation.MUL, 1000)
        for (q in list) {
            assertCorrect(q)
            assertTrue("乘数 %o 应为 1..20", q.operandA in 1..20)
            assertTrue("乘数 %o 应为 1..10", q.operandB in 1..10)
        }
    }

    @Test
    fun division_isExact() {
        val list = generator.generate(Operation.DIV, 1000)
        for (q in list) {
            assertCorrect(q)
            assertTrue("被除数 ${q.operandA} 不能整除 ${q.operandB}", q.operandA % q.operandB == 0)
            assertTrue(q.operandB in 2..9)
            assertTrue(q.answer in 1..9)
        }
    }

    @Test
    fun mixed_coversAllOperations() {
        val list = generator.generateMixed(1000)
        assertEquals(1000, list.size)
        for (q in list) assertCorrect(q)
        val ops = list.map { it.operator }.distinct().sorted()
        assertEquals("混合模式应覆盖全部四则运算", Operation.entries.toList(), ops)
    }

    @Test
    fun displayText_isWellFormed() {
        val q = ArithmeticQuestion(Operation.MUL, 23, 4, 92)
        assertEquals("23×4=", q.text)
        assertEquals(92, q.expected)
    }
}