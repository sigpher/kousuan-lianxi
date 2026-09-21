package com.example.myapplication.arithmetic

data class VibrationTier(val durationMs: Int, val amplitude: Int)

object EnergyRules {
    const val MAX = 100f
    const val GAIN_PER_CORRECT = 15f
    const val DRAIN_PER_SECOND = 2f
    const val VIBRATION_THRESHOLD = 50f

    fun afterGain(energy: Float): Float =
        (energy + GAIN_PER_CORRECT).coerceIn(0f, MAX)

    fun afterDrain(energy: Float): Float =
        (energy - DRAIN_PER_SECOND).coerceIn(0f, MAX)

    fun vibrationTierFor(energy: Float): VibrationTier? = when {
        energy < VIBRATION_THRESHOLD -> null
        energy < 70f -> VibrationTier(40, 80)
        energy < 90f -> VibrationTier(80, 160)
        else -> VibrationTier(120, 255)
    }
}