package com.ads.admob

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.lib.mobileads.R
import androidx.core.graphics.drawable.toDrawable

class AdLoadingDialog(
    context: Context,
    private val customView: View? = null
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_ad_loading)

        window?.apply {
            setBackgroundDrawable(Color.WHITE.toDrawable())
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        setCancelable(false)

        val customViewContainer = findViewById<FrameLayout>(R.id.customViewContainer)
        val defaultLoadingView = findViewById<View>(R.id.defaultLoadingView)

        if (customView != null) {
            defaultLoadingView.visibility = View.GONE
            customViewContainer.visibility = View.VISIBLE
            customViewContainer.removeAllViews()
            if (customView.parent != null) {
                (customView.parent as ViewGroup).removeView(customView)
            }
            customViewContainer.addView(customView)
        } else {
            defaultLoadingView.visibility = View.VISIBLE
            customViewContainer.visibility = View.GONE
        }
    }
}