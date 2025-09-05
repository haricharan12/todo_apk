plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.testapp101"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.testapp101"
        minSdk = 21
        targetSdk = 36   // default; flavors below will override
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 👇 Add flavors to toggle targetSdk quickly
    flavorDimensions += "target"
    productFlavors {
        create("t22") {
            dimension = "target"
            targetSdk = 22
            applicationIdSuffix = ".t22"
            versionNameSuffix = "-t22"
        }
        create("t23") {
            dimension = "target"
            targetSdk = 23
            applicationIdSuffix = ".t23"
            versionNameSuffix = "-t23"
        }
        create("t33") {
            dimension = "target"
            targetSdk = 33
            applicationIdSuffix = ".t33"
            versionNameSuffix = "-t33"
        }
        create("t36") {
            dimension = "target"
            targetSdk = 36
            applicationIdSuffix = ".t36"
            versionNameSuffix = "-t36"
        }
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

    // Network demo
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.activity:activity-compose:1.9.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
