plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    id("de.mannodermaus.android-junit") version "2.0.2"
    id("tech.apter.junit5.jupiter.robolectric-extension-gradle-plugin") version "0.9.0"
}

android {
    namespace = "com.joehxblog.qrreader"
    compileSdk {
        version = release(37)
    }

    buildFeatures {
        compose = true
    }

    defaultConfig {
        applicationId = "com.joehxblog.qrreader"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.runtime)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.ez.vcard)
    implementation(libs.biweekly)

    implementation(libs.barcode.scanning)
    implementation(libs.coil.compose)

    implementation(libs.androidx.ui.tooling.preview)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.robolectric)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
//    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}