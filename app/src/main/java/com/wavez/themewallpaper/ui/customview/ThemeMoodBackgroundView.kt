package com.wavez.themewallpaper.ui.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.text.Normalizer
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/** Decorative background for the themes tab. It does not own category selection or content state. */
class ThemeMoodBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true }
    private val frameAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = MOTION_DURATION_MS
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { postInvalidateOnAnimation() }
    }

    private var currentMood = Mood.DEFAULT
    private var previousMood = Mood.DEFAULT
    private var moodChangedAt = 0L

    fun setCategory(categoryName: String) {
        val nextMood = Mood.from(categoryName)
        if (nextMood == currentMood) return

        previousMood = currentMood
        currentMood = nextMood
        moodChangedAt = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startMotion()
    }

    override fun onDetachedFromWindow() {
        frameAnimator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) startMotion() else frameAnimator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val time = (SystemClock.uptimeMillis() % MOTION_DURATION_MS).toFloat() / MOTION_DURATION_MS
        val transition = transitionProgress()
        if (transition < 1f) {
            drawMood(canvas, previousMood, time, 1f)
            drawMood(canvas, currentMood, time, transition)
        } else {
            drawMood(canvas, currentMood, time, 1f)
        }
    }

    private fun startMotion() {
        if (ValueAnimator.areAnimatorsEnabled() && !frameAnimator.isRunning) {
            frameAnimator.start()
        }
    }

    private fun transitionProgress(): Float {
        if (moodChangedAt == 0L || !ValueAnimator.areAnimatorsEnabled()) return 1f
        return ((SystemClock.uptimeMillis() - moodChangedAt).toFloat() / TRANSITION_DURATION_MS)
            .coerceIn(0f, 1f)
    }

    private fun drawMood(canvas: Canvas, mood: Mood, time: Float, alpha: Float) {
        when (mood) {
            Mood.HOT -> drawHot(canvas, time, alpha)
            Mood.SPORT -> drawSport(canvas, time, alpha)
            Mood.ANIME -> drawAnime(canvas, time, alpha)
            Mood.AESTHETICS -> drawAesthetics(canvas, time, alpha)
            Mood.CUTE -> drawCute(canvas, time, alpha)
            Mood.K_POP -> drawKPop(canvas, time, alpha)
            Mood.CARTOON -> drawCartoon(canvas, time, alpha)
            Mood.DEFAULT -> drawDefault(canvas, time, alpha)
        }
    }

    private fun drawHot(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(24, 17, 21), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * .22f, height * (.92f + sin(time * TWO_PI) * .03f), width * .85f, Color.argb(152, 237, 73, 24), alpha)
        radial(canvas, width * .74f, height * (.78f + cos(time * TWO_PI * .8f) * .04f), width * .62f, Color.argb(112, 255, 164, 35), alpha)
        radial(canvas, width * (.48f + sin(time * TWO_PI * 1.2f) * .08f), height * .54f, width * .32f, Color.argb(72, 255, 99, 28), alpha)
        repeat(6) { index ->
            val phase = time * TWO_PI + index * 1.31f
            val x = width * (.1f + index * .17f + sin(phase) * .035f)
            val y = height * (.88f - (index % 3) * .12f - (time * .18f + index * .07f) % .22f)
            circle(canvas, x, y, width * (.006f + (index % 2) * .002f), Color.argb(150, 255, 191, 65), alpha)
        }
    }

    private fun drawSport(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(11, 25, 43), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.24f + sin(time * TWO_PI * .6f) * .08f), height * .2f, width * .8f, Color.argb(118, 18, 126, 192), alpha)
        radial(canvas, width * .82f, height * (.72f + cos(time * TWO_PI * .7f) * .05f), width * .62f, Color.argb(96, 45, 199, 224), alpha)
        repeat(5) { index ->
            val offset = ((time * 1.4f + index * .23f) % 1f) * width * 1.4f
            val startX = offset - width * .65f
            val centerY = height * (.18f + index * .17f)
            streak(canvas, startX, centerY + width * .18f, startX + width * .75f, centerY - width * .15f, Color.argb(58 + index * 8, 121, 226, 255), alpha)
        }
    }

    private fun drawAnime(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(48, 31, 78), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.2f + sin(time * TWO_PI * .6f) * .08f), height * .26f, width * .78f, Color.argb(132, 237, 92, 190), alpha)
        radial(canvas, width * (.78f + cos(time * TWO_PI * .5f) * .06f), height * .74f, width * .72f, Color.argb(138, 115, 112, 255), alpha)
        streak(canvas, -width * .1f, height * (.72f + sin(time * TWO_PI) * .04f), width * 1.1f, height * (.36f + cos(time * TWO_PI) * .04f), Color.argb(54, 255, 215, 247), alpha)
    }

    private fun drawAesthetics(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(61, 54, 76), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.24f + sin(time * TWO_PI * .45f) * .06f), height * .3f, width * .84f, Color.argb(120, 189, 161, 230), alpha)
        radial(canvas, width * (.78f + cos(time * TWO_PI * .55f) * .06f), height * .7f, width * .78f, Color.argb(112, 242, 174, 159), alpha)
        radial(canvas, width * .5f, height * (.54f + sin(time * TWO_PI * .8f) * .04f), width * .4f, Color.argb(58, 255, 230, 207), alpha)
    }

    private fun drawCute(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(100, 57, 90), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.2f + sin(time * TWO_PI * .45f) * .07f), height * .25f, width * .7f, Color.argb(124, 255, 165, 205), alpha)
        radial(canvas, width * (.78f + cos(time * TWO_PI * .55f) * .05f), height * .76f, width * .64f, Color.argb(116, 255, 205, 147), alpha)
        repeat(4) { index ->
            val phase = time * TWO_PI * (.4f + index * .1f) + index
            circle(canvas, width * (.16f + index * .22f + sin(phase) * .035f), height * (.38f + (index % 2) * .24f + cos(phase) * .025f), width * (.055f + index * .006f), Color.argb(34, 255, 239, 248), alpha)
        }
    }

    private fun drawCartoon(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(107, 79, 25), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.22f + sin(time * TWO_PI * .5f) * .07f), height * .22f, width * .78f, Color.argb(132, 255, 211, 74), alpha)
        radial(canvas, width * (.8f + cos(time * TWO_PI * .55f) * .06f), height * .76f, width * .7f, Color.argb(108, 255, 160, 36), alpha)
        circle(canvas, width * .5f, height * (.48f + sin(time * TWO_PI * .75f) * .04f), width * .16f, Color.argb(34, 255, 248, 194), alpha)
    }

    private fun drawKPop(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(13, 20, 39), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.25f + sin(time * TWO_PI * .7f) * .08f), height * .22f, width * .74f, Color.argb(112, 48, 102, 202), alpha)
        radial(canvas, width * (.78f + cos(time * TWO_PI * .6f) * .06f), height * .74f, width * .66f, Color.argb(86, 214, 64, 185), alpha)
        streak(canvas, width * .1f, height * 1.1f, width * .55f, -height * .1f, Color.argb(44, 157, 201, 255), alpha)
        streak(canvas, width * .72f, height * 1.1f, width * .42f, -height * .1f, Color.argb(42, 255, 128, 214), alpha)
    }

    private fun drawDefault(canvas: Canvas, time: Float, alpha: Float) {
        fill(canvas, Color.rgb(42, 41, 54), alpha)
        val width = width.toFloat()
        val height = height.toFloat()
        radial(canvas, width * (.23f + sin(time * TWO_PI * .5f) * .06f), height * .24f, width * .8f, Color.argb(92, 100, 116, 178), alpha)
        radial(canvas, width * (.77f + cos(time * TWO_PI * .55f) * .06f), height * .76f, width * .72f, Color.argb(78, 131, 103, 160), alpha)
    }

    private fun fill(canvas: Canvas, color: Int, alpha: Float) {
        paint.shader = null
        paint.color = color
        paint.alpha = (Color.alpha(color) * alpha).toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun radial(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int, alpha: Float) {
        paint.shader = RadialGradient(x, y, radius, withAlpha(color, alpha), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        paint.alpha = 255
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null
    }

    private fun circle(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int, alpha: Float) {
        paint.shader = null
        paint.color = color
        paint.alpha = (Color.alpha(color) * alpha).toInt()
        canvas.drawCircle(x, y, radius, paint)
    }

    private fun streak(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float, color: Int, alpha: Float) {
        paint.shader = LinearGradient(startX, startY, endX, endY, Color.TRANSPARENT, withAlpha(color, alpha), Shader.TileMode.CLAMP)
        paint.strokeWidth = width * .025f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(startX, startY, endX, endY, paint)
        paint.shader = null
    }

    private fun withAlpha(color: Int, multiplier: Float): Int =
        Color.argb((Color.alpha(color) * multiplier).toInt(), Color.red(color), Color.green(color), Color.blue(color))

    private enum class Mood {
        HOT,
        SPORT,
        ANIME,
        AESTHETICS,
        CUTE,
        K_POP,
        CARTOON,
        DEFAULT;

        companion object {
            fun from(categoryName: String): Mood {
                val normalized = Normalizer.normalize(categoryName, Normalizer.Form.NFD)
                    .replace("\\p{M}".toRegex(), "")
                    .lowercase(Locale.ROOT)
                    .replace("[^a-z0-9]".toRegex(), "")
                return when {
                    normalized.contains("hot") -> HOT
                    normalized.contains("sport") -> SPORT
                    normalized.contains("anime") -> ANIME
                    normalized.contains("aesthetic") -> AESTHETICS
                    normalized.contains("cute") -> CUTE
                    normalized.contains("kpop") -> K_POP
                    normalized.contains("cartoon") -> CARTOON
                    else -> DEFAULT
                }
            }
        }
    }

    private companion object {
        const val MOTION_DURATION_MS = 16_000L
        const val TRANSITION_DURATION_MS = 360L
        const val TWO_PI = (Math.PI * 2).toFloat()
    }
}
