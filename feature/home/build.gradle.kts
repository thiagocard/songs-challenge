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

dependencies {
    implementation(projects.core.navigation)
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.networking)
    implementation(projects.core.database)
    implementation(projects.feature.home.domain)
    implementation(projects.support.mock)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.bundles.networking)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.navigation3)
    implementation(libs.coil)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.test)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.robolectric)
    testImplementation(libs.paging.testing)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core)
    debugImplementation(libs.compose.ui.test.manifest)
}
