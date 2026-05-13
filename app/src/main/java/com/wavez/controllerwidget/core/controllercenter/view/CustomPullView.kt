package com.wavez.controllerwidget.core.controllercenter.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

class CustomPullView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var startRawY = 0f
    private var lastRawY = 0f

    private var pullDirection: PullDirection = PullDirection.DOWN
    private var pullingListener: PullingListener? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                startRawY = event.rawY
                lastRawY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                val currentRawY = event.rawY
                val isForward = currentRawY - lastRawY > 0f

                val distance = when (pullDirection) {
                    PullDirection.DOWN -> {
                        currentRawY - startRawY
                    }

                    PullDirection.UP -> {
                        startRawY - currentRawY
                    }
                }

                pullingListener?.onPull(distance, isForward)

                lastRawY = currentRawY
            }

            MotionEvent.ACTION_UP -> {

                val distance = when (pullDirection) {
                    PullDirection.DOWN -> {
                        event.rawY - startRawY
                    }

                    PullDirection.UP -> {
                        startRawY - event.rawY
                    }
                }

                pullingListener?.onRelease(distance)
            }

            MotionEvent.ACTION_CANCEL -> {

                val distance = when (pullDirection) {
                    PullDirection.DOWN -> {
                        event.rawY - startRawY
                    }

                    PullDirection.UP -> {
                        startRawY - event.rawY
                    }
                }

                pullingListener?.onCancel(distance)
            }
        }

        return true
    }

    fun setPullDirection(direction: PullDirection) {
        pullDirection = direction
    }

    fun setPullingListener(listener: PullingListener) {
        pullingListener = listener
    }
}