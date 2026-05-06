import extensions.getBundle
import extensions.getLibrary

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins {
    id("com.songs.android-library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures.compose = true
    buildFeatures.buildConfig = true

    packaging {
        resources {
            excludes.apply {
                add("META-INF/AL2.0")
                add("META-INF/LGPL2.1")
            }
        }
    }
}

dependencies {
    implementation(platform(libs.getLibrary("compose.bom")))
    androidTestImplementation(platform(libs.getLibrary("compose.bom")))

    implementation(libs.getBundle("compose"))
    debugImplementation(libs.getLibrary("compose.ui.tooling"))
}
