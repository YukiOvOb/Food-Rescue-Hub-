// APP android/app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    jacoco
}

jacoco {
    toolVersion = "0.8.13"
}

android {
    namespace = "com.example.foodrescuehub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodrescuehub"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://13.228.183.177:8080/\"")
        }
    }

    buildTypes {
        debug {
            // Enables unit-test coverage data generation for JVM tests.
            isTestCoverageEnabled = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

// ---- JaCoCo report for unit tests (devDebug) ----
// Produces:
// - app/build/reports/jacoco/jacocoDevDebugUnitTestReport/html/
// - app/build/reports/jacoco/jacocoDevDebugUnitTestReport/jacocoDevDebugUnitTestReport.xml
tasks.register<JacocoReport>("jacocoDevDebugUnitTestReport") {
    dependsOn("testDevDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*\$Companion*.*",
        "**/*\$Lambda\$*.*",
        "**/*\$inlined\$*.*"
    )

    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/devDebug") {
        exclude(fileFilter)
    }
    val javaClasses = fileTree("$buildDir/intermediates/javac/devDebug/classes") {
        exclude(fileFilter)
    }

    classDirectories.setFrom(files(kotlinClasses, javaClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testDevDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/devDebugUnitTest/testDevDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/devDebugUnitTest/testDevDebugUnitTest.ec"
            )
        }
    )
}

dependencies {
    implementation(libs.activity)
    // Core Library Desugaring for Java 8+ APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.glide)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.coordinatorlayout)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // ADDED: Browser for Chrome Custom Tabs
    implementation("androidx.browser:browser:1.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
