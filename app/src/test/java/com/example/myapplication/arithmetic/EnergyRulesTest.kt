package com.example.myapplication.arithmetic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnergyRulesTest {

    @Test
    fun gainIncreasesEnergy() {
        assertEquals(15f, EnergyRules.afterGain(0f))
        assertEquals(45f, EnergyRules.afterGain(30f))
    }

    @Test
    fun gainClampsAtMax() {
        assertEquals(100f, EnergyRules.afterGain(95f))
        assertEquals(100f, EnergyRules.afterGain(100f))
    }

    @Test
    fun drainDecreasesEnergy() {
        assertEquals(48f, EnergyRules.afterDrain(50f))
    }

    @Test
    fun drainClampsAtZero() {
        assertEquals(0f, EnergyRules.afterDrain(1f))
        assertEquals(0f, EnergyRules.afterDrain(0f))
    }

    @Test
    fun noVibrationBelowThreshold() {
        assertNull(EnergyRules.vibrationTierFor(0f))
        assertNull(EnergyRules.vibrationTierFor(49.9f))
        assertNull(EnergyRules.vibrationTierFor(EnergyRules.VIBRATION_THRESHOLD - 1f))
    }

    @Test
    fun vibrationStartsAtThreshold() {
        assertEquals(40, EnergyRules.vibrationTierFor(50f)?.durationMs)
        assertEquals(80, EnergyRules.vibrationTierFor(50f)?.amplitude)
    }

    @Test
    fun vibrationTiersIncreaseWithEnergy() {
        val light = EnergyRules.vibrationTierFor(60f)!!
        val medium = EnergyRules.vibrationTierFor(80f)!!
        val max = EnergyRules.vibrationTierFor(100f)!!
        assertEquals(40, light.durationMs)
        assertEquals(80, light.amplitude)
        assertEquals(80, medium.durationMs)
        assertEquals(160, medium.amplitude)
        assertEquals(120, max.durationMs)
        assertEquals(255, max.amplitude)
        assert(max.durationMs > medium.durationMs)
    }
}