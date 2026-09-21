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
    const val YELLOW_FLOWER = "yellow_flower"
    const val RED_HEART = "red_heart"
    const val SAPPHIRE = "sapphire"
    const val CROWN = "crown"

    val REWARD_QUESTION_COUNTS: Map<Int, String> = mapOf(
        10 to YELLOW_FLOWER,
        20 to RED_HEART,
        50 to SAPPHIRE,
        100 to CROWN
    )
    val REDO_QUESTION_COUNTS: Set<Int> = setOf(50, 100)

    fun rewardForRound(plannedCount: Int, score: Int, review: Boolean): String? {
        if (review) return null
        return REWARD_QUESTION_COUNTS[plannedCount]?.takeIf { score == plannedCount }
    }

    val CROWN_LEVEL_THRESHOLDS: List<Int> = listOf(0, 1, 3, 6, 10)

    fun levelIndexForCrowns(crowns: Int): Int =
        CROWN_LEVEL_THRESHOLDS.indexOfLast { crowns >= it }.coerceAtLeast(0)

    fun nextLevelCrowns(levelIndex: Int): Int? =
        CROWN_LEVEL_THRESHOLDS.getOrNull(levelIndex + 1)
}