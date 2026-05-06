import java.net.URI

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinx.kover)
}

dependencies {
    kover(project(":app"))
    kover(project(":core:common"))
    kover(project(":core:ui"))
    kover(project(":core:navigation"))
    kover(project(":core:networking"))
    kover(project(":core:database"))
    kover(project(":feature:home"))
    kover(project(":feature:home:domain"))
    kover(project(":feature:player"))
    kover(project(":feature:player:domain"))
    kover(project(":support:mock"))
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI.create("https://jitpack.io")
        }
    }
}
