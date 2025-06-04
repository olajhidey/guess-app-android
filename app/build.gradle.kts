plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.taloscore.guessapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.taloscore.guessapp"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.navigation)
    /**
     * Hilt library for dependency injection
     */
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)
    // Datastore library for storing data in key-value pairs
    implementation(libs.androidx.datastore.preferences)
    /**
     * Retrofit library for making network requests
     */
    implementation(libs.retrofit.library)
    implementation(libs.retrofit.converter.gson)
    /**
     * OkHttp library for Retrofit
     */
    implementation(libs.okhttp.library)
    implementation(libs.okhttp.logging.interceptor)
    // Lifecycle KTX for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Splash Screen
    implementation(libs.androidx.splashscreen)
    // Gson library
    implementation(libs.gson.library)
    /**
     * Coil library for image loading
     */
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)

    // WebSocket Library
    implementation(libs.socket.io)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}