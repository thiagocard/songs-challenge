import java.net.URI

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
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
