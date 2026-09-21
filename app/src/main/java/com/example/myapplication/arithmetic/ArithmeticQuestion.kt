package com.example.myapplication.arithmetic

enum class Operation(val symbol: String) {
    ADD("+"),
    SUB("-"),
    MUL("×"),
    DIV("÷")
}

data class ArithmeticQuestion(
    val operator: Operation,
    val operandA: Int,
    val operandB: Int,
    val answer: Int
) {
    val text: String
        get() = "$operandA${operator.symbol}$operandB="

    val expected: Int
        get() = when (operator) {
            Operation.ADD -> operandA + operandB
            Operation.SUB -> operandA - operandB
            Operation.MUL -> operandA * operandB
            Operation.DIV -> operandA / operandB
        }
}