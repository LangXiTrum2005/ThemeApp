package com.wavez.controllerwidget.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Insets
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.util.concurrent.LinkedBlockingQueue


fun Context.getDimension(value: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, this.resources.displayMetrics
    ).toInt()
}

fun Activity.getScreenWidth(): Int {
    return if (SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets: Insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun Activity.getScreenHeight(): Int {
    return if (SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets: Insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.top - insets.bottom
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
)

fun hideNavigationButton(window: Window) {
    /**
     * note:    add <item name="android:windowTranslucentStatus">true</item> to themes style
     * **/
    try {
        val decorView = window.decorView
        val uiOptions =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    } catch (e: Exception) {
        e.printStackTrace()
    }
    window.fullScreen()
}

fun Window.fullScreen() {
    setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
}

inline fun <reified T : Parcelable> Intent.getParcel(key: String): T? =
    IntentCompat.getParcelableExtra(this, key, T::class.java)

inline fun <reified T : Parcelable> Bundle.getParcel(key: String): T? =
    BundleCompat.getParcelable(this, key, T::class.java)

inline fun <reified T : Serializable> Intent.getSerializable(key: String): T? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Serializable> Intent.getSerial(key: String): T? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

inline fun <reified T : Serializable> Bundle.getSerial(key: String): T? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >=  Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}


suspend fun getAdvertisingId(context: Context): String {
    return withContext(Dispatchers.IO) {
        val idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        idInfo.id ?: ""
    }
}


fun Context.getIcon(packageName: String): Drawable? {
    return try {
        runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T : View> View.findFirstViewOfType(): T? =
    findFirstViewOfType(T::class.java)

fun <T : View> View.findFirstViewOfType(type: Class<T>): T? {
    if (type.isInstance(this)) return type.cast(this)
    if (this !is ViewGroup) return null
    for (i in 0 until childCount) {
        getChildAt(i).findFirstViewOfType(type)?.let { return it }
    }
    return null
}

fun <T> Flow<T>.collectWithLifecycle(
    activity: AppCompatActivity,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    activity.lifecycleScope.launch {
        activity.repeatOnLifecycle(state) {
            collectLatest {
                action(it)
            }
        }
    }
}

fun <T> Flow<T>.collectWithLifecycle(
    fragment: Fragment,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    fragment.viewLifecycleOwner.lifecycleScope.launch {
        fragment.viewLifecycleOwner.repeatOnLifecycle(state) {
            collectLatest {
                action(it)
            }
        }
    }
}

fun <T> Flow<T>.cacheLatest(capacity: Int): Flow<List<T>> {
    return scan<T, LinkedBlockingQueue<T>>(LinkedBlockingQueue(capacity)) { cache, item ->
        while (cache.remainingCapacity() < 1) {
            cache.poll()
        }
        cache.add(item)
        cache
    }
        .map { it.toList() }
        .drop(1)
}
