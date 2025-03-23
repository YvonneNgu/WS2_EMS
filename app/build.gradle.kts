plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Firebase integration
}

android {
    namespace = "com.example.workshop2"  // Namespace is defined as a string
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.workshop2"  // Use string literals for values
        minSdk = 26 // Lower minSdk from coding 1 for broader compatibility
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // String literal
    }

    buildTypes {
        getByName("release") {
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
    // Core dependencies
    implementation(libs.appcompat.v161)
    implementation(libs.material.v190)
    implementation(libs.activity.v180)
    implementation(libs.constraintlayout.v214)


    // ML Kit Barcode Scanner and Google Play Services
    implementation(libs.barcode.scanning)
    implementation(libs.play.services.base)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.code.scanner)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.swiperefreshlayout)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(libs.ext.junit)

    implementation (libs.core)
    implementation (libs.zxing.android.embedded)

    implementation (libs.zxing.android.embedded)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.multidex)

    implementation (libs.glide)  // Glide dependency
    annotationProcessor (libs.compiler)  // Glide annotation processor

    implementation (libs.material.v160)

    //Pdf
    implementation(libs.android.pdf.viewer)

    // RecyclerView
    implementation(libs.recyclerview)

    // Colour picker
    implementation(project(":library"))

    //Photo view
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    //Chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.airbnb.android:lottie:6.1.0")

    // Flexible Adapter
    implementation("com.github.shuhart:stickyheader:1.1.0")


}
