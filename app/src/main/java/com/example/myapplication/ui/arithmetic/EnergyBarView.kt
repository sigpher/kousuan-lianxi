package com.example.myapplication.ui.arithmetic

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class EnergyBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        private val TRACK_COLOR = 0xFFE0E0E0.toInt()
        private val COLOR_LOW = 0xFFFFF9C4.toInt()
        private val COLOR_MID = 0xFFFFB300.toInt()
        private val COLOR_HIGH = 0xFFF44336.toInt()
        private const val CORNER_RADIUS = 20f
        private const val ANIM_MS = 250L
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = TRACK_COLOR
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_LOW
    }
    private val bounds = RectF()
    private var fraction = 0f

    private var colorAnimator = ValueAnimator.ofFloat(0f, 1f)

    fun setEnergy(energy: Float) {
        val target = energy.coerceIn(0f, 100f) / 100f
        if (target == fraction) return
        colorAnimator.cancel()
        val start = fraction
        colorAnimator = ValueAnimator.ofFloat(start, target).apply {
            duration = ANIM_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                fraction = it.animatedValue as Float
                fillPaint.color = interpolateColor(fraction)
                invalidate()
            }
        }
        colorAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(bounds, CORNER_RADIUS, CORNER_RADIUS, trackPaint)
        val fillWidth = width * fraction
        if (fillWidth <= 0f) return
        bounds.right = fillWidth
        canvas.drawRoundRect(bounds, CORNER_RADIUS, CORNER_RADIUS, fillPaint)
    }

    private fun interpolateColor(v: Float): Int {
        val evaluator = ArgbEvaluator()
        return when {
            v < 0.5f -> evaluator.evaluate(v / 0.5f, COLOR_LOW, COLOR_MID) as Int
            else -> evaluator.evaluate((v - 0.5f) / 0.5f, COLOR_MID, COLOR_HIGH) as Int
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        colorAnimator.cancel()
    }
}