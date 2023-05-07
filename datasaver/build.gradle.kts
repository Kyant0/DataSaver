plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "com.kyant.datasaver"
version = "2023.5.1"

android {
    namespace = "com.kyant.datasaver"
    compileSdk = 33
    buildToolsVersion = "34.0.0-rc3"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
}

dependencies {
    val composeVersion = "1.5.0-alpha03"
    implementation("androidx.compose.runtime:runtime:$composeVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.5.0")
}

afterEvaluate {
    publishing {
        publications {
            register("mavenRelease", MavenPublication::class) {
                groupId = "com.kyant"
                artifactId = "datasaver"
                version = "2023.5.1"
                from(components["release"])
            }
        }
    }
}
