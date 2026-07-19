plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.maangatech.gojuagent"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maangatech.gojuagent"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Mirrors the existing goju-android WebView app's flavor structure so the two projects
    // stay operationally consistent for whoever manages release builds day to day.
    flavorDimensions += "environment"
    productFlavors {
        create("development") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "DEFAULT_SERVER_URL", "\"https://dev.gojucloud.maangatech.com/\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "DEFAULT_SERVER_URL", "\"https://staging.gojucloud.maangatech.com/\"")
        }
        create("production") {
            dimension = "environment"
            buildConfigField("String", "DEFAULT_SERVER_URL", "\"https://gojucloud.maangatech.com/\"")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = System.getenv("KEYSTORE_PASS") ?: ""
            keyAlias = "goju-agent"
            keyPassword = System.getenv("KEY_PASS") ?: ""
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-designsystem"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-security"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-ussd"))

    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-transactions"))
    implementation(project(":feature:feature-customers"))
    implementation(project(":feature:feature-sync"))

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation("com.google.android.material:material:1.12.0")

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.navigation)
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation(libs.hilt.navigation.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
