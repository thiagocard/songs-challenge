plugins {
    alias(libs.plugins.songs.android.library)
    alias(libs.plugins.songs.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.networking)
    implementation(projects.core.database)
    implementation(projects.feature.player.domain)
    implementation(projects.support.mock)

    implementation(libs.bundles.networking)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.navigation3)
    implementation(libs.coil)
    implementation(libs.compose.animation.graphics)
    implementation(libs.bundles.media3)

    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.layout)
    implementation(libs.compose.material3.adaptive.navigation)

    testImplementation(libs.bundles.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(projects.support.mock)
}
