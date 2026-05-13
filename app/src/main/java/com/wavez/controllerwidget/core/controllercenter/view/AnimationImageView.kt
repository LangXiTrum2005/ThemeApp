package com.wavez.controllerwidget.core.controllercenter.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.wavez.controllerwidget.databinding.AnimationImageViewBinding
import org.libpag.PAGView
import kotlin.Unit

class AnimationImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = "AnimationImageView"

    private val binding: AnimationImageViewBinding =
        AnimationImageViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var isDetached = false
    private var isDisabledAnim = false

    var animationFileClose: String = ""
    var animationFileOpen: String = ""

    var listener: AnimationListener? = null

    var viewState: State = State.Open
        private set

    private var onPageViewEnd: () -> Unit = {}

    private val pagViewListener = object : PAGView.PAGViewListener {

        override fun onAnimationStart(pagView: PAGView?) {}

        override fun onAnimationRepeat(pagView: PAGView?) {}
        override fun onAnimationUpdate(p0: PAGView?) {

        }

        override fun onAnimationCancel(pagView: PAGView?) {}

        override fun onAnimationEnd(pagView: PAGView?) {
            onPageViewEnd.invoke()
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

        if (!isDisabledAnim) {
            animationFileClose = closeAnim
            animationFileOpen = openAnim
        }

        binding.animation.apply {
            setRepeatCount(1)
            removeListener(pagViewListener)
            addListener(pagViewListener)
        }

        setState(initState, playAnim)

        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDetached = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isDetached = true
        binding.animation.clearAnimation()
    }

    fun setState(
        state: State,
        playAnim: Boolean = true,
        onEnd: (State) -> Unit = {}
    ) {

        viewState = state

        binding.imgClose.visibility = GONE
        binding.imgOpen.visibility = GONE
        binding.animation.visibility = VISIBLE

        val animationPath = when (state) {
            State.Close -> animationFileClose
            State.Open -> animationFileOpen
        }

        binding.animation.path = animationPath

        if (playAnim) {

            onPageViewEnd = {
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
}