package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardRulesTest {

    @Test
    fun awardsCrownForPerfect50() {
        assertTrue(RewardRules.awardsCrown(plannedCount = 50, score = 50, review = false))
    }

    @Test
    fun awardsCrownForPerfect100() {
        assertTrue(RewardRules.awardsCrown(plannedCount = 100, score = 100, review = false))
    }

    @Test
    fun noCrownForSmallCounts() {
        assertFalse(RewardRules.awardsCrown(plannedCount = 10, score = 10, review = false))
        assertFalse(RewardRules.awardsCrown(plannedCount = 20, score = 20, review = false))
    }

    @Test
    fun noCrownWhenNotPerfect() {
        assertFalse(RewardRules.awardsCrown(plannedCount = 50, score = 49, review = false))
        assertFalse(RewardRules.awardsCrown(plannedCount = 100, score = 99, review = false))
    }

    @Test
    fun noCrownForReviewRounds() {
        assertFalse(RewardRules.awardsCrown(plannedCount = 50, score = 50, review = true))
        assertFalse(RewardRules.awardsCrown(plannedCount = 100, score = 100, review = true))
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