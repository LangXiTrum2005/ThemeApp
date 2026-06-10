package com.wavez.themewallpaper.core.controllercenter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.wavez.themewallpaper.databinding.AnimationImageViewBinding
import org.libpag.PAGView

class AnimationImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: AnimationImageViewBinding =
        AnimationImageViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var isDetached = false
    private var isDisabledAnim = false

    private var openImagePath: String = ""
    private var closeImagePath: String = ""

    private var animationFileOpen: String = ""
    private var animationFileClose: String = ""

    var listener: AnimationListener? = null

    var viewState: State = State.Open
        private set

    private var onPagViewEnd: () -> Unit = {}

    private val pagViewListener = object : PAGView.PAGViewListener {
        override fun onAnimationStart(pagView: PAGView?) = Unit

        override fun onAnimationRepeat(pagView: PAGView?) = Unit

        override fun onAnimationUpdate(pagView: PAGView?) = Unit

        override fun onAnimationCancel(pagView: PAGView?) = Unit

        override fun onAnimationEnd(pagView: PAGView?) {
            onPagViewEnd.invoke()
        }
    }

    enum class State {
        Open,
        Close
    }

    fun initData(
        openPath: String,
        closePath: String,
        openAnim: String,
        closeAnim: String,
        initState: State,
        playAnim: Boolean = true
    ) {
        openImagePath = openPath
        closeImagePath = closePath

        animationFileOpen = openAnim
        animationFileClose = closeAnim

        isDisabledAnim = openAnim.isBlank() || closeAnim.isBlank()

        binding.animation.apply {
            setRepeatCount(1)
            removeListener(pagViewListener)
            addListener(pagViewListener)
        }

        setState(
            state = initState,
            playAnim = playAnim
        )
    }

    fun setState(
        state: State,
        playAnim: Boolean = true,
        onEnd: (State) -> Unit = {}
    ) {
        viewState = state

        val animationPath = when (state) {
            State.Open -> animationFileOpen
            State.Close -> animationFileClose
        }

        val imagePath = when (state) {
            State.Open -> openImagePath
            State.Close -> closeImagePath
        }

        val canPlayAnimation = !isDisabledAnim && animationPath.isNotBlank()

        if (canPlayAnimation) {
            showAnimation(
                animationPath = animationPath,
                playAnim = playAnim,
                state = state,
                onEnd = onEnd
            )
        } else {
            showStaticImage(
                imagePath = imagePath,
                state = state
            )
            onEnd.invoke(state)
        }
    }

    private fun showAnimation(
        animationPath: String,
        playAnim: Boolean,
        state: State,
        onEnd: (State) -> Unit
    ) {
        binding.imgOpen.visibility = GONE
        binding.imgClose.visibility = GONE
        binding.animation.visibility = VISIBLE

        binding.animation.stop()
        binding.animation.progress = 0.0
        binding.animation.path = animationPath

        if (playAnim && !isDetached) {
            onPagViewEnd = {
                binding.animation.progress = 1.0
                binding.animation.stop()
                onEnd.invoke(state)
            }

            binding.animation.play()
        } else {
            binding.animation.progress = 1.0
            binding.animation.stop()
            onEnd.invoke(state)
        }
    }

    private fun showStaticImage(
        imagePath: String,
        state: State
    ) {
        binding.animation.stop()
        binding.animation.visibility = GONE

        binding.imgClose.visibility = GONE

        val imageView = binding.imgOpen

        imageView.animate().cancel()

        imageView.apply {
            visibility = VISIBLE
            alpha = 0f
            scaleX = 0.90f
            scaleY = 0.90f
        }

        Glide.with(this)
            .load(imagePath)
            .into(imageView)

        imageView.animate()
            .alpha(1f)
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(140L)
            .withEndAction {
                imageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(90L)
                    .start()
            }
            .start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDetached = false
    }

    override fun onDetachedFromWindow() {
        binding.animation.removeListener(pagViewListener)
        binding.animation.stop()
        binding.animation.clearAnimation()
        onPagViewEnd = {}

        isDetached = true

        super.onDetachedFromWindow()
    }
}