package com.wavez.controllerwidget.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.core.extensions.gone

class CustomToast(context: Context) : Toast(context) {

    companion object {

        fun makeText(
            context: Context,
            @SuppressLint("SupportAnnotationUsage") @StringRes contentId: Int,
            @DrawableRes bgDrawable: Int? = null,
            @DrawableRes iconId: Int? = null,
            gravity: Int = Gravity.BOTTOM,
            duration: Int = LENGTH_SHORT,
            textColor: Int = R.color.black
        ): Toast {
            val toast = Toast(context)
            toast.duration = duration

            val layout: View =
                LayoutInflater.from(context).inflate(R.layout.layout_toast, null, false)

            val bgRoot = layout.findViewById<View>(R.id.bgRoot) as ConstraintLayout
            val icon = layout.findViewById<View>(R.id.imgIcon) as AppCompatImageView
            val text = layout.findViewById<View>(R.id.tvContent) as TextView

            bgDrawable?.let { bgRoot.setBackgroundResource(it) }
            iconId?.let {
                icon.setImageResource(it)
            } ?: run {
                icon.gone()
            }
            if (iconId != 0) iconId?.let { icon.setImageResource(it) }
            else {
                icon.gone()
            }
            text.text = context.getString(contentId)
            text.setTextColor(context.getColor(textColor))

            try {
                toast.view = layout
                toast.setGravity(gravity, 0, 0)
            } catch (_: Exception) {
                toast.setText(context.getString(contentId))
            }
            toast.duration = duration
            return toast
        }
    }

}