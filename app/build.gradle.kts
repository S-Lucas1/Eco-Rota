plugins {
    alias(libs.plugins.android.application)
    kotlin("android") version "1.9.22"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ecorota"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ecorota"
        minSdk = 23  // Atualizado para atender ao requisito do Firebase Auth
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // MultiDex support
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Firebase BOM para gerenciar versões das dependências do Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
      // APIs do Firebase
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Google Play Services - usando versões compatíveis
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-base:18.2.0") 
    implementation("com.google.android.gms:play-services-tasks:18.0.2")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Core Kotlin
    implementation("androidx.core:core-ktx:1.12.0")
    
    // Dependências de UI e utilitários
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Dependências de teste
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}