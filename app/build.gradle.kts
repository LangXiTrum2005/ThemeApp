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
}

val baseApplicationId = "com.onetabb.template.android"

android {
    namespace = "com.onetabb.template.android"
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
    }

    buildTypes {
        debug {
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    // ads
    implementation(libs.play.services.ads.api)
    implementation(libs.facebook.android.sdk)
    implementation(libs.adshub)
    implementation("com.google.ads.mediation:facebook:6.22.0.0")
    implementation("com.google.ads.mediation:pangle:8.2.0.4.0")
}
