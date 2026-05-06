plugins {
    alias(libs.plugins.songs.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.songs.core.navigation"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
    
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    
    testImplementation(libs.bundles.test)
}
