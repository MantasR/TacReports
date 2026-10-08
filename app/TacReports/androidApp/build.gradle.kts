import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * Release key: a base64 PKCS12 keystore in SIGNING_KEYSTORE_B64 plus its password in SIGNING_PASSWORD
 * (alias in SIGNING_ALIAS, default "upload"). They come from GitHub Actions secrets or the
 * environment and are never committed. Without them the release build uses the debug key.
 */
val releaseKeystore: File? = System.getenv("SIGNING_KEYSTORE_B64")?.takeIf { it.isNotBlank() }?.let { b64 ->
    layout.buildDirectory.file("signing/release.p12").get().asFile.apply {
        parentFile.mkdirs()
        writeBytes(Base64.getMimeDecoder().decode(b64.trim()))
    }
}

android {
    namespace = "lt.tacreports"
    compileSdk = 36

    defaultConfig {
        applicationId = "lt.tacreports"
        minSdk = 26
        targetSdk = 36
        // Play needs a higher code for every upload: CI builds use 100 + the workflow run number.
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()?.let { 100 + it } ?: 2
        versionName = "0.2.0"
    }

    signingConfigs {
        releaseKeystore?.let { file ->
            create("release") {
                storeFile = file
                storeType = "pkcs12"
                storePassword = System.getenv("SIGNING_PASSWORD")
                keyAlias = System.getenv("SIGNING_ALIAS")?.takeIf { it.isNotBlank() } ?: "upload"
                keyPassword = System.getenv("SIGNING_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: run {
                logger.warn("SIGNING_KEYSTORE_B64 not set: release APK is signed with the DEBUG key")
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.material3)
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
