import java.util.Properties

plugins {
    id("com.android.application")
}

// Signing: create keystore.properties at the project root (see README.md).
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.consobox"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.consobox"
        minSdk = 23
        targetSdk = 35
        versionCode = 51
        versionName = "1.23"
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreProps.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.viewpager:viewpager:1.1.0")
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
}
