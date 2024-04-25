import com.android.build.api.dsl.Packaging

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

    androidResources {
        packagingOptions {
            pickFirst("**")
        }

    }


    dependencies {



        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.9.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("com.google.firebase:firebase-database-ktx:20.2.2")
        implementation("androidx.navigation:navigation-fragment:2.7.5")
        implementation("androidx.navigation:navigation-ui:2.3.5")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-auth:21.0.1")

        implementation("androidx.camera:camera-core:1.1.0")
        implementation("androidx.camera:camera-lifecycle:1.1.0")
        implementation("androidx.camera:camera-view:1.1.0")
        implementation("androidx.camera:camera-camera2:1.1.0")


        implementation("net.sourceforge.tess4j:tess4j:5.2.1")
        implementation("com.rmtheis:tess-two:9.1.0")
        implementation ("com.google.android.gms:play-services-vision:20.1.3")
        implementation ("com.github.evrencoskun:TableView:v0.8.9.4")
        implementation ("com.squareup.okhttp3:okhttp:4.9.1")

        implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
        implementation ("com.squareup.retrofit2:retrofit:2.9.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation ("com.squareup.okhttp3:okhttp:4.9.1")
        implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
        implementation ("com.google.firebase:firebase-messaging:23.0.0")
        implementation ("ca.uhn.hapi:hapi-base:2.5.1")





        implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:11.0.1")


        configurations.implementation {
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")



        }


    }
}



