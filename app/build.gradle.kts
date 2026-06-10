plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp.android)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.wavez.themewallpaper"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.themes.wallpaper.icon.widget"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
            isMinifyEnabled = false
            isShrinkResources = false

            externalNativeBuild {
                cmake {
                    arguments("-DBASE_URL=http://192.168.1.40/server4/public/")
                }
            }
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            externalNativeBuild {
                cmake {
                    arguments("-DBASE_URL=https://themesmobiletechnologyvn.xyz/")
                }
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

base {
    val versionCode = android.defaultConfig.versionCode
    val versionName = android.defaultConfig.versionName

    archivesName =
        "P67-ThemeWallpaper-v${versionName}(${versionCode})"
}

dependencies {
    implementation(project(":billing_library"))
    implementation(project(":mmonetize_ads"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.user.messaging.platform)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.libpag)
    implementation(libs.glide)
    implementation(libs.eventbus)
    implementation(libs.lottie)

    //firebase
    implementation(libs.firebase.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.google.firebase.crashlytics.ndk)
    implementation(libs.firebase.messaging)

    implementation(libs.infer.annotation)
    implementation(libs.facebook.android.sdk)
    implementation(libs.gson)
    implementation(libs.shimmer)

    implementation(libs.adjust.android)
    implementation(libs.android.installreferrer)
    implementation(libs.adjust.android.webbridge)
    implementation(libs.play.services.ads.identifier)
    implementation(libs.adjust.android.huawei.referrer)
    implementation(libs.androidx.recyclerview)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.billing)

    //mediation
    implementation(libs.facebook)
    implementation(libs.mintegral)
    implementation(libs.pangle)


}
