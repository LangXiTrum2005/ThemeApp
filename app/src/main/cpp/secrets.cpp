#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wavez_themewallpaper_Secrets_getBaseURL(JNIEnv *env, jobject) {
    return env->NewStringUTF(BASE_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_onetabb_template_android_Secrets_getBaseURL(JNIEnv *env, jobject) {
    return env->NewStringUTF(BASE_URL);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_thinklife_prism_wallpaper_widget_theme_Secrets_getBaseURL(JNIEnv *env, jobject) {
    return env->NewStringUTF(BASE_URL);
}


