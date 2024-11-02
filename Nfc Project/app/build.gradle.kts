

// Version pour le fichier app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "ma.ensa.projet"
    compileSdk = 34

    defaultConfig {
        applicationId = "ma.ensa.projet"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
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
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "androidx.core" && requested.name == "core-ktx") {
                useVersion("1.12.0")
            }
            if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-")) {
                useVersion("1.9.0")
            }
            if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-coroutines-")) {
                useVersion("1.7.3")
            }
        }
    }
}

dependencies {
    implementation("androidx.multidex:multidex:2.0.1")

    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // NFC Dependencies
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Lifecycle Components
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")



    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}