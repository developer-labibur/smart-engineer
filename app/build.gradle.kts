plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    id("androidx.navigation.safeargs.kotlin")
    kotlin("kapt")
}

android {
    namespace = "com.softylasa.smartengineer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.softylasa.smartengineer"
        minSdk = 25
        targetSdk = 35
        versionCode = 6
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true // Enable minification
            isShrinkResources = true // Enable shrink
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
    implementation(libs.firebase.crashlytics)
    implementation(libs.core.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.transport.backend.cct)
    implementation(libs.transport.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Authenticate with Google
    implementation(libs.andoidx.credentials)
    implementation(libs.androidx.credential.play.services.auth)
    implementation(libs.googleid)
    implementation (libs.play.services.auth) // Latest version

    // Google ----------------------------------------------------------------
    implementation (libs.play.services.base)
    implementation (libs.play.services.auth.api.phone)
    implementation (libs.firebase.appcheck.interop)
    implementation (libs.firebase.database.collection)
    implementation (libs.play.services.appset)
    implementation (libs.play.services.ads)
    implementation (libs.play.services.measurement.impl)

    // Import the Firebase BoM Dependencies
    implementation(platform(libs.firebase.bom)) // Firebase BOM
    implementation(libs.firebase.auth.ktx) // Firebase Authentication

    implementation(libs.firebase.auth) // Firebase Authentication
    implementation(libs.firebase.database) // Firebase Database
    implementation(libs.firebase.firestore) // Firebase FireStore
    implementation(libs.firebase.analytics) // Firebase Analytics
    implementation(libs.firebase.storage) // Firebase Storage

    implementation(libs.circleimageview) // CircleImageView
    implementation(libs.imagepicker) // Pick Gallery Image & Capture Camera Image
    implementation(libs.amazingspinner) // Amazing Spinner

    implementation(libs.material.dialogs.core)
    implementation(libs.input)

    implementation(libs.shimmer) // Shimmer Effect for Facebook
    implementation(libs.lottie) // Lottie Animation

    // API Integrations
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.preference)

    implementation(libs.glide) // Image from Glide
    implementation(libs.viewpager2)
    kapt("com.github.bumptech.glide:compiler:${libs.versions.glide.get()}") // For Glide annotation processing
    implementation(libs.coil)
    implementation(libs.dotsindicator)

    implementation(libs.pdf.viewer.android) { exclude(group = "com.android.support", module = "support-compat") }

    implementation (libs.okhttp) // For downloading the PDF
    implementation (libs.com.squareup.okhttp.okhttp)

    implementation(libs.androidx.browser) //Chrome Custom Tabs


    // Add latest gRPC dependencies (needed for Firestore)
    implementation (libs.grpc.okhttp)
    implementation (libs.grpc.protobuf.lite)
    implementation (libs.grpc.stub)

}

// Apply the Google Services plugin here
apply(plugin = "com.google.gms.google-services")