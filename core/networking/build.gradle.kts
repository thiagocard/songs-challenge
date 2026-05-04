plugins {
    alias(libs.plugins.songs.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}
android {
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        debug {
            buildConfigField("String", "HOST", Config.BuildField.ITUNES_API_BASE_URL)

        }
        getByName("release") {
            buildConfigField("String", "HOST", Config.BuildField.ITUNES_API_BASE_URL)
        }
    }
}
dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.networking)
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)
    testImplementation(libs.bundles.test)
}
