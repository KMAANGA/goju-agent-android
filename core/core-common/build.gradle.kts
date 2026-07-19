plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.maangatech.gojuagent.core.common"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.core.ktx)
    // api, not implementation: GojuAgentApplication (:app) directly injects a
    // @ApplicationScope CoroutineScope field, so kotlinx.coroutines types need to be
    // visible there too, not just inside this module.
    api(libs.coroutines.android)
    // api, not implementation: MoshiModule provides the shared Moshi singleton for the
    // whole app (see WorkflowSeeder in :app, which injects Moshi directly), so the type
    // itself needs to be visible on every module that transitively depends on core-common.
    api(libs.moshi.kotlin)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
