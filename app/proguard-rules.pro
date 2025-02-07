##############################
# 🚀 Firebase Firestore Rules (Fixed for CollectionGroup Queries)
##############################

# Keep essential Firestore classes
-keep class com.google.firebase.firestore.** { *; }

# Keep Firestore model classes with annotations
-keepclassmembers class com.softylasa.smartengineer.data.models.** {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.Exclude <fields>;
    public <fields>;
    public <methods>;
}

# Prevent Firestore model class names from being obfuscated
-keepnames class com.softylasa.smartengineer.data.models.** { *; }

# Ensure Firestore snapshot and listener classes remain intact
-keep class com.google.firebase.firestore.QuerySnapshot { *; }
-keep class com.google.firebase.firestore.DocumentSnapshot { *; }
-keep class com.google.firebase.firestore.EventListener { *; }
-keep class com.google.firebase.firestore.FirebaseFirestoreException { *; }

# Ensure Firestore collectionGroup queries work properly
-keep class com.google.firebase.firestore.Query { *; }
-keep class com.google.firebase.firestore.CollectionReference { *; }

##############################
# 🚀 gRPC (Firestore Uses gRPC)
##############################

# Keep gRPC dependencies (Firestore relies on this)
-keep class io.grpc.** { *; }
-keep class io.grpc.okhttp.** { *; }
-keep class io.grpc.protobuf.** { *; }

# Keep OkHttp classes that gRPC depends on
-keep class com.squareup.okhttp.** { *; }
-keep class io.grpc.okhttp.internal.** { *; }
-keep class io.grpc.okhttp.OkHttpChannelBuilder { *; }
-keep class io.grpc.okhttp.internal.ConnectionSpec { *; }
-keep class io.grpc.okhttp.Utils { *; }

# Specific classes that are missing (OkHttp dependencies used by gRPC)
-keep class com.squareup.okhttp.CipherSuite { *; }
-keep class com.squareup.okhttp.ConnectionSpec { *; }
-keep class com.squareup.okhttp.TlsVersion { *; }

##############################
# 🚀 Kotlin Metadata (Prevents issues with Firestore)
##############################

# Keep annotations to ensure Firestore model serialization works
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

##############################
# 🚀 OkHttp (Networking Library)
##############################

# Keep essential OkHttp classes (including older versions)
-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp.** { *; }  # 👈 Keeps older OkHttp versions

# Ensure OkHttp logging interceptor is not stripped
-keep class okhttp3.logging.** { *; }

##############################
# 🚀 Retrofit & API Service Rules (Optimized)
##############################

# Keep Retrofit core classes
-keep class retrofit2.** { *; }

# Keep Retrofit API services (prevent obfuscation)
-keep interface com.softylasa.smartengineer.network.ApiService { *; }

# Keep Gson classes (used for JSON parsing)
-keep class com.google.gson.** { *; }

# Keep Gson converter for Retrofit
-keep class retrofit2.converter.gson.GsonConverterFactory { *; }

# Ensure Gson serialization annotations are preserved
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

##############################
# 🚀 Firestore CollectionGroup Fix
##############################

# Prevent Firestore `collectionGroup` queries from breaking due to obfuscation
-keep class com.google.firebase.firestore.Query { *; }
-keep class com.google.firebase.firestore.FirebaseFirestore { *; }

##############################
# 🚀 Debugging Rules (Optional)
##############################

#print ProGuard outputs for debugging (useful for troubleshooting)
-printusage proguard-usage.txt
-printseeds proguard-seeds.txt
-printmapping proguard-mapping.txt

##############################
# 🚀 Optimization Rules (Reduce APK Size)
##############################

# Enable ProGuard optimizations while keeping required components
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Prevent ProGuard from renaming Firebase, Retrofit, and critical networking classes
-keepnames class com.google.** { *; }
-keepnames class retrofit2.** { *; }

# Suppress warnings about unused classes in libraries (prevents unnecessary logs)
-dontwarn retrofit2.**
-dontwarn com.google.gson.**
-dontwarn com.bumptech.glide.**
-dontwarn com.squareup.okhttp3.**

# Keep Retrofit and Gson models
-keep class com.softylasa.smartengineer.data.models.** { *; }

# Keep important data models used in API responses
-keep class com.softylasa.smartengineer.data.models.BtebNotice { *; }
-keep class com.softylasa.smartengineer.network.NoticeResponse { *; }

# Keep Retrofit API service interfaces
-keep class com.softylasa.smartengineer.network.ApiService { *; }

# Preserve Retrofit annotations
-keepattributes Signature
-keepattributes *Annotation*

# Suppress warnings related to OkHttp and Retrofit
-dontwarn okhttp3.**