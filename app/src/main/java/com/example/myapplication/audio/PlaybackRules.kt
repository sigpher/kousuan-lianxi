package com.example.myapplication.audio

import kotlin.random.Random

enum class MusicMode(val value: Int) {
    SINGLE_LOOP(0),
    LIST_LOOP(1),
    SHUFFLE(2);

    companion object {
        fun fromValue(v: Int): MusicMode =
            values().firstOrNull { it.value == v } ?: LIST_LOOP
    }
}

object PlaybackRules {

    fun startIndex(mode: MusicMode, size: Int, random: Random = Random.Default): Int {
        if (size <= 0) return 0
        return when (mode) {
            MusicMode.SHUFFLE -> random.nextInt(size)
            else -> 0
        }
    }

    fun nextIndex(mode: MusicMode, size: Int, current: Int, random: Random = Random.Default): Int {
        if (size <= 0) return 0
        return when (mode) {
            MusicMode.SINGLE_LOOP -> current
            MusicMode.LIST_LOOP -> (current + 1) % size
            MusicMode.SHUFFLE -> {
                if (size == 1) return 0
                var next = random.nextInt(size)
                while (next == current) {
                    next = random.nextInt(size)
                }
                next
            }
        }
    }
}