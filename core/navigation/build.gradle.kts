plugins {
    alias(libs.plugins.songs.android.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
}
