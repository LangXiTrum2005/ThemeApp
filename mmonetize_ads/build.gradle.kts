plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lib.mobileads"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true   // Android methods return 0/null/false instead of throwing
        }
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.play.services.ads)
    implementation(libs.adjust.android)
    implementation(libs.android.installreferrer)
    implementation(libs.adjust.android.webbridge)
    implementation(libs.play.services.ads.identifier)
    implementation(libs.adjust.android.huawei.referrer)
    implementation(libs.androidx.recyclerview)
    implementation(libs.shimmer)
    implementation(libs.user.messaging.platform)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.koin.android)
}