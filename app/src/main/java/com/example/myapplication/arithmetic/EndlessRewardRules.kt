package com.example.myapplication.arithmetic

object EndlessRewardRules {

    private const val CYCLE = 100

    fun flowerCountFor(correct: Int): Int =
        if (correct >= 10) (correct - 10) / CYCLE + 1 else 0

    fun heartCountFor(correct: Int): Int =
        if (correct >= 20) (correct - 20) / CYCLE + 1 else 0

    fun sapphireCountFor(correct: Int): Int =
        if (correct >= 50) (correct - 50) / CYCLE + 1 else 0

    fun crownCountFor(correct: Int): Int = correct / CYCLE

    fun rewardCounts(correct: Int): Map<String, Int> = mapOf(
        RewardRules.YELLOW_FLOWER to flowerCountFor(correct),
        RewardRules.RED_HEART to heartCountFor(correct),
        RewardRules.SAPPHIRE to sapphireCountFor(correct),
        RewardRules.CROWN to crownCountFor(correct)
    )
}