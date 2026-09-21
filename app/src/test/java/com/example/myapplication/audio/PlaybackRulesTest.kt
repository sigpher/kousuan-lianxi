package com.example.myapplication.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class PlaybackRulesTest {

    @Test
    fun singleLoopReplaysSameTrack() {
        val r = Random(1)
        for (i in 0..20) {
            assertEquals(3, PlaybackRules.nextIndex(MusicMode.SINGLE_LOOP, 10, 3, r))
        }
    }

    @Test
    fun listLoopWrapsAround() {
        val r = Random(1)
        val size = 4
        var current = 0
        val seen = mutableListOf<Int>()
        repeat(12) {
            current = PlaybackRules.nextIndex(MusicMode.LIST_LOOP, size, current, r)
            seen.add(current)
        }
        assertEquals(listOf(1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0), seen)
    }

    @Test
    fun shuffleNeverRepeatsImmediately() {
        val r = Random(7)
        var current = 0
        repeat(100) {
            val next = PlaybackRules.nextIndex(MusicMode.SHUFFLE, 5, current, r)
            assertNotEquals(current, next)
            assert(next in 0..4)
            current = next
        }
    }

    @Test
    fun shuffleWithSingleTrackReturnsZero() {
        assertEquals(0, PlaybackRules.nextIndex(MusicMode.SHUFFLE, 1, 0, Random(1)))
    }

    @Test
    fun startIndexIsZeroForOrderedModes() {
        assertEquals(0, PlaybackRules.startIndex(MusicMode.LIST_LOOP, 10, Random(1)))
        assertEquals(0, PlaybackRules.startIndex(MusicMode.SINGLE_LOOP, 10, Random(1)))
    }

    @Test
    fun startIndexRandomForShuffle() {
        val r = Random(3)
        val indices = (0 until 20).map { PlaybackRules.startIndex(MusicMode.SHUFFLE, 7, r) }
        assert(indices.all { it in 0..6 })
        assert(indices.distinct().size > 1)
    }

    @Test
    fun emptyListReturnsZero() {
        assertEquals(0, PlaybackRules.nextIndex(MusicMode.LIST_LOOP, 0, 0))
        assertEquals(0, PlaybackRules.nextIndex(MusicMode.SINGLE_LOOP, 0, 0))
        assertEquals(0, PlaybackRules.nextIndex(MusicMode.SHUFFLE, 0, 0))
    }

    @Test
    fun modeFromValueFallsBackToListLoop() {
        assertEquals(MusicMode.SINGLE_LOOP, MusicMode.fromValue(0))
        assertEquals(MusicMode.LIST_LOOP, MusicMode.fromValue(1))
        assertEquals(MusicMode.SHUFFLE, MusicMode.fromValue(2))
        assertEquals(MusicMode.LIST_LOOP, MusicMode.fromValue(99))
    }
}