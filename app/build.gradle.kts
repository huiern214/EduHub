plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eduhub"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eduhub"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Enable view binding (optional, may use findViewById)
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.navigation:navigation-fragment:2.7.5")
    implementation ("androidx.navigation:navigation-ui:2.7.5")

    // Firebase
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.firebase:firebase-storage:20.3.0")
    implementation ("com.google.firebase:firebase-analytics:21.5.0")

    // PDF view library
    implementation ("com.github.barteksc:android-pdf-viewer:2.8.2")
    implementation ("org.greenrobot:eventbus:3.2.0")
    implementation ("com.google.firebase:firebase-firestore:24.10.0")

    //Profile Image View
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
}
