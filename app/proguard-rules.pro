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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

 -keep class org.libpag.** {*;}
 -keep class androidx.exifinterface.** {*;}
 -keep class retrofit.** { *; }
 -keepclasseswithmembers class * {
     @retrofit2.http.* <methods>;
 }


# Keep ToJson/FromJson-annotated methods
-keepclassmembers class * {
  @com.squareup.moshi.FromJson <methods>;
  @com.squareup.moshi.ToJson <methods>;
}

-dontwarn javax.annotation.**

-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keep @com.squareup.moshi.JsonQualifier @interface *

# Enum field names are used by the integrated EnumJsonAdapter.
# values() is synthesized by the Kotlin compiler and is used by EnumJsonAdapter indirectly
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

# Keep helper method to avoid R8 optimisation that would keep all Kotlin Metadata when unwanted
-keepclassmembers class com.squareup.moshi.internal.Util {
    private static java.lang.String getKotlinMetadataClassName();
}


 # Preserve Gson classes
 -keep class com.google.gson.** { *; }
 -keepattributes Signature
 -keepattributes EnclosingMethod

 # Preserve OkHttp
 -dontwarn okhttp3.**
 -keep class okhttp3.** { *; }
 -keep interface okhttp3.** { *; }
 -dontwarn okio.**
 -keep class okio.** { *; }
 -keep class com.google.gson.reflect.TypeToken { *; }
 -keep class * extends com.google.gson.reflect.TypeToken

 -keepclassmembers,allowobfuscation class * {
   @com.google.gson.annotations.SerializedName <fields>;
 }

 -keep class * {
   @com.google.gson.annotations.SerializedName <fields>;
 }
 -keep class * extends com.google.gson.TypeAdapter

 # Application classes that will be serialized/deserialized over Gson
 -keep class com.myapplication.model.api.** { *; }
 -keepattributes AnnotationDefault,RuntimeVisibleAnnotations

 #start for adjust sdk
 -keep class com.adjust.sdk.** { *; }
 -keep class com.google.android.gms.common.ConnectionResult {
     int SUCCESS;
 }
 -keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
     com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
 }
 -keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
     java.lang.String getId();
     boolean isLimitAdTrackingEnabled();
 }
 -keep public class com.android.installreferrer.** { *; }
 #end of adjust sdk

 -keep class androidx.media3.** { *; }
 -keep interface androidx.media3.** { *; }

 -keep class com.github.anrwatchdog.** { *; }

 -dontwarn com.facebook.infer.annotation.Nullsafe$Mode
 -dontwarn com.facebook.infer.annotation.Nullsafe
