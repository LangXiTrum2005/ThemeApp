/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.distribution)
    alias(libs.plugins.google.firebase.firebase.perf)
    alias(libs.plugins.tabb.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

val baseApplicationId = "thinklife.prism.wallpaper.widget.theme"

android {
    namespace = "thinklife.prism.wallpaper.widget.theme"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = baseApplicationId
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "STORE_ID", "\"$baseApplicationId\"")
        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"X6Kiaov2ZqhCk3fc7dGnCd\"")

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        debug {
            externalNativeBuild {
                cmake {
                    arguments("-DBASE_URL=https://theme-wallpaper.wwsmind.com/api/")
                }
            }
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            externalNativeBuild {
                cmake {
                    arguments("-DBASE_URL=https://theme-wallpaper.wwsmind.com/api/")
                }
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        resValues = true
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":ontabb_rate"))
    implementation(libs.firebase.perf)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // UI
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)

    // Coroutines
    implementation(libs.coroutines.android)
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.glide)
    implementation(libs.coil.network.okhttp)
    implementation(libs.lottie)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.config)
    implementation(libs.firebase.database)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit & Moshi
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    // Media & Animation
    implementation(libs.libpag)
    implementation(libs.simpleratingbar)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.eventbus)
    implementation(libs.shimmer)
    implementation(libs.gson)

    // Adjust & Referrer
    implementation(libs.adjust.android)
    implementation(libs.adjust.android.webbridge)
    implementation(libs.adjust.android.huawei.referrer)
    implementation(libs.android.installreferrer)

    // UI & Navigation
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.infer.annotation)

    // ads
    implementation(libs.play.services.ads.api)
    implementation(libs.facebook.android.sdk)
    implementation(libs.play.services.ads.identifier)
    implementation(libs.adshub)
    implementation("com.google.ads.mediation:applovin:13.6.4.2")
    implementation("com.google.ads.mediation:ironsource:9.6.0.1")
    implementation("com.google.ads.mediation:facebook:6.22.0.1")
    implementation("com.google.ads.mediation:mintegral:17.1.81.1")
    implementation("com.google.ads.mediation:pangle:8.3.0.3.0")
    implementation("com.unity3d.ads:unity-ads:4.20.1")
    implementation("com.google.ads.mediation:unity:4.20.1.0")
    implementation("com.iabgpp:iabgpp-encoder:3.2.5")
}
