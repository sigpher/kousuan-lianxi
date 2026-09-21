package com.example.myapplication.arithmetic

import kotlin.random.Random

class ArithmeticProblemGenerator(
    private val random: Random = Random.Default,
    val maxNumber: Int = 100
) {

    fun generate(op: Operation, count: Int = 100): List<ArithmeticQuestion> =
        List(count) {
            when (op) {
                Operation.ADD -> add()
                Operation.SUB -> sub()
                Operation.MUL -> mul()
                Operation.DIV -> div()
            }
        }

    fun generateMixed(count: Int = 100): List<ArithmeticQuestion> =
        List(count) {
            when (Operation.entries.random(random)) {
                Operation.ADD -> add()
                Operation.SUB -> sub()
                Operation.MUL -> mul()
                Operation.DIV -> div()
            }
        }

    private fun add(): ArithmeticQuestion {
        val a = random.nextInt(1, maxNumber)
        val b = random.nextInt(1, maxNumber - a + 1)
        return ArithmeticQuestion(Operation.ADD, a, b, a + b)
    }

    private fun sub(): ArithmeticQuestion {
        val a = random.nextInt(1, maxNumber + 1)
        val b = random.nextInt(1, a + 1)
        return ArithmeticQuestion(Operation.SUB, a, b, a - b)
    }

    private fun mul(): ArithmeticQuestion {
        val a = random.nextInt(1, 21)
        val b = random.nextInt(1, 11)
        return ArithmeticQuestion(Operation.MUL, a, b, a * b)
    }

    private fun div(): ArithmeticQuestion {
        val divisor = random.nextInt(2, 10)
        val quotient = random.nextInt(1, 10)
        return ArithmeticQuestion(Operation.DIV, divisor * quotient, divisor, quotient)
    }
}