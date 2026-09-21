package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Test

class EndlessRewardRulesTest {

    @Test
    fun nothingBelowTen() {
        assertEquals(0, EndlessRewardRules.flowerCountFor(0))
        assertEquals(0, EndlessRewardRules.flowerCountFor(9))
        assertEquals(0, EndlessRewardRules.heartCountFor(9))
        assertEquals(0, EndlessRewardRules.sapphireCountFor(9))
        assertEquals(0, EndlessRewardRules.crownCountFor(9))
        assertEquals(0, EndlessRewardRules.rewardCounts(9).values.sum())
    }

    @Test
    fun firstCycleMilestones() {
        assertEquals(1, EndlessRewardRules.flowerCountFor(10))
        assertEquals(1, EndlessRewardRules.heartCountFor(20))
        assertEquals(1, EndlessRewardRules.sapphireCountFor(50))
        assertEquals(1, EndlessRewardRules.crownCountFor(100))
    }

    @Test
    fun first100() {
        val counts = EndlessRewardRules.rewardCounts(100)
        assertEquals(1, counts[RewardRules.YELLOW_FLOWER])
        assertEquals(1, counts[RewardRules.RED_HEART])
        assertEquals(1, counts[RewardRules.SAPPHIRE])
        assertEquals(1, counts[RewardRules.CROWN])
    }

    @Test
    fun oneTenGivesSecondFlower() {
        val counts = EndlessRewardRules.rewardCounts(110)
        assertEquals(2, counts[RewardRules.YELLOW_FLOWER])
        assertEquals(1, counts[RewardRules.RED_HEART])
        assertEquals(1, counts[RewardRules.SAPPHIRE])
        assertEquals(1, counts[RewardRules.CROWN])
    }

    @Test
    fun midCycleCounts() {
        val counts = EndlessRewardRules.rewardCounts(125)
        assertEquals(2, counts[RewardRules.YELLOW_FLOWER])
        assertEquals(2, counts[RewardRules.RED_HEART])
        assertEquals(1, counts[RewardRules.SAPPHIRE])
        assertEquals(1, counts[RewardRules.CROWN])
    }

    @Test
    fun twoHundredFive() {
        val counts = EndlessRewardRules.rewardCounts(205)
        assertEquals(2, counts[RewardRules.YELLOW_FLOWER])
        assertEquals(2, counts[RewardRules.RED_HEART])
        assertEquals(2, counts[RewardRules.SAPPHIRE])
        assertEquals(2, counts[RewardRules.CROWN])
    }

    @Test
    fun hundredTwenty() {
        val counts = EndlessRewardRules.rewardCounts(120)
        assertEquals(2, counts[RewardRules.YELLOW_FLOWER])
        assertEquals(2, counts[RewardRules.RED_HEART])
        assertEquals(1, counts[RewardRules.SAPPHIRE])
        assertEquals(1, counts[RewardRules.CROWN])
    }
}