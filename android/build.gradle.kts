// Top-level build file where you can add configuration options common to all sub-projects/modules.
// ROOT android/build.gradle.kts
plugins {
    // We hardcoded the version here to bypass the TOML file for now
    id("com.android.application") version "8.13.1" apply false
    id("com.android.library") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}


dependencies {


    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")




    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    implementation("androidx.browser:browser:1.5.0")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

}