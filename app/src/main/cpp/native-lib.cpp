#include <jni.h>
#include <string>
#include <iostream>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mr_1w_resourceplus_activities_splash_1activity_SplashActivity_base(JNIEnv *env,
                                                                            jclass clazz) {
    std::string baseURL = "http://51.89.165.55:3000/";
    return env->NewStringUTF(baseURL.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_mr_1w_resourceplus_activities_splash_1activity_SplashActivity_baseAPIURL(JNIEnv *env,
                                                                                  jclass clazz) {
    std::string baseURL = "http://51.89.165.55:3000/";
    return env->NewStringUTF(baseURL.c_str());
}