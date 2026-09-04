# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod
-keepattributes Exceptions

# Keep Google AdvertisingIdClient classes
-keep class com.google.android.gms.ads.identifier.** { *; }

# Parcelable models need CREATOR fields for Android framework unparceling.
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Hilt/Dagger generated classes are normally covered by consumer rules, but keep app entry points.
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# OkHttp/Okio optional platform integrations can trigger harmless R8 warnings.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.bouncycastle.**

-keep class com.android.installreferrer.** { *; }
-dontwarn com.android.installreferrer.**

# ==================== Facebook SDK / mediation ====================
# Pangle SDK
-keep class com.bytedance.sdk.** { *; }
-keep class com.bytedance.** { *; }
-keep class com.pangle.** { *; }
-keep class com.pgl.** { *; }
-keep class com.bykv.** { *; }
-keep class com.ss.android.** { *; }

-keep class com.google.ads.mediation.pangle.** { *; }

-keep class com.facebook.** { *; }
-dontwarn com.facebook.**
-keep class com.bytedance.sdk.** { *; }
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**

-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.work.**
-dontwarn androidx.room.**

# ==================== AdsHub SDK ====================
# Config parse bằng kotlinx.serialization — R8 đổi tên model là parse hỏng toàn bộ
-keep class com.adshub.monetization.model.** { *; }
-keepclassmembers class com.adshub.monetization.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.adshub.monetization.**$$serializer { *; }
-keepclassmembers class com.adshub.monetization.** { *** Companion; }
-keep class com.adshub.monetization.AdsController { *; }
-keep class com.adshub.monetization.engine.tracking.AnalyticsProvider { *; }
-dontwarn com.adshub.monetization.**

# GAID — AppsFlyer/AdMob gọi qua reflection
-keep class com.google.android.gms.ads.identifier.** { *; }
