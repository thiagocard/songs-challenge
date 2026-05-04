include(":feature:player")


pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven(url = uri("https://oss.sonatype.org/content/repositories/snapshots/"))
    }
}

include(":app")
include(":core")
include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":core:networking")
include(":core:database")
include(":feature")
include(":feature:home")
include(":feature:home:domain")
include(":feature:player")
include(":feature:player:domain")
include(":support")
include(":support:mock")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
