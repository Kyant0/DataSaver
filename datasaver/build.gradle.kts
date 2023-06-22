plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "com.kyant.datasaver"
version = "2023.6.1"

android {
    namespace = "com.kyant.datasaver"
    compileSdk = 33
    buildToolsVersion = "33.0.2"

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
}

dependencies {
    implementation("androidx.compose.runtime:runtime:1.6.0-alpha01")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.5.1")
}

afterEvaluate {
    publishing {
        publications {
            register("mavenRelease", MavenPublication::class) {
                groupId = "com.kyant"
                artifactId = "datasaver"
                version = "2023.6.1"
                from(components["release"])
            }
        }
    }
}
