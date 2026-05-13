package com.wavez.controllerwidget.core.controllercenter.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.controllercenter.utils.UtilsKt
import com.wavez.controllerwidget.databinding.LayoutSeekBarControlBinding
import kotlin.math.max
import kotlin.math.min

class SeekBarControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutSeekBarControlBinding =
        LayoutSeekBarControlBinding.inflate(LayoutInflater.from(context), this, true)

    private val handler = Handler(Looper.getMainLooper())

    private var currentProgress = 0
    private var maxProgress = 100

    private var defaultHeight = 0f
    private var defaultWidth = 0f

    private var iconMarginBottom = 0f
    private var iconSize = 0f

    private var iconNormal = true

    private var iconActiveFile: String? = null
    private var iconNormalFile: String? = null

    private var progressBitmap: Bitmap? = null

    private var progressRunnable: Runnable? = null
    private var dragStartProgress: Int? = null
    private var lastUserProgress = -1

    var progressListener: OnProgressChangeListener? = null

    init {

        if (attrs != null) {

            context.withStyledAttributes(
                attrs,
                R.styleable.SeekBarControlView,
                0,
                0
            ) {

                iconMarginBottom = getDimension(
                    R.styleable.SeekBarControlView_iconMarginBottom,
                    0f
                )

                iconSize = getDimension(
                    R.styleable.SeekBarControlView_iconWH,
                    0f
                )

                defaultHeight = getDimension(
                    R.styleable.SeekBarControlView_defaultHeight,
                    0f
                )

                defaultWidth = getDimension(
                    R.styleable.SeekBarControlView_defaultWidth,
                    0f
                )

            }
        }

        setupIconLayout()
        setupPull()
    }

    private fun setupIconLayout() {

        val params =
            binding.icon.layoutParams as LayoutParams

        params.width = iconSize.toInt()
        params.height = iconSize.toInt()
        params.bottomMargin = iconMarginBottom.toInt()

        binding.icon.layoutParams = params
    }

    private fun setupPull() {

        binding.pullBar.setPullDirection(PullDirection.UP)

        binding.pullBar.setPullingListener(object : PullingListener {

            override fun onCancel(distance: Float) {
                dragStartProgress = null
                updateProgress(currentProgress, saveCurrent = false, fromInit = true)
            }

            override fun onPull(distance: Float, isForward: Boolean) {
                val startProgress = dragStartProgress ?: currentProgress.also {
                    dragStartProgress = it
                }

                val progress =
                    startProgress + ((distance * maxProgress) / height).toInt()

                updateProgress(progress, false, false)
            }

            override fun onRelease(distance: Float) {
                val startProgress = dragStartProgress ?: currentProgress

                val progress =
                    startProgress + ((distance * maxProgress) / height).toInt()

                updateProgress(progress, true, false)
                dragStartProgress = null
            }
        })
    }

    fun initData(
        iconActive: String,
        iconNormal: String,
        progressForeground: String,
        background: String?,
        progress: Int,
        maxProgress: Int
    ) {

        this.iconActiveFile = iconActive
        this.iconNormalFile = iconNormal

        this.maxProgress = maxProgress
        this.currentProgress = progress

        Glide.with(this)
            .asBitmap()
            .load(progressForeground)
            .format(PREFER_RGB_565)
            .into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {

                    progressBitmap = resource

                    updateProgress(
                        currentProgress,
                        true,
                        true
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        val iconPath =
            if (currentProgress > 0) iconActiveFile else iconNormalFile

        UtilsKt.loadImageOptimizeRam(
            binding.icon,
            iconPath,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
        )

        background?.let {

            UtilsKt.loadImageOptimizeRam(
                binding.background,
                it,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
            )
        }
    }

    fun updateProgress(
        progress: Int,
        saveCurrent: Boolean,
        fromInit: Boolean
    ) {

        val value = min(max(progress, 0), maxProgress)

        if (saveCurrent) {
            currentProgress = value
        }

        drawProgressBitmap(value.toFloat() / maxProgress)

        updateIconState(value)

        if (fromInit) return
        lastUserProgress = value

        progressRunnable?.let {
            handler.removeCallbacks(it)
        }

        val runnable = Runnable {
            progressListener?.onProgressChanged(value)
        }

        progressRunnable = runnable

        handler.postDelayed(runnable, 300)
    }

    fun syncProgress(progress: Int) {
        if (dragStartProgress != null) return
        val value = min(max(progress, 0), maxProgress)
        if (lastUserProgress >= 0 && kotlin.math.abs(value - lastUserProgress) <= 2) {
            currentProgress = lastUserProgress
            return
        }
        lastUserProgress = -1
        updateProgress(value, saveCurrent = true, fromInit = true)
    }

    private fun drawProgressBitmap(percent: Float) {

        val bitmap = progressBitmap ?: return

        if (percent <= 0f) {
            binding.progress.setImageDrawable(null)
            return
        }

        val height = (bitmap.height * percent).toInt()

        val cropBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            bitmap.height - height,
            bitmap.width,
            height
        )

        val outputBitmap = createBitmap(defaultWidth.toInt(), defaultHeight.toInt())

        val canvas = Canvas(outputBitmap)

        val top = defaultHeight - (percent * defaultHeight)

        canvas.drawBitmap(
            cropBitmap,
            null,
            RectF(
                0f,
                top,
                defaultWidth,
                defaultHeight
            ),
            null as Paint?
        )

        binding.progress.setImageBitmap(outputBitmap)
    }

    private fun updateIconState(progress: Int) {

        if (
            iconNormal &&
            progress > 0 &&
            iconActiveFile != null
        ) {

            iconNormal = false

            loadIcon(binding.icon, iconActiveFile)

        } else if (
            !iconNormal &&
            progress == 0 &&
            iconNormalFile != null
        ) {

            iconNormal = true

            loadIcon(binding.icon, iconNormalFile)
        }
    }

    private fun loadIcon(
        imageView: ImageView,
        path: String?
    ) {

        UtilsKt.loadImageOptimizeRam(
            imageView,
            path,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(progress: Int)
    }
}
