// Top-level build file where you can add configuration options common to all sub-projects/modules.
// ROOT android/build.gradle.kts
plugins {
    // We hardcoded the version here to bypass the TOML file for now
    id("com.android.application") version "8.7.2" apply false
    id("com.android.library") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}