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

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ontabb.rate"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // `api`: FeedbackActivity kế thừa AppCompatActivity và layout dùng widget của appcompat,
    // app nhúng module phải thấy được các kiểu đó.
    api(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    // BottomSheetDialogFragment của RateAppBottomSheet.
    implementation(libs.material)

    // Lottie cho animation mặt cười trong hộp thoại.
    implementation(libs.lottie)
}
