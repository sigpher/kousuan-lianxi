package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RewardRulesTest {

    @Test
    fun rewardForPerfect10IsYellowFlower() {
        assertEquals(RewardRules.YELLOW_FLOWER, RewardRules.rewardForRound(10, 10, review = false))
    }

    @Test
    fun rewardForPerfect20IsRedHeart() {
        assertEquals(RewardRules.RED_HEART, RewardRules.rewardForRound(20, 20, review = false))
    }

    @Test
    fun rewardForPerfect50IsSapphire() {
        assertEquals(RewardRules.SAPPHIRE, RewardRules.rewardForRound(50, 50, review = false))
    }

    @Test
    fun rewardForPerfect100IsCrown() {
        assertEquals(RewardRules.CROWN, RewardRules.rewardForRound(100, 100, review = false))
    }

    @Test
    fun noRewardWhenNotPerfect() {
        assertNull(RewardRules.rewardForRound(50, 49, review = false))
        assertNull(RewardRules.rewardForRound(100, 99, review = false))
        assertNull(RewardRules.rewardForRound(30, 30, review = false))
    }

    @Test
    fun noRewardForReviewRounds() {
        assertNull(RewardRules.rewardForRound(50, 50, review = true))
        assertNull(RewardRules.rewardForRound(100, 100, review = true))
        assertNull(RewardRules.rewardForRound(10, 10, review = true))
    }

    @Test
    fun levelIndexForCrowns() {
        assertEquals(0, RewardRules.levelIndexForCrowns(0))
        assertEquals(1, RewardRules.levelIndexForCrowns(1))
        assertEquals(1, RewardRules.levelIndexForCrowns(2))
        assertEquals(2, RewardRules.levelIndexForCrowns(3))
        assertEquals(2, RewardRules.levelIndexForCrowns(5))
        assertEquals(3, RewardRules.levelIndexForCrowns(6))
        assertEquals(3, RewardRules.levelIndexForCrowns(9))
        assertEquals(4, RewardRules.levelIndexForCrowns(10))
        assertEquals(4, RewardRules.levelIndexForCrowns(99))
    }

    @Test
    fun nextLevelCrowns() {
        assertEquals(1, RewardRules.nextLevelCrowns(0))
        assertEquals(3, RewardRules.nextLevelCrowns(1))
        assertEquals(6, RewardRules.nextLevelCrowns(2))
        assertEquals(10, RewardRules.nextLevelCrowns(3))
        assertNull(RewardRules.nextLevelCrowns(4))
    }
}