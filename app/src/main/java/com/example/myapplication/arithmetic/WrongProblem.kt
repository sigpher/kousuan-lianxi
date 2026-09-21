package com.example.myapplication.arithmetic

import kotlin.random.Random

data class WrongProblem(
    val question: ArithmeticQuestion,
    val userAnswer: String,
    val createdAt: Long
)

object ReviewSelector {
    fun pick(problems: List<WrongProblem>, limit: Int, random: Random = Random.Default): List<WrongProblem> =
        if (problems.size <= limit) problems else problems.shuffled(random).take(limit)
}

object RewardRules {
    const val CROWN = "crown"
    val CROWN_QUESTION_COUNTS: Set<Int> = setOf(50, 100)

    fun awardsCrown(plannedCount: Int, score: Int, review: Boolean): Boolean =
        !review && plannedCount in CROWN_QUESTION_COUNTS && score == plannedCount

    val CROWN_LEVEL_THRESHOLDS: List<Int> = listOf(0, 1, 3, 6, 10)

    fun levelIndexForCrowns(crowns: Int): Int =
        CROWN_LEVEL_THRESHOLDS.indexOfLast { crowns >= it }.coerceAtLeast(0)

    fun nextLevelCrowns(levelIndex: Int): Int? =
        CROWN_LEVEL_THRESHOLDS.getOrNull(levelIndex + 1)
}