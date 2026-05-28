#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wavez_controllerwidget_Secrets_getBaseURL(JNIEnv *env, jobject) {
    return env->NewStringUTF(BASE_URL);
}
