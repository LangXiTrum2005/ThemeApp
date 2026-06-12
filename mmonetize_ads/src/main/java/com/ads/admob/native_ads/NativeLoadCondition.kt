package com.ads.admob.native_ads

fun interface NativeLoadCondition {
    fun shouldLoad(adPlacement: String): Boolean
}
