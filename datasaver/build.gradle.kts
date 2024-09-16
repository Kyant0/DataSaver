plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "com.kyant.datasaver"
version = "2024.1"

android {
    namespace = "com.kyant.datasaver"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.compose.runtime:runtime:1.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.7.2")
}

afterEvaluate {
    publishing {
        publications {
            register("mavenRelease", MavenPublication::class) {
                groupId = "com.kyant"
                artifactId = "datasaver"
                version = "2024.1"
                from(components["release"])
            }
        }
    }
}
