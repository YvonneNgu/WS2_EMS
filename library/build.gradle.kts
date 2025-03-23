plugins {
    id("com.android.library") // Apply the Android Library plugin
}

android {
    namespace = "com.flask.colorpicker" // library's package name
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    lint {
        targetSdk = 34 // app's targetSdk value
    }

    testOptions {
        targetSdk = 34 // app's targetSdk value
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.1.0")
}
