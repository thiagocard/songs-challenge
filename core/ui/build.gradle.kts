plugins {
    alias(libs.plugins.songs.android.library)
    alias(libs.plugins.songs.compose)
}

android {
    namespace = "com.songs.core.ui"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.android.support)
    testImplementation(libs.bundles.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core)
    debugImplementation(libs.compose.ui.test.manifest)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.layout)
    implementation(libs.compose.material3.adaptive.navigation)
}
