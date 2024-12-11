# Keep Google services related classes (required for Firebase, Play Services, etc.)
-keep class com.google.android.gms.** { *; }

# Keep classes related to Firebase (if you use Firebase in your app)
-keep class com.google.firebase.** { *; }

# If you're using Glide, Picasso, or any image libraries that use reflection, add keep rules:
-keep class com.bumptech.glide.** { *; }
-keep class com.squareup.picasso.** { *; }

# If you're using Retrofit (if applicable)
-keep class retrofit2.** { *; }

# If you're using any other libraries that use reflection, add keep rules for those
-keep class com.shockwave.** { *; }

# Keep public classes that extend ListResourceBundle (typically for localization purposes)
-keep public class * extends java.util.ListResourceBundle

# If you're using BackupAgentHelper, keep those classes (remove if not using backups)
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.app.backup.BackupAgent
