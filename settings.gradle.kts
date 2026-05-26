pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()  // This ensures you can access plugins from the Gradle Plugin Portal
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // Ensures project-level repositories are not used
    repositories {
        google()  // Required for Android-specific dependencies
        mavenCentral()  // Central repository for most dependencies
        maven { url = uri("https://jitpack.io") }  // Optional: For dependencies hosted on JitPack
    }
}

rootProject.name = "Smart Engineer"  // Set your root project name
include(":app")  // Include your app module (you can add more modules if necessary)