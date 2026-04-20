import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val cloudinaryCloudName = (localProperties.getProperty("cloudinary.cloud_name")
    ?: providers.gradleProperty("CLOUDINARY_CLOUD_NAME").orNull
    ?: "dwtvqd3nu").trim()

val cloudinaryUploadPreset = (localProperties.getProperty("cloudinary.upload_preset")
    ?: providers.gradleProperty("CLOUDINARY_UPLOAD_PRESET").orNull
    ?: "foodnow_unsigned").trim()

android {
    namespace = "com.example.foodnow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodnow"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"$cloudinaryUploadPreset\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // ← THÊM VÀO TỪ ĐÂY
    implementation("com.google.firebase:firebase-firestore")

    // Glide (tải ảnh)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // ViewPager2 (banner)
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // CircleIndicator (chấm tròn banner)
    implementation("me.relex:circleindicator:2.1.6")

    // ViewModel + LiveData (MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")

    // Cloudinary (upload ảnh)
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    // ← ĐẾN ĐÂY

    implementation("jp.wasabeef:glide-transformations:4.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
