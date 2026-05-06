plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.android.tools.common)
    implementation(libs.compose.gradle.plugin)
    implementation(libs.compose.compiler)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.android.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
}
