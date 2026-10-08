import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * Release key: a base64 PKCS12 keystore in SIGNING_KEYSTORE_B64 plus its password in SIGNING_PASSWORD
 * (alias in SIGNING_ALIAS, default "simplegrid"). They come from GitHub Actions secrets or the
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
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        releaseKeystore?.let { file ->
            create("release") {
                storeFile = file
                storeType = "pkcs12"
                storePassword = System.getenv("SIGNING_PASSWORD")
                keyAlias = System.getenv("SIGNING_ALIAS")?.takeIf { it.isNotBlank() } ?: "simplegrid"
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
    androidResources { generateLocaleConfig = true }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-service:2.9.4")
    implementation("androidx.core:core-ktx:1.17.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517") // android.jar's org.json is only a stub in JVM tests
}
