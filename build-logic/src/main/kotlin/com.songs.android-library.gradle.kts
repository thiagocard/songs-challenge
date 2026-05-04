import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    id("com.android.library")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    val moduleName = project.displayName
        .removePrefix("project ")
        .replace(":", ".")
        .replace("'", "")
        .replace("-", ".")

    namespace = "com.songs$moduleName"

    compileSdk = Config.compileSdkVersion

    packaging {
        resources {
            excludes.apply {
                add("META-INF/AL2.0")
                add("META-INF/LGPL2.1")
            }
        }
    }

    compileOptions {
        // Up to Java 11 APIs are available through desugaring
        // https://developer.android.com/studio/write/java11-minimal-support-table
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro")
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}
