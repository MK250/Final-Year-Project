plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.sugarsync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sugarsync"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

repositories {

       // maven {
        //    url = uri("file:///Users/sanjivkumar/Downloads/Tess4J/dist")
       // }


    // Add other repositories as needed
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation ("androidx.navigation:navigation-ui:2.3.5")
    implementation("com.github.tesseract-ocr:tesseract-android:5.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:21.0.1")

    implementation ("androidx.camera:camera-core:1.1.0")
    implementation ("androidx.camera:camera-lifecycle:1.1.0")
    implementation ("androidx.camera:camera-view:1.1.0")
    implementation ("androidx.camera:camera-camera2:1.1.0")
    implementation ("com.github.tesseract-ocr:tesseract-android:3.4.2")
    implementation ("net.sourceforge.tess4j:tess4j:5.2.1")
    implementation ("com.rmtheis:tess-two:9.1.0")



    configurations.implementation {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        // implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")



    }


}