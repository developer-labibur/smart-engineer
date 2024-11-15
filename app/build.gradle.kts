plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("androidx.navigation.safeargs.kotlin")
    kotlin("kapt") // This line enables kapt in Kotlin DSL
}

android {
    namespace = "com.rudhashi.smartengineer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rudhashi.smartengineer"
        minSdk = 25
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.preference)

    // Authenticate with Google
    implementation(libs.andoidx.credentials)
    implementation(libs.androidx.credential.play.services.auth)
    implementation(libs.googleid)

    // Import the Firebase BoM Dependencies
    implementation(platform(libs.firebase.bom)) // Firebase BOM
    implementation(libs.firebase.auth.ktx) // Firebase Authentication

    //implementation(libs.google.firebase.auth) // Firebase Authentication
    implementation(libs.firebase.database) // Firebase Database
    implementation(libs.firebase.firestore) // Firebase FireStore
    implementation(libs.firebase.analytics) // Firebase Analytics
    implementation(libs.firebase.storage) // Firebase Storage

    implementation(libs.circleimageview) // CircleImageView
    implementation (libs.imagepicker) // Pick Gallery Image & Capture Camera Image
    implementation (libs.amazingspinner) // Amazing Spinner

    implementation (libs.material.dialogs.core)
    implementation (libs.input)

    implementation(libs.shimmer) // Shimmer Effect for Facebook

    // API Integrations
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.glide) // Image from Glide
    implementation(libs.viewpager2)
    kapt("com.github.bumptech.glide:compiler:${libs.versions.glide.get()}") // For Glide annotation processing
    implementation("io.coil-kt:coil:2.5.0")
    implementation("com.tbuonomo:dotsindicator:5.0")
}