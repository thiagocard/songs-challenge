plugins {
    alias(libs.plugins.songs.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.feature.player)
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    implementation(projects.core.networking)
    implementation(projects.core.database)

    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.android.support)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.kotlin)

    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.splashscreen)
    testImplementation(libs.bundles.test)
}
