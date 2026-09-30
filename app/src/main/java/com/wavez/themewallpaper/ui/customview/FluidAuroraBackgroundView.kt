package com.wavez.themewallpaper.ui.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom View that renders a smooth, GPU-accelerated fluid aurora background
 * with breathing luminous gradient orbs, creating a modern and alive visual backdrop.
 */
class FluidAuroraBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
    }

    private var animator: ValueAnimator? = null
    private var animationTime = 0f

    // Soft pastel / neon aurora glowing orbs
    private val orbs = arrayOf(
        // Orb 1: Primary Modern Violet (#865CFA)
        AuroraOrb(
            baseX = 0.25f,
            baseY = 0.20f,
            radiusMultiplier = 0.65f,
            colorCenter = Color.argb(60, 134, 92, 250),
            colorEdge = Color.argb(0, 134, 92, 250),
            speedX = 0.7f,
            speedY = 0.5f,
            phase = 0.0f
        ),
        // Orb 2: Vibrant Soft Pink/Magenta (#FF6DE9)
        AuroraOrb(
            baseX = 0.80f,
            baseY = 0.35f,
            radiusMultiplier = 0.55f,
            colorCenter = Color.argb(45, 255, 109, 233),
            colorEdge = Color.argb(0, 255, 109, 233),
            speedX = 0.6f,
            speedY = 0.8f,
            phase = 1.5f
        ),
        // Orb 3: Sky Cyan / Soft Blue (#52B7FF)
        AuroraOrb(
            baseX = 0.35f,
            baseY = 0.75f,
            radiusMultiplier = 0.60f,
            colorCenter = Color.argb(40, 82, 183, 255),
            colorEdge = Color.argb(0, 82, 183, 255),
            speedX = 0.5f,
            speedY = 0.6f,
            phase = 3.0f
        ),
        // Orb 4: Soft Lavender (#C7B3FF)
        AuroraOrb(
            baseX = 0.75f,
            baseY = 0.85f,
            radiusMultiplier = 0.50f,
            colorCenter = Color.argb(35, 199, 179, 255),
            colorEdge = Color.argb(0, 199, 179, 255),
            speedX = 0.8f,
            speedY = 0.4f,
            phase = 4.2f
        )
    )

    private val baseBackgroundColor = Color.parseColor("#F9F8FC")

    init {
        basePaint.color = baseBackgroundColor
        setupAnimator()
    }

    private fun setupAnimator() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16_000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val fraction = it.animatedFraction
                animationTime = (fraction * 2f * Math.PI).toFloat()
                postInvalidateOnAnimation()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    private fun startAnimation() {
        if (animator?.isRunning != true) {
            animator?.start()
        }
    }

    private fun stopAnimation() {
        if (animator?.isRunning == true) {
            animator?.cancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // 1. Draw base soft clean background
        canvas.drawRect(0f, 0f, w, h, basePaint)

        // 2. Draw animated glowing orbs
        val baseDim = maxOf(w, h)
        for (orb in orbs) {
            val offsetX = sin(animationTime * orb.speedX + orb.phase) * (w * 0.18f)
            val offsetY = cos(animationTime * orb.speedY + orb.phase) * (h * 0.12f)

            val currentX = (orb.baseX * w) + offsetX
            val currentY = (orb.baseY * h) + offsetY

            val pulse = 1f + 0.08f * sin(animationTime * 1.2f + orb.phase)
            val radius = baseDim * orb.radiusMultiplier * pulse

            if (radius > 0f) {
                orbPaint.shader = RadialGradient(
                    currentX,
                    currentY,
                    radius,
                    orb.colorCenter,
                    orb.colorEdge,
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(currentX, currentY, radius, orbPaint)
            }
        }
    }

    private data class AuroraOrb(
        val baseX: Float,
        val baseY: Float,
        val radiusMultiplier: Float,
        val colorCenter: Int,
        val colorEdge: Int,
        val speedX: Float,
        val speedY: Float,
        val phase: Float,
    )
}
